package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.util.PersianAdjusters;
import ir.amirzad.interop.DateInterop;
import org.junit.Test;

import java.time.*;
import java.util.Date;

import static org.junit.Assert.*;

public class AdjustersEdgeTests {

    private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");

    private static LocalDate toLocalDateTehran(PersianDate p) {
        Date d = DateInterop.fromJalali(p.getYear(), p.getMonthValue(), p.getDayOfMonth());
        return d.toInstant().atZone(TEHRAN).toLocalDate();
    }

    @Test
    public void month_boundaries_prev_next() {
        // Esfand 1402 length is 29, so use a valid date
        PersianDate s1 = PersianDate.of(1402, 12, 29);
        PersianDate firstOfNext = PersianAdjusters.firstDayOfNextMonth(s1);
        PersianDate lastOfPrev = PersianAdjusters.lastDayOfPreviousMonth(PersianDate.of(1403, 1, 1));

        assertEquals(PersianDate.of(1403, 1, 1), firstOfNext);
        assertEquals(PersianDate.of(1402, 12, 29), lastOfPrev);
    }

    @Test
    public void start_end_of_week_saturday_based() {
        PersianDate any = PersianDate.of(1403, 1, 3); // arbitrary
        PersianDate start = PersianAdjusters.startOfWeek(any, DayOfWeek.SATURDAY);
        PersianDate end = PersianAdjusters.endOfWeek(any, DayOfWeek.SATURDAY);

        // start <= any <= end
        assertTrue(start.compareTo(any) <= 0);
        assertTrue(end.compareTo(any) >= 0);

        // start is SATURDAY, end is FRIDAY (Tehran civil week)
        assertEquals(DayOfWeek.SATURDAY, toLocalDateTehran(start).getDayOfWeek());
        assertEquals(DayOfWeek.FRIDAY, toLocalDateTehran(end).getDayOfWeek());

        // distance must be 6 days
        long days = Duration.between(
                toLocalDateTehran(start).atStartOfDay(TEHRAN).toInstant(),
                toLocalDateTehran(end).atStartOfDay(TEHRAN).toInstant()
        ).toDays();
        assertEquals(6L, days);
    }

    @Test
    public void nth_and_last_in_month() {
        PersianDate base = PersianDate.of(1403, 1, 1);
        PersianDate thirdWed = PersianAdjusters.nthInMonth(base, DayOfWeek.WEDNESDAY, 3);
        PersianDate lastFri  = PersianAdjusters.lastInMonth(base, DayOfWeek.FRIDAY);

        assertNotNull(thirdWed);
        assertNotNull(lastFri);

        // in the same month
        assertEquals(1403, thirdWed.getYear());
        assertEquals(1, thirdWed.getMonthValue());
        assertEquals(1403, lastFri.getYear());
        assertEquals(1, lastFri.getMonthValue());

        // correct day-of-week
        assertEquals(DayOfWeek.WEDNESDAY, toLocalDateTehran(thirdWed).getDayOfWeek());
        assertEquals(DayOfWeek.FRIDAY, toLocalDateTehran(lastFri).getDayOfWeek());

        // "third Wednesday" must fall between 15..21 of the month
        assertTrue(thirdWed.getDayOfMonth() >= 15 && thirdWed.getDayOfMonth() <= 21);
    }
}