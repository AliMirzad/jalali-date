//package ir.amirzad;
//
//import ir.amirzad.core.PersianDate;
//import ir.amirzad.core.PersianDateTime;
//import ir.amirzad.interop.PersianInterop;
//import org.junit.Test;
//
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//
//import static org.junit.Assert.*;
//
//public class InteropFinalTests {
//
//    @Test
//    public void with_and_without_timezone() {
//        PersianDate pd = PersianDate.of(1402, 11, 30);
//        LocalDate gd = PersianInterop.toGregorian(pd);
//
//        ZonedDateTime zdt = gd.atStartOfDay(ZoneId.of("Asia/Tehran"));
//        PersianDateTime back = PersianInterop.fromZoned(zdt);
//        assertEquals(pd.getYear(), back.getYear());
//    }
//
//    @Test
//    public void invalid_input_returns_null_or_exception() {
//        try {
//            PersianInterop.fromZoned(null);
//            // اگر null برگردونه، خطا نمی‌دیم
//        } catch (Exception ignored) {
//            // شاخه‌ی catch
//        }
//    }
//}
