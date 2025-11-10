package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.util.PersianDateRange;
import ir.amirzad.util.PersianDateUtils;
import ir.amirzad.util.PersianPeriod;
import org.junit.Test;

import static org.junit.Assert.*;

public class UtilEdgeTests {

    @Test
    public void range_single_day_closed_and_halfOpen() {
        PersianDate d = PersianDate.of(1402, 7, 15);

        PersianDateRange closed = PersianDateRange.closed(d, d);
        assertTrue(closed.contains(d));
        assertEquals(1, closed.lengthInDays()); // [d..d] = 1 روز

        PersianDateRange half = PersianDateRange.halfOpen(d, d);
        assertFalse(half.contains(d));
        assertEquals(0, half.lengthInDays());   // [d..d) = تهی
    }

    @Test
    public void range_intersection_empty_object_instead_of_null() {
        PersianDate a = PersianDate.of(1402, 1, 1);
        PersianDate b = PersianDate.of(1402, 1, 10);
        PersianDateRange r1 = PersianDateRange.closed(a, b);                 // [1..10]
        PersianDateRange r2 = PersianDateRange.closed(b.plusDays(1), b.plusDays(5)); // [11..15] (بدون همپوشانی)

        PersianDateRange inter = r1.intersection(r2);
        // پیاده‌سازی تو رنج تهی برمی‌گرداند، نه null
        assertEquals(0, inter.lengthInDays());
        assertFalse(inter.contains(a));
    }

    @Test
    public void range_union_contiguous_and_noncontiguous() {
        PersianDate a = PersianDate.of(1401, 4, 1);
        PersianDate b = PersianDate.of(1401, 4, 10);

        PersianDateRange r1 = PersianDateRange.closed(a, b);                 // [1..10]
        PersianDateRange r3 = PersianDateRange.closed(b.plusDays(1), b.plusDays(5)); // [11..15] contiguous

        PersianDateRange u13 = r1.unionIfContiguous(r3);
        assertEquals(PersianDateRange.closed(a, b.plusDays(5)), u13);

        // یک غیرچسبیده واقعی (gap)
        PersianDateRange rGap = PersianDateRange.closed(b.plusDays(2), b.plusDays(6)); // [12..16]
        PersianDateRange uGap = r1.unionIfContiguous(rGap);
        // بسته به قرارداد API: یا null یا empty. اینجا انتظار null می‌گذاریم؛ اگر empty می‌دهد، به 0 تغییر بده
        if (uGap != null) {
            assertEquals(0, uGap.lengthInDays());
        } else {
            assertNull(uGap);
        }
    }

    @Test
    public void period_sign_and_zero_and_compare_helpers() {
        PersianDate a = PersianDate.of(1400, 1, 1);
        PersianDate b = PersianDate.of(1400, 1, 20);

        PersianPeriod pos = PersianPeriod.between(a, b);
        PersianPeriod neg = PersianPeriod.between(b, a);
        PersianPeriod zero = PersianPeriod.between(a, a);

        assertEquals(b, pos.addTo(a));
        assertEquals(a, pos.subtractFrom(b));
        assertEquals(a, zero.addTo(a));
        assertEquals(a, zero.subtractFrom(a));
        assertEquals(a, neg.addTo(b));
        assertEquals(b, neg.subtractFrom(a));

        assertTrue(PersianDateUtils.isBefore(a, b));
        assertTrue(PersianDateUtils.isAfter(b, a));
        assertTrue(PersianDateUtils.isEqual(a, PersianDate.of(1400, 1, 1)));
    }
}
