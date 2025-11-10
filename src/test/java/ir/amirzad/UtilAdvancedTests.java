package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.util.PersianDateRange;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class UtilAdvancedTests {

    @Test
    public void empty_range_behavior() {
        PersianDate d = PersianDate.of(1402, 10, 10);
        PersianDateRange empty = PersianDateRange.halfOpen(d, d);
        assertEquals(0, empty.lengthInDays());
        assertFalse(empty.contains(d));
        assertFalse(empty.iterator().hasNext());
    }

    @Test
    public void iterator_exhaustion_should_stop_gracefully() {
        PersianDate a = PersianDate.of(1402, 1, 1);
        PersianDate b = PersianDate.of(1402, 1, 3);
        PersianDateRange r = PersianDateRange.closed(a, b);

        Iterator<PersianDate> it = r.iterator();
        assertTrue(it.hasNext());
        it.next(); it.next(); it.next();
        assertFalse(it.hasNext());
    }

    @Test
    public void union_of_self_should_return_same() {
        PersianDate a = PersianDate.of(1402, 2, 1);
        PersianDate b = PersianDate.of(1402, 2, 5);
        PersianDateRange r1 = PersianDateRange.closed(a, b);
        PersianDateRange merged = r1.unionIfContiguous(r1);
        assertEquals(r1, merged);
    }

    @Test
    public void intersection_with_subrange() {
        PersianDateRange full = PersianDateRange.closed(PersianDate.of(1402, 1, 1), PersianDate.of(1402, 1, 10));
        PersianDateRange sub  = PersianDateRange.closed(PersianDate.of(1402, 1, 3), PersianDate.of(1402, 1, 7));
        PersianDateRange inter = full.intersection(sub);
        assertEquals(sub, inter);
    }
}
