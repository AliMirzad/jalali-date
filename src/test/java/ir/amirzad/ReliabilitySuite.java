package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;
import ir.amirzad.util.PersianDateUtils;
import ir.amirzad.util.PersianPeriod;
import ir.amirzad.interop.DateInterop;
import ir.amirzad.jackson.PersianDateTimeModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * یک سوتِ تستِ سنگین برای افزایش اطمینان مهندسی.
 * زمان اجرا ممکن است چند ثانیه طول بکشد (بسته به ماشین).
 */
public class ReliabilitySuite {

    /* ======================  A) PROPERTY TESTS  ====================== */

    @Test
    public void property_roundtrips_random_10000() {
        Random rnd = new Random(12345);
        for (int i = 0; i < 10_000; i++) {
            int y = 1200 + rnd.nextInt(600); // 1200..1799 جلالی
            int m = 1 + rnd.nextInt(12);
            int d = 1 + rnd.nextInt(PersianDate.lengthOfMonth(y, m));
            PersianDate pd = PersianDate.of(y, m, d);

            // JDN roundtrip
            long j = pd.toJulianDay();
            PersianDate backJ = PersianDate.ofJulianDay(j);
            assertEquals(pd, backJ);

            // Gregorian roundtrip
            LocalDate g = pd.toGregorian();
            PersianDate backG = PersianDate.fromGregorian(g);
            assertEquals(pd, backG);

            // Monotonicity of JDN
            long j2 = pd.plusDays(1).toJulianDay();
            assertEquals(j + 1, j2);
        }
    }

    @Test
    public void property_diff_with_gregorian_plusDays() {
        // هر تاریخ را به گریگوری ببریم، یک روز اضافه کنیم و برگردیم؛ باید معادل plusDays(1) باشد
        Random rnd = new Random(54321);
        for (int i = 0; i < 5000; i++) {
            int y = 1300 + rnd.nextInt(300); // 1300..1599
            int m = 1 + rnd.nextInt(12);
            int d = 1 + rnd.nextInt(PersianDate.lengthOfMonth(y, m));
            PersianDate pd = PersianDate.of(y, m, d);

            PersianDate nextByJalali = pd.plusDays(1);
            LocalDate g = pd.toGregorian().plusDays(1);
            PersianDate nextByGreg = PersianDate.fromGregorian(g);
            assertEquals(nextByJalali, nextByGreg);

            PersianDate prevByJalali = pd.minusDays(1);
            LocalDate g2 = pd.toGregorian().minusDays(1);
            PersianDate prevByGreg = PersianDate.fromGregorian(g2);
            assertEquals(prevByJalali, prevByGreg);
        }
    }

    /* ======================  B) EXHAUSTIVE SWEEP  ====================== */

    @Test
    public void sweep_years_1300_to_1405_all_days() {
        // حدوداً ~ 387*365 ~ 141k روز؟ (بسته به بازه؛ اینجا 106 سال ~ 38k * 4 ؟ زمان‌بر نیست)
        for (int jy = 1300; jy <= 1405; jy++) {
            for (int jm = 1; jm <= 12; jm++) {
                int len = PersianDate.lengthOfMonth(jy, jm);
                for (int jd = 1; jd <= len; jd++) {
                    PersianDate d = PersianDate.of(jy, jm, jd);

                    // JDN roundtrip
                    PersianDate back = PersianDate.ofJulianDay(d.toJulianDay());
                    assertEquals(d, back);

                    // Gregorian roundtrip
                    PersianDate back2 = PersianDate.fromGregorian(d.toGregorian());
                    assertEquals(d, back2);

                    // plus/minus inverse
                    assertEquals(d, d.plusDays(10).minusDays(10));
                }
            }
        }
    }

    /* ======================  C) EDGE CASES  ====================== */

    @Test
    public void edges_month_boundaries_and_leap() {
        // آخر/اول ماه‌ها
        for (int y = 1390; y <= 1405; y++) {
            for (int m = 1; m <= 12; m++) {
                int len = PersianDate.lengthOfMonth(y, m);
                PersianDate end = PersianDate.of(y, m, len);
                PersianDate next = end.plusDays(1);
                if (m < 12) {
                    assertEquals(y, next.getYear());
                    assertEquals(m + 1, next.getMonthValue());
                    assertEquals(1, next.getDayOfMonth());
                } else {
                    // انتهای اسفند → فروردین سال بعد
                    assertEquals(y + 1, next.getYear());
                    assertEquals(1, next.getMonthValue());
                    assertEquals(1, next.getDayOfMonth());
                }
            }
        }

        // اسفند کبیسه vs غیرکبیسه
        int[] years = {1390, 1391, 1394, 1395, 1398, 1399, 1402, 1403};
        for (int y : years) {
            int esf = PersianDate.lengthOfMonth(y, 12);
            assertTrue("Esfand len must be 29 or 30 for " + y, esf == 29 || esf == 30);
            // اگر 30 بود، روز 30 ام معتبر است و بعدش باید 1 فروردین شود
            if (esf == 30) {
                PersianDate d = PersianDate.of(y, 12, 30);
                PersianDate next = d.plusDays(1);
                assertEquals(y + 1, next.getYear());
                assertEquals(1, next.getMonthValue());
                assertEquals(1, next.getDayOfMonth());
            }
        }
    }

    /* ======================  D) FORMAT/PARSE & JACKSON  ====================== */

    @Test
    public void formatter_and_parse_various() {
        PersianDate d = PersianDate.of(1404, 8, 19);

        PersianDateFormatter latin = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        String s1 = latin.format(d);
        assertEquals("1404/08/19", s1);
        assertEquals(d, latin.parseDate(s1));

        PersianDateTime dt = PersianDateTime.of(1404, 8, 19, 13, 5, 9, 123_000_000);
        PersianDateFormatter ts = new PersianDateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS", PersianDateFormatter.DigitStyle.LATIN);
        String t1 = ts.format(dt);
        assertEquals("1404-08-19T13:05:09.123", t1);
        assertEquals(dt, ts.parseDateTime(t1));
    }

    @Test
    public void jackson_roundtrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(
                PersianDateTimeModule.builder()
                        .datePattern("yyyy-MM-dd")
                        .dateTimePattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                        .lenient(false)
                        .build()
        );

        PersianDate d = PersianDate.of(1404, 8, 19);
        String jsonD = mapper.writeValueAsString(d);
        PersianDate backD = mapper.readValue(jsonD, PersianDate.class);
        assertEquals(d, backD);

        PersianDateTime dt = PersianDateTime.of(1404, 8, 19, 13, 5, 9, 123_000_000);
        String jsonDT = mapper.writeValueAsString(dt);
        PersianDateTime backDT = mapper.readValue(jsonDT, PersianDateTime.class);
        assertEquals(dt, backDT);
    }

    /* ======================  E) INTEROP WITH DATE/ZONE  ====================== */

    @Test
    public void interop_date_multiple_zones() {
        ZoneId[] zones = { ZoneId.of("Asia/Tehran"), ZoneId.of("UTC"), ZoneId.of("Europe/Berlin") };

        for (ZoneId z : zones) {
            PersianDate d = PersianDate.of(1404, 8, 19);
            String txt = DateInterop.toJalaliString(new Date(), "yyyy/MM/dd", z, PersianDateFormatter.DigitStyle.LATIN);
            // متن را به جلالی پارس کن و برگردان راستی آزمایی.
            PersianDate parsed = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN).parseDate(txt);

            // رفت و برگشت به Date (شروع روز) و تطبیق با پارس‌شده
            Date back = DateInterop.parseJalaliToDate(txt, "yyyy/MM/dd", z);
            PersianDate backP = PersianDate.fromGregorian(back.toInstant().atZone(z).toLocalDate());
            assertEquals(parsed, backP);
        }
    }

    /* ======================  F) CONCURRENCY (SMOKE)  ====================== */

    @Test
    public void concurrency_formatter_parse() throws Exception {
        PersianDateFormatter f = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 1300; i <= 1450; i++) {
            final int y = i;
            tasks.add(() -> {
                for (int m = 1; m <= 12; m++) {
                    int len = PersianDate.lengthOfMonth(y, m);
                    for (int d = 1; d <= len; d++) {
                        PersianDate pd = PersianDate.of(y, m, d);
                        String s = f.format(pd);
                        PersianDate back = f.parseDate(s);
                        if (!pd.equals(back)) return false;
                    }
                }
                return true;
            });
        }
        java.util.concurrent.ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Boolean>> results = pool.invokeAll(tasks);
        pool.shutdown();
        for (Future<Boolean> r : results) {
            assertTrue(r.get());
        }
    }

    /* ======================  G) PERIOD & UTILS  ====================== */

    @Test
    public void utils_daysBetween_and_periodConsistency() {
        PersianDate a = PersianDate.of(1400, 1, 1);
        PersianDate b = PersianDate.of(1400, 12, 29);
        long days = PersianDateUtils.daysBetween(a, b);
        assertEquals(b.toJulianDay() - a.toJulianDay(), days);

        PersianPeriod p = PersianPeriod.between(a, b);
        assertEquals(b, p.addTo(a));
        assertEquals(a, p.subtractFrom(b));
    }
}
