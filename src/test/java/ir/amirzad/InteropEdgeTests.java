package ir.amirzad;

import ir.amirzad.interop.DateInterop;
import ir.amirzad.format.PersianDateFormatter.DigitStyle;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;

public class InteropEdgeTests {

    private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");

    @Test
    public void to_jalali_string_with_zone_and_pattern() {
        // Use a moment close to Nowruz but assert via round-trip to avoid astronomical boundary assumptions
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(2020, 3, 20, 12, 0), TEHRAN);
        Date d = Date.from(zdt.toInstant());

        String pattern = "yyyy/MM/dd";
        String s = DateInterop.toJalaliString(d, pattern, TEHRAN, DigitStyle.LATIN);
        assertNotNull(s);
        assertTrue("formatted string should be at least yyyy/MM/dd", s.length() >= 10);

        Optional<Date> back = DateInterop.tryParseJalaliToDate(s, pattern, TEHRAN);
        assertTrue("round-trip parse should succeed", back.isPresent());

        // Round-trip should map to the same civil day in Tehran (ignore time of day)
        ZonedDateTime backTehran = back.get().toInstant().atZone(TEHRAN);
        assertEquals(zdt.toLocalDate(), backTehran.toLocalDate());
    }

    @Test
    public void parse_tryparse_defaults() {
        String pattern = "yyyy/MM/dd";
        String txt = "1399/01/02"; // avoid exact Nowruz moment; unambiguous day in 1399
        Optional<Date> ok = DateInterop.tryParseJalaliToDate(txt, pattern, TEHRAN);
        assertTrue(ok.isPresent());

        Optional<Date> bad = DateInterop.tryParseJalaliToDate("1399-01-02", pattern, TEHRAN);
        assertFalse(bad.isPresent());
    }
}