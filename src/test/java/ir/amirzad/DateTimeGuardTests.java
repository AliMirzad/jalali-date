package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class DateTimeGuardTests {

    @Test(expected = IllegalArgumentException.class)
    public void nano_of_day_negative_throws() {
        // construct via of(date,h,m,s,n) -> underflow hour makes negative NOD
        PersianDateTime.of(PersianDate.of(1403,1,1), -1, 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nano_of_day_exceeds_range_throws() {
        PersianDateTime.of(PersianDate.of(1403,1,1), 24, 0, 0, 0);
    }

    @Test
    public void from_gregorian_roundtrip() {
        LocalDateTime g = LocalDateTime.of(2024, 3, 20, 0, 0);
        PersianDateTime pdt = PersianDateTime.fromGregorian(g);
        PersianDateTime back = PersianDateTime.of(pdt.toDate(), pdt.getHour(), pdt.getMinute(), pdt.getSecond(), pdt.getNano());
        assertEquals(pdt, back);
    }
}