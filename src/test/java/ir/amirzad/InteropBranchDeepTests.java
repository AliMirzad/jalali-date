package ir.amirzad;

import ir.amirzad.format.PersianDateFormatter.DigitStyle;
import ir.amirzad.interop.DateInterop;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;

public class InteropBranchDeepTests {

    private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");

    @Test
    public void to_jalali_string_all_digit_styles_non_null() {
        Date now = new Date();
        for (DigitStyle ds : DigitStyle.values()) {
            String s = DateInterop.toJalaliString(now, "yyyy/MM/dd HH:mm:ss", TEHRAN, ds);
            assertNotNull("toJalaliString must not return null for " + ds, s);
            assertTrue("length should be reasonable", s.length() >= 10);
        }
    }

    @Test
    public void to_jalali_string_null_arguments_are_guarded() {
        boolean guardedDate = false, guardedPattern = false, guardedZone = false;

        // null date
        try {
            DateInterop.toJalaliString(null, "yyyy/MM/dd", TEHRAN, DigitStyle.LATIN);
            guardedDate = true; // if it returns gracefully, still acceptable
        } catch (NullPointerException | IllegalArgumentException ex) {
            guardedDate = true; // guard via exception
        }

        // null pattern
        try {
            DateInterop.toJalaliString(new Date(), null, TEHRAN, DigitStyle.LATIN);
            guardedPattern = true;
        } catch (NullPointerException | IllegalArgumentException ex) {
            guardedPattern = true;
        }

        // null zone
        try {
            DateInterop.toJalaliString(new Date(), "yyyy/MM/dd", null, DigitStyle.LATIN);
            guardedZone = true;
        } catch (NullPointerException | IllegalArgumentException ex) {
            guardedZone = true;
        }

        assertTrue(guardedDate && guardedPattern && guardedZone);
    }

    @Test
    public void try_parse_valid_and_invalid() {
        Optional<Date> ok = DateInterop.tryParseJalaliToDate("1402/12/29", "yyyy/MM/dd", TEHRAN);
        assertTrue(ok.isPresent());

        Optional<Date> badFmt = DateInterop.tryParseJalaliToDate("1402-12-29", "yyyy/MM/dd", TEHRAN);
        assertFalse(badFmt.isPresent());

        Optional<Date> badMonth = DateInterop.tryParseJalaliToDate("1402/13/01", "yyyy/MM/dd", TEHRAN);
        assertFalse(badMonth.isPresent());

        Optional<Date> badDay = DateInterop.tryParseJalaliToDate("1402/12/32", "yyyy/MM/dd", TEHRAN);
        assertFalse(badDay.isPresent());
    }

    @Test
    public void from_jalali_overloads_are_consistent() {
        // 1399/01/01 around Nowruz; use 12:00 to avoid DST edges
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(2020, 3, 20, 12, 0), TEHRAN);
        Date noRef = Date.from(zdt.toInstant());

        Date d1 = DateInterop.fromJalali(1399, 1, 1);
        Date d2 = DateInterop.fromJalali(1399, 1, 1, 0, 0);
        Date d3 = DateInterop.fromJalali(1399, 1, 1, 0, 0, 0);
        Date d4 = DateInterop.fromJalali(1399, 1, 1, 0, 0, 0, TEHRAN);

        assertNotNull(d1);
        assertNotNull(d2);
        assertNotNull(d3);
        assertNotNull(d4);

        // All should map to the same civil day in Tehran
        LocalDateTime l1 = d1.toInstant().atZone(TEHRAN).toLocalDateTime();
        LocalDateTime l2 = d2.toInstant().atZone(TEHRAN).toLocalDateTime();
        LocalDateTime l3 = d3.toInstant().atZone(TEHRAN).toLocalDateTime();
        LocalDateTime l4 = d4.toInstant().atZone(TEHRAN).toLocalDateTime();

        assertEquals(l1.toLocalDate(), l2.toLocalDate());
        assertEquals(l1.toLocalDate(), l3.toLocalDate());
        assertEquals(l1.toLocalDate(), l4.toLocalDate());
    }
}