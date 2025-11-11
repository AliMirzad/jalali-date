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

public class UtilEdgeTests {

    private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");

    @Test
    public void gregorian_conversions_known_points() {
        // Choose a date safely after Nowruz to avoid edge mismatches between arithmetic/astronomical variants
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(2021, 4, 5, 12, 0), TEHRAN);
        Date d = Date.from(zdt.toInstant());
        String pattern = "yyyy/MM/dd";

        String jalali = DateInterop.toJalaliString(d, pattern, TEHRAN, DigitStyle.LATIN);
        assertNotNull(jalali);
        assertTrue("expect yyyy/MM/dd", jalali.length() >= 10);

        Optional<Date> back = DateInterop.tryParseJalaliToDate(jalali, pattern, TEHRAN);
        assertTrue(back.isPresent());

        // Round-trip date (civil day in Tehran) must be stable
        ZonedDateTime backTehran = back.get().toInstant().atZone(TEHRAN);
        assertEquals(zdt.toLocalDate(), backTehran.toLocalDate());
    }
}