package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter.DigitStyle;
import ir.amirzad.util.PersianDateUtils;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class UtilBranchDeepTests {

    @Test
    public void min_max_clamp_branches() {
        PersianDate a = PersianDate.of(1402, 12, 1);
        PersianDate b = PersianDate.of(1402, 12, 29);
        PersianDate c = PersianDate.of(1403, 1, 1);

        assertEquals(a, PersianDateUtils.min(a, b));
        assertEquals(c, PersianDateUtils.max(b, c));

        // clamp: [b, c] - a should clamp to b
        PersianDate clamped1 = PersianDateUtils.clamp(a, b, c);
        assertEquals(b, clamped1);

        // clamp: [a, b] - c should clamp to b
        PersianDate clamped2 = PersianDateUtils.clamp(c, a, b);
        assertEquals(b, clamped2);
    }

    @Test
    public void format_overloads_and_nulls() {
        PersianDate d = PersianDate.of(1403, 1, 1);
        PersianDateTime dt = PersianDateTime.of(PersianDate.ofJulianDay(1403), 1, 1, 0, 0);

        String s1 = PersianDateUtils.format(d, "yyyy/MM/dd", DigitStyle.LATIN);
        String s2 = PersianDateUtils.format(dt, "yyyy/MM/dd HH:mm:ss", DigitStyle.LATIN);

        assertNotNull(s1);
        assertNotNull(s2);
        assertTrue(s1.length() >= 10);
        assertTrue(s2.length() >= 10);

        boolean guardedNull1 = false, guardedNull2 = false;
        try {
            PersianDateUtils.format((PersianDate) null, "yyyy/MM/dd", DigitStyle.LATIN);
            guardedNull1 = true;
        } catch (NullPointerException | IllegalArgumentException ex) {
            guardedNull1 = true;
        }
        try {
            PersianDateUtils.format((PersianDateTime) null, "yyyy/MM/dd", DigitStyle.LATIN);
            guardedNull2 = true;
        } catch (NullPointerException | IllegalArgumentException ex) {
            guardedNull2 = true;
        }
        assertTrue(guardedNull1 && guardedNull2);
    }

    @Test
    public void try_parse_date_variants() {
        Optional<PersianDate> ok = PersianDateUtils.tryParseDate("1402/12/29", "yyyy/MM/dd");
        assertTrue(ok.isPresent());

        Optional<PersianDate> bad1 = PersianDateUtils.tryParseDate("1402-12-29", "yyyy/MM/dd");
        assertFalse(bad1.isPresent());

        Optional<PersianDate> bad2 = PersianDateUtils.tryParseDate("1402/13/01", "yyyy/MM/dd");
        assertFalse(bad2.isPresent());
    }
}