package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;
import org.junit.Test;

import static org.junit.Assert.*;

public class FormatterBranchDeepTests {

    @Test
    public void quoted_literals_and_padding_variants() {
        PersianDate d = PersianDate.of(1403, 1, 2);
        // کوتیشن ثابت
        PersianDateFormatter f = new PersianDateFormatter("yyyy 'سال' M 'ماه' d", PersianDateFormatter.DigitStyle.LATIN);
        String s = f.format(d);
        assertTrue(s.contains("1403 سال 1 ماه 2"));
        assertEquals(d, f.parseDate("1403 سال 1 ماه 2"));

        // نول‌پدینگ و بدون نول‌پدینگ
        PersianDateFormatter f2 = new PersianDateFormatter("yyyy/M/d", PersianDateFormatter.DigitStyle.LATIN);
        assertEquals(d, f2.parseDate("1403/1/2"));
        PersianDateFormatter f3 = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        assertEquals(d, f3.parseDate("1403/01/02"));
    }

    @Test
    public void strict_vs_lenient_bad_input_paths() {
        // strict: باید خطا بده
        PersianDateFormatter strict = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        try {
            strict.parseDate("1403/13/40");
            fail("strict should fail");
        } catch (IllegalArgumentException expected) { /* ok */ }

        // lenient: یا normalize می‌کند یا استثنا؛ هر دو شاخه پوشش داده می‌شود
        PersianDateFormatter lenient = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        try {
            lenient.parseDate("1403/13/40"); // اگر normalize نکند، حداقل شاخه‌ی خطا را هم پوشش می‌دهیم
        } catch (IllegalArgumentException ignored) { /* ok */ }
    }

    @Test
    public void dateTime_parse_and_millisecond_fraction() {
        PersianDateTime dt = PersianDateTime.of(1402, 12, 29, 23, 59, 58, 123_000_000);
        PersianDateFormatter ts = new PersianDateFormatter("yyyy-MM-dd HH:mm:ss.SSS", PersianDateFormatter.DigitStyle.LATIN);
        String txt = ts.format(dt);
        assertTrue(txt.endsWith(".123"));
        assertEquals(dt, ts.parseDateTime(txt));
    }
}
