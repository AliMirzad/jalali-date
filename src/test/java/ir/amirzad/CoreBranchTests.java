package ir.amirzad;

import ir.amirzad.core.PersianDate;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreBranchTests {

    @Test(expected = IllegalArgumentException.class)
    public void invalid_month_low() {
        PersianDate.of(1402, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_month_high() {
        PersianDate.of(1402, 13, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_day_low() {
        PersianDate.of(1402, 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_day_high_in_31_month() {
        PersianDate.of(1402, 1, 32);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_day_in_short_month() {
        // ماه 8 = آبان (۳۰ روز), روز 31 غیرمعتبر
        PersianDate.of(1402, 8, 31);
    }

    @Test
    public void leap_last_day_and_next_day() {
        // اسفند 30 در سال کبیسه
        // اگر الگوریتمت 1399 را کبیسه می‌شناسد:
        PersianDate d = PersianDate.of(1399, 12, 30);
        PersianDate next = d.plusDays(1);
        assertEquals(1400, next.getYear());
        assertEquals(1, next.getMonthValue());
        assertEquals(1, next.getDayOfMonth());
    }

    @Test
    public void clamp_in_plusMonths_and_minusMonths() {
        // 31 فروردین → +7 ماه = 30 آبان (clamp)
        PersianDate a = PersianDate.of(1402, 1, 31).plusMonths(7);
        assertEquals(1402, a.getYear());
        assertEquals(8, a.getMonthValue());
        assertEquals(30, a.getDayOfMonth());

        // 30 آبان → -7 ماه = 30 فروردین (clamp معکوس)
        PersianDate b = PersianDate.of(1402, 8, 30).minusMonths(7);
        assertEquals(1402, b.getYear());
        assertEquals(1, b.getMonthValue());
        assertEquals(30, b.getDayOfMonth());
    }

    @Test
    public void equals_null_and_different_type() {
        PersianDate d = PersianDate.of(1402, 5, 10);
        assertFalse(d.equals(null));
        assertFalse(d.equals("1402-05-10"));
        assertTrue(d.equals(PersianDate.of(1402, 5, 10)));
    }
}
