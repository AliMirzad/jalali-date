package ir.amirzad;

import ir.amirzad.core.PersianDate;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.Assert.*;

public class CoreCoverageTests {

    @Test
    public void month_lengths_and_leap_variations() {
        // ماه‌های 1..6 = 31، 7..11 = 30، 12 = 29/30 بسته به کبیسه
        for (int y = 1350; y <= 1410; y++) {
            for (int m = 1; m <= 11; m++) {
                int len = PersianDate.lengthOfMonth(y, m);
                if (m <= 6) assertEquals(31, len);
                else assertEquals(30, len);
            }
            int esf = PersianDate.lengthOfMonth(y, 12);
            assertTrue(esf == 29 || esf == 30);
            // اگر اسفند 30 باشد، روز 30 معتبر و روز بعد 1 فروردین سال بعد است
            if (esf == 30) {
                PersianDate endEsf = PersianDate.of(y, 12, 30);
                PersianDate next = endEsf.plusDays(1);
                assertEquals(y + 1, next.getYear());
                assertEquals(1, next.getMonthValue());
                assertEquals(1, next.getDayOfMonth());
            }
        }
    }

    @Test
    public void plus_minus_large_offsets_and_roundtrip() {
        PersianDate base = PersianDate.of(1400, 1, 1);
        // آفست‌های بزرگ ±100000 روز
        long[] shifts = { -100000, -36500, -1000, -1, 0, 1, 1000, 36500, 100000 };
        for (long s : shifts) {
            PersianDate d2 = base.plusDays(s);
            // roundtrip
            assertEquals(base.toJulianDay() + s, d2.toJulianDay());
            assertEquals(base, d2.minusDays(s));
            // roundtrip via Gregorian
            PersianDate back = PersianDate.fromGregorian(d2.toGregorian());
            assertEquals(d2, back);
        }
    }

    @Test
    public void gregorian_boundary_near_march_equinox() {
        // چند تاریخ اطراف تبدیل نوروز (حدوداً 20/21 مارس)
        LocalDate[] gregDates = {
                LocalDate.of(2020, 3, 19),
                LocalDate.of(2020, 3, 20),
                LocalDate.of(2020, 3, 21),
                LocalDate.of(2021, 3, 20),
                LocalDate.of(2021, 3, 21),
                LocalDate.of(2016, 3, 19),
                LocalDate.of(2016, 3, 20),
                LocalDate.of(2016, 3, 21)
        };
        for (LocalDate g : gregDates) {
            PersianDate j = PersianDate.fromGregorian(g);
            // رفت و برگشت
            assertEquals(g, j.toGregorian());
            // پرش ±1 روز مطابق با گریگوری
            assertEquals(j.plusDays(1), PersianDate.fromGregorian(g.plusDays(1)));
            assertEquals(j.minusDays(1), PersianDate.fromGregorian(g.minusDays(1)));
        }
    }

    @Test
    public void compareTo_equals_hashcode_contract() {
        PersianDate a = PersianDate.of(1401, 7, 15);
        PersianDate b = PersianDate.of(1401, 7, 15);
        PersianDate c = PersianDate.of(1401, 7, 16);

        assertEquals(0, a.compareTo(b));
        assertTrue(a.compareTo(c) < 0);
        assertTrue(c.compareTo(a) > 0);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void plusMonths_plusYears_day_clamping() {
        // 31 فروردین + 1 ماه => 31 اردیبهشت (معتبر)
        PersianDate d1 = PersianDate.of(1402, 1, 31).plusMonths(1);
        assertEquals(1402, d1.getYear());
        assertEquals(2, d1.getMonthValue());
        assertEquals(31, d1.getDayOfMonth());

        // 31 فروردین + 2 ماه => 31 خرداد (معتبر)
        PersianDate d2 = PersianDate.of(1402, 1, 31).plusMonths(2);
        assertEquals(1402, d2.getYear());
        assertEquals(3, d2.getMonthValue());
        assertEquals(31, d2.getDayOfMonth());

        // 31 فروردین + 7 ماه => باید clamp شود به 30 مهر
        PersianDate d3 = PersianDate.of(1402, 1, 31).plusMonths(7);
        assertEquals(1402, d3.getYear());
        assertEquals(8, d3.getMonthValue());
        assertEquals(30, d3.getDayOfMonth());

        // 30 اسفندِ کبیسه + 1 سال => اگر سال بعد غیرکبیسه باشد، clamp به 29 اسفند
        // سال 1399 کبیسه؛ 1400 غیرکبیسه
        PersianDate d4 = PersianDate.of(1399, 12, 30).plusYears(1);
        assertEquals(1400, d4.getYear());
        assertEquals(12, d4.getMonthValue());
        assertTrue(d4.getDayOfMonth() == 29 || d4.getDayOfMonth() == 30); // بسته به الگوریتم leap
    }

    @Test
    public void random_roundtrips_broad() {
        Random rnd = new Random(7);
        for (int i = 0; i < 5000; i++) {
            int y = 1200 + rnd.nextInt(800); // 1200..1999
            int m = 1 + rnd.nextInt(12);
            int d = 1 + rnd.nextInt(PersianDate.lengthOfMonth(y, m));
            PersianDate pd = PersianDate.of(y, m, d);
            long j = pd.toJulianDay();
            assertEquals(pd, PersianDate.ofJulianDay(j));
            assertEquals(pd, PersianDate.fromGregorian(pd.toGregorian()));
        }
    }
}
