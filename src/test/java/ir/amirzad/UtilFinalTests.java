//package ir.amirzad;
//
//import ir.amirzad.core.PersianDate;
//import ir.amirzad.util.PersianDateRange;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class UtilFinalTests {
//
//    @Test
//    public void equals_hash_compare_and_nulls() {
//        PersianDate a = PersianDate.of(1402, 5, 1);
//        PersianDate b = PersianDate.of(1402, 5, 5);
//
//        PersianDateRange r1 = PersianDateRange.closed(a, b);
//        PersianDateRange r2 = PersianDateRange.closed(a, b);
//        PersianDateRange r3 = PersianDateRange.closed(a, a.plusDays(1));
//
//        assertEquals(r1, r2);
//        assertEquals(r1.hashCode(), r2.hashCode());
//        assertNotEquals(r1, r3);
//        assertNotEquals(r1, null);
//        assertNotEquals(r1, "string");
//
//        assertTrue(r1.compareTo(r3) > 0 || r1.compareTo(r3) < 0);
//        assertFalse(r1.contains(null));
//    }
//}
