package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.util.PersianDateRange;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class UtilBranchDeepTests {

    @Test
    public void halfOpen_empty_and_iterator_noSuchElement() {
        PersianDate d = PersianDate.of(1402, 10, 10);
        PersianDateRange empty = PersianDateRange.halfOpen(d, d); // تهی

        assertEquals(0, empty.lengthInDays());
        assertFalse(empty.contains(d));

        Iterator<PersianDate> it = empty.iterator();
        assertFalse(it.hasNext());
        boolean threw = false;
        try {
            it.next(); // باید NoSuchElementException بده
        } catch (NoSuchElementException ex) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void closed_singleton_and_adjacent_union_and_intersection() {
        PersianDate a = PersianDate.of(1402, 2, 1);
        PersianDate b = PersianDate.of(1402, 2, 1);

        // singleton
        PersianDateRange one = PersianDateRange.closed(a, b);
        assertEquals(1, one.lengthInDays());
        assertTrue(one.contains(a));

        // رنج بعدی که «چسبیده» است
        PersianDateRange next = PersianDateRange.closed(b.plusDays(1), b.plusDays(3));
        PersianDateRange u = one.unionIfContiguous(next); // باید merge کند
        assertEquals(PersianDateRange.closed(a, b.plusDays(3)), u);

        // intersection تهی با یک گپ واقعی
        PersianDateRange far = PersianDateRange.closed(b.plusDays(5), b.plusDays(7));
        PersianDateRange inter = one.intersection(far);
        assertEquals(0, inter.lengthInDays()); // این پیاده‌سازی null نمی‌دهد، رنج تهی می‌دهد
    }

    @Test
    public void overlap_full_sub_super() {
        PersianDateRange full = PersianDateRange.closed(PersianDate.of(1401,1,1), PersianDate.of(1401,1,10));
        PersianDateRange sub  = PersianDateRange.closed(PersianDate.of(1401,1,3), PersianDate.of(1401,1,7));
        PersianDateRange superR = PersianDateRange.closed(PersianDate.of(1400,12,29), PersianDate.of(1401,1,12));

        // اشتراک‌ها
        assertEquals(sub, full.intersection(sub));
        assertEquals(full, full.intersection(superR));
        assertEquals(sub, superR.intersection(sub));

        // یونین‌های همپوشان
        assertEquals(superR, full.unionIfContiguous(superR));
        assertEquals(superR, superR.unionIfContiguous(full));
    }

    @Test
    public void spliterator_tryAdvance_and_estimateSize_paths() {
        PersianDate start = PersianDate.of(1401, 3, 28);
        PersianDate end   = PersianDate.of(1401, 4, 2); // 5 روز
        PersianDateRange r = PersianDateRange.closed(start, end);

        Spliterator<PersianDate> sp = r.spliterator();
        AtomicInteger cnt = new AtomicInteger();
        while (sp.tryAdvance(x -> cnt.incrementAndGet())) { /* consume */ }

        assertEquals(r.lengthInDays(), cnt.get());
        // ensure subsequent tryAdvance is false
        assertFalse(sp.tryAdvance(x -> {}));
    }
}
