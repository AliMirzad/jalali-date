package ir.amirzad;

import ir.amirzad.core.PersianDate;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class InteropCoverageTests {

    @Test
    public void to_and_from_gregorian_roundtrip() {
        PersianDate p = PersianDate.of(1402, 8, 25);
        LocalDate g = p.toGregorian();
        PersianDate back = p.fromGregorian(g);
        assertEquals(p, back);
    }
}
