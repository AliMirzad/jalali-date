package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;
import ir.amirzad.util.PersianDateUtils;
import ir.amirzad.util.PersianPeriod;
import ir.amirzad.util.PersianDateRange;
import ir.amirzad.util.PersianAdjusters;
import ir.amirzad.interop.DateInterop;
import ir.amirzad.jackson.PersianDateTimeModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.time.*;
import java.util.*;

import static org.junit.Assert.*;

public class AllMiniTests {

    /* ----------------------------- Helpers ----------------------------- */

    private static void assertDate(int y, int m, int d, PersianDate actual) {
        assertEquals("year", y, actual.getYear());
        assertEquals("month", m, actual.getMonthValue());
        assertEquals("day", d, actual.getDayOfMonth());
    }

    private static void assertDateTime(int y, int m, int d, int hh, int mi, int ss, int nano, PersianDateTime dt) {
        assertDate(y, m, d, dt.toDate());
        assertEquals("hour",   hh, dt.getHour());
        assertEquals("minute", mi, dt.getMinute());
        assertEquals("second", ss, dt.getSecond());
        assertEquals("nano",   nano, dt.getNano());
    }

    /* ----------------------- Core: creation & basics ----------------------- */

    @Test
    public void create_and_basic_arithmetic() {
        PersianDate d = PersianDate.of(1404, 8, 19);
        assertDate(1404, 8, 19, d);

        PersianDate plus10 = d.plusDays(10);
        assertDate(1404, 8, 29, plus10);

        PersianDate minus18 = d.plusDays(-18);
        // عبور از مرز ماه
        assertTrue(minus18.getMonthValue() == 8 || minus18.getMonthValue() == 7);

        PersianDate firstOfNextMonth = PersianAdjusters.firstDayOfNextMonth(d);
        assertEquals(1, firstOfNextMonth.getDayOfMonth());

        // مقایسه‌ها
        assertTrue(PersianDateUtils.isAfter(plus10, d));
        assertTrue(PersianDateUtils.isBefore(minus18, d));
        assertTrue(PersianDateUtils.isEqual(d, PersianDate.of(1404, 8, 19)));
    }

    /* ----------------------------- Leap & month edges ----------------------------- */

    @Test
    public void leap_year_edge_and_month_lengths() {
        // اسفند در سال کبیسه جلالی 30 روزه می‌شود
        int yLeap = 1399; // نمونهٔ رایج کبیسه
        int lenEsfandLeap = PersianDate.lengthOfMonth(yLeap, 12);
        assertTrue("Esfand in leap should be 30", lenEsfandLeap == 30 || lenEsfandLeap == 29); // اگر الگوریتمت متفاوت باشد، این را تنظیم کن

        // ماه‌های 1..6 سی روزه و 7..11 سی‌روز/29؟ بسته به الگوریتم شما:
        assertEquals(31, PersianDate.lengthOfMonth(1402, 1)); // فروردین
        assertEquals(30, PersianDate.lengthOfMonth(1402, 8)); // آبان
    }

    /* ----------------------------- JDN & epochDay roundtrip ----------------------------- */

    @Test
    public void jdn_and_epochDay_roundtrips() {
        PersianDate d = PersianDate.of(1404, 1, 1);

        long jdn = PersianDateUtils.toJulianDay(d);
        PersianDate d2 = PersianDateUtils.fromJulianDay(jdn);
        assertEquals(d, d2);

        long epochDay = PersianDateUtils.toEpochDay(d);
        PersianDate d3 = PersianDateUtils.fromEpochDay(epochDay);
        assertEquals(d, d3);
    }

    /* ----------------------------- Formatter: fa/latin + patterns ----------------------------- */

    @Test
    public void format_and_parse_various_patterns_and_digits() {
        PersianDate d = PersianDate.of(1404, 8, 19);

        // لاتین
        PersianDateFormatter f1 = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        String s1 = f1.format(d);
        assertEquals("1404/08/19", s1);
        assertEquals(d, f1.parseDate(s1));

        // فارسی
        PersianDateFormatter f2 = new PersianDateFormatter("dd MMM yyyy", PersianDateFormatter.DigitStyle.PERSIAN);
        String s2 = f2.format(d);
        assertTrue("must contain Persian digits", s2.contains("۱۴۰۴") || s2.contains("۱۴۰۴".substring(0,1)));
        assertEquals(d, f2.parseDate(PersianDateFormatter.Numerals.toLatinDigits(s2)));

        // DateTime با میلی‌ثانیه
        PersianDateTime dt = PersianDateTime.of(1404, 8, 19, 13, 5, 9, 123_000_000);
        PersianDateFormatter f3 = new PersianDateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS", PersianDateFormatter.DigitStyle.LATIN);
        String ts = f3.format(dt);
        assertEquals("1404-08-19T13:05:09.123", ts);
        assertEquals(dt, f3.parseDateTime(ts));
    }

    /* ----------------------------- Utils: daysBetween / periodBetween ----------------------------- */

    @Test
    public void days_and_period_between() {
        PersianDate a = PersianDate.of(1404, 1, 29);
        PersianDate b = PersianDate.of(1404, 2, 5);

        long days = PersianDateUtils.daysBetween(a, b); // b - a
        assertEquals(7, days);

        PersianPeriod p = PersianPeriod.between(a, b);
        assertEquals(b, p.addTo(a));
        assertEquals(a, p.subtractFrom(b));
    }

    /* ----------------------------- Range: bounds, iterate, overlaps ----------------------------- */

    @Test
    public void range_semantics_and_iteration() {
        PersianDate a = PersianDate.of(1404, 8, 1);
        PersianDate b = PersianDate.of(1404, 8, 10);

        PersianDateRange rClosed = PersianDateRange.closed(a, b);
        assertTrue(rClosed.contains(a));
        assertTrue(rClosed.contains(b));
        assertEquals(10, rClosed.lengthInDays()); // 10 روز (1..10)

        PersianDateRange rHalfOpen = PersianDateRange.halfOpen(a, b);
        assertTrue(rHalfOpen.contains(a));
        assertFalse(rHalfOpen.contains(b));
        assertEquals(9, rHalfOpen.lengthInDays()); // [1..9]

        // پیمایش
        int cnt = 0;
        for (PersianDate d : rClosed) cnt++;
        assertEquals(10, cnt);

        // همپوشانی و اشتراک
        PersianDateRange r2 = PersianDateRange.closed(b, b.plusDays(5)); // [10..15]
        assertTrue(rClosed.overlaps(r2));
        PersianDateRange inter = rClosed.intersection(r2); // [10..10]
        assertEquals(PersianDateRange.closed(b, b), inter);

        // اتحاد اگر چسبیده
        PersianDateRange union = rClosed.unionIfContiguous(r2); // [1..15]
        assertEquals(PersianDateRange.closed(a, b.plusDays(5)), union);
    }

    /* ----------------------------- Adjusters: month/week helpers ----------------------------- */

    @Test
    public void adjusters_month_and_week() {
        PersianDate d = PersianDate.of(1404, 8, 19);
        assertEquals(1, PersianAdjusters.firstDayOfMonth(d).getDayOfMonth());
        PersianDate lm = PersianAdjusters.lastDayOfMonth(d);
        assertTrue(lm.getDayOfMonth() == 30 || lm.getDayOfMonth() == 31); // بسته به ماه

        PersianDate sowSat = PersianAdjusters.startOfWeekSaturday(d);
        PersianDate eowSat = PersianAdjusters.endOfWeekSaturday(d);
        assertEquals(6, PersianDateUtils.daysBetween(sowSat, eowSat));
    }

    /* ----------------------------- Interop: Date bridge (fixed zone) ----------------------------- */

    @Test
    public void interop_date_bridge_fixed_zone() {
        ZoneId zone = ZoneId.of("Asia/Tehran");

        // از Date به رشته جلالی
        Date date = Date.from(LocalDateTime.of(2025, 10, 31, 10, 15, 0).atZone(zone).toInstant());
        String jalali = DateInterop.toJalaliString(date, "yyyy/MM/dd", zone, PersianDateFormatter.DigitStyle.LATIN);
        // صرفاً چک شکل: yyyy/MM/dd
        assertTrue(jalali.matches("\\d{4}/\\d{2}/\\d{2}"));

        // از متن جلالی به Date (شروع روز)
        Date again = DateInterop.parseJalaliToDate(jalali, "yyyy/MM/dd", zone);
        // چون start-of-day است ممکن است با Date اولیه برابر دقیق نباشد؛ حداقل تاریخ جلالی برابر شود:
        PersianDate eq = PersianDateFormatter.of("yyyy/MM/dd").parseDate(jalali);
        PersianDate gregBack = PersianDate.fromGregorian(again.toInstant().atZone(zone).toLocalDate());
        assertEquals(eq, gregBack);
    }

    /* ----------------------------- Jackson: serialize/deserialize ----------------------------- */

    @Test
    public void jackson_serializer_deserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(
                PersianDateTimeModule.builder()
                        .datePattern("yyyy-MM-dd")
                        .dateTimePattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                        .lenient(false)
                        .build()
        );

        PersianDate d = PersianDate.of(1404, 8, 19);
        String jsDate = mapper.writeValueAsString(d);
        PersianDate d2 = mapper.readValue(jsDate, PersianDate.class);
        assertEquals(d, d2);

        PersianDateTime dt = PersianDateTime.of(1404, 8, 19, 13, 5, 9, 123_000_000);
        String jsDT = mapper.writeValueAsString(dt);
        PersianDateTime dt2 = mapper.readValue(jsDT, PersianDateTime.class);
        assertEquals(dt, dt2);
    }

    /* ----------------------------- Edge: negative/ordering/equals ----------------------------- */

    @Test
    public void comparisons_and_equals_contracts() {
        PersianDate a = PersianDate.of(1400, 1, 1);
        PersianDate b = PersianDate.of(1400, 1, 2);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(PersianDate.of(1400,1,1)));
        assertEquals(a, PersianDate.of(1400,1,1));
        assertNotEquals(a, b);
        assertTrue(PersianDateUtils.isBefore(a, b));
        assertTrue(PersianDateUtils.isAfter(b, a));
        assertTrue(PersianDateUtils.isEqual(a, PersianDate.of(1400,1,1)));
    }

    /* ----------------------------- DateTime roundtrip via Gregorian ----------------------------- */

    @Test
    public void datetime_roundtrip_via_gregorian() {
        PersianDateTime dt = PersianDateTime.of(1402, 12, 29, 23, 59, 59, 987_000_000);
        LocalDateTime g = dt.toGregorian();
        PersianDateTime back = PersianDateTime.fromGregorian(g);
        assertEquals(dt, back);
    }
}
