package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.util.PersianDateRange;
import ir.amirzad.util.PersianDateUtils;
import ir.amirzad.util.PersianPeriod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class UtilCoverageTests {

    /* ---------- PersianDateUtils ---------- */

    @Test
    public void utils_daysBetween_sign_and_zero() {
        PersianDate a = PersianDate.of(1402, 1, 1);
        PersianDate b = PersianDate.of(1402, 1, 10);
        assertEquals(9, PersianDateUtils.daysBetween(a, b));
        assertEquals(-9, PersianDateUtils.daysBetween(b, a));
        assertEquals(0, PersianDateUtils.daysBetween(a, a));
    }

    @Test
    public void utils_compare_helpers() {
        PersianDate d = PersianDate.of(1403, 5, 20);
        assertTrue(PersianDateUtils.isEqual(d, PersianDate.of(1403, 5, 20)));
        assertTrue(PersianDateUtils.isBefore(PersianDate.of(1403, 5, 19), d));
        assertTrue(PersianDateUtils.isAfter(PersianDate.of(1403, 5, 21), d));
    }

    @Test
    public void utils_epoch_and_julian_roundtrip() {
        PersianDate d = PersianDate.of(1404, 8, 19);
        long epochDay = PersianDateUtils.toEpochDay(d);
        PersianDate backE = PersianDateUtils.fromEpochDay(epochDay);
        assertEquals(d, backE);

        long jdn = PersianDateUtils.toJulianDay(d);
        PersianDate backJ = PersianDateUtils.fromJulianDay(jdn);
        assertEquals(d, backJ);
    }

    /* ---------- PersianPeriod ---------- */

    @Test
    public void period_positive_zero_negative_and_add_subtract() {
        PersianDate a = PersianDate.of(1400, 2, 15);
        PersianDate b = PersianDate.of(1402, 6, 10);

        PersianPeriod pos = PersianPeriod.between(a, b);
        assertEquals(b, pos.addTo(a));
        assertEquals(a, pos.subtractFrom(b));

        PersianPeriod zero = PersianPeriod.between(a, a);
        assertEquals(a, zero.addTo(a));
        assertEquals(a, zero.subtractFrom(a));

        PersianPeriod neg = PersianPeriod.between(b, a); // منفی
        assertEquals(a, neg.addTo(b));
        assertEquals(b, neg.subtractFrom(a));
    }

    /* ---------- PersianDateRange ---------- */

    @Test
    public void range_closed_and_halfOpen_contains_and_length() {
        PersianDate a = PersianDate.of(1401, 3, 1);
        PersianDate b = PersianDate.of(1401, 3, 10);

        PersianDateRange closed = PersianDateRange.closed(a, b); // [a..b]
        assertTrue(closed.contains(a));
        assertTrue(closed.contains(b));
        assertEquals(10, closed.lengthInDays());

        PersianDateRange half = PersianDateRange.halfOpen(a, b); // [a..b)
        assertTrue(half.contains(a));
        assertFalse(half.contains(b));
        assertEquals(9, half.lengthInDays());
    }

    @Test
    public void range_iteration_and_spliterator() {
        PersianDate start = PersianDate.of(1401, 1, 28);
        PersianDate end   = PersianDate.of(1401, 2, 3);
        PersianDateRange r = PersianDateRange.closed(start, end);

        // Iterator (بازهٔ بسته شامل هر دو سر)
        List<PersianDate> all = new ArrayList<>();
        for (PersianDate d : r) all.add(d);
        assertEquals(7, all.size());            // 28..31 فروردین (4 روز) + 1..3 اردیبهشت (3 روز) = 7
        assertEquals(start, all.get(0));
        assertEquals(end, all.get(6));

        // Spliterator: به‌جای characteristic، شمارش precise آیتم‌ها
        int counted = countWithSpliterator(r.spliterator());
        assertEquals(all.size(), counted);
        assertEquals(r.lengthInDays(), counted);
    }

    private static int countWithSpliterator(Spliterator<PersianDate> sp) {
        final int[] c = {0};
        sp.forEachRemaining(new Consumer<PersianDate>() {
            @Override public void accept(PersianDate ignored) { c[0]++; }
        });
        return c[0];
    }

    @Test
    public void range_overlap_intersection_union_adjacent_and_disjoint() {
        PersianDate a = PersianDate.of(1401, 4, 1);
        PersianDate b = PersianDate.of(1401, 4, 10);

        PersianDateRange r1 = PersianDateRange.closed(a, b);                 // [1..10]
        PersianDateRange r2 = PersianDateRange.closed(b, b.plusDays(5));     // [10..15]  -> overlap در 10
        PersianDateRange r3 = PersianDateRange.closed(b.plusDays(1), b.plusDays(5)); // [11..15] -> contiguous با r1 (چسبیده، بدون همپوشانی)

        // همپوشانی
        assertTrue(r1.overlaps(r2));    // چون روز 10 مشترک است
        assertFalse(r1.overlaps(r3));   // چسبیده ولی همپوشان نیست

        // اشتراک
        PersianDateRange i12 = r1.intersection(r2);
        assertEquals(PersianDateRange.closed(b, b), i12);

        PersianDateRange i13 = r1.intersection(r3);
        // در این پیاده‌سازی، برای «بدون اشتراک»، آبجکتِ رنجِ خالی برمی‌گردد (نه null).
        // پس به جای assertNull، خالی بودن را تست می‌کنیم:
        assertEquals(0, i13.lengthInDays());

        // اتحاد:
        // - اگر همپوشانی داشته باشند => حتماً قابل اتحاد است
        PersianDateRange u12 = r1.unionIfContiguous(r2); // overlap دارد
        assertEquals(PersianDateRange.closed(a, b.plusDays(5)), u12);

        // - اگر فقط چسبیده باشند (contiguous) هم باید merge بشوند
        PersianDateRange u13 = r1.unionIfContiguous(r3); // contiguous (10 و 11)
        assertEquals(PersianDateRange.closed(a, b.plusDays(5)), u13);
    }

    @Test
    public void range_reverse_bounds_should_throw_or_normalize() {
        PersianDate a = PersianDate.of(1401, 5, 10);
        PersianDate b = PersianDate.of(1401, 5, 1);

        boolean threw = false;
        try {
            PersianDateRange.closed(a, b); // بسته به پیاده‌سازی: غالباً IllegalArgumentException
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        assertTrue("closed(start>end) must fail or normalize; current expected: fail", threw);
    }

    @Test
    public void iterator_remove_not_supported() {
        PersianDate a = PersianDate.of(1401, 6, 1);
        PersianDate b = PersianDate.of(1401, 6, 3);
        PersianDateRange r = PersianDateRange.closed(a, b);
        Iterator<PersianDate> it = r.iterator();
        it.next();
        boolean threw = false;
        try {
            it.remove();
        } catch (UnsupportedOperationException ex) {
            threw = true;
        }
        assertTrue(threw);
    }
}
