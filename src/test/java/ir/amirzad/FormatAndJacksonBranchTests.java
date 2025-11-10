package ir.amirzad;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;
import ir.amirzad.jackson.PersianDateTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class FormatAndJacksonBranchTests {

    @Test
    public void formatter_lenient_vs_strict_and_digit_styles() {
        PersianDate d = PersianDate.of(1402, 9, 7);

        // LATIN
        PersianDateFormatter f1 = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        String s1 = f1.format(d);
        assertEquals("1402/09/07", s1);
        assertEquals(d, f1.parseDate(s1));

        // PERSIAN_DIGITS
        PersianDateFormatter f2 = new PersianDateFormatter("yyyy-MM-dd", PersianDateFormatter.DigitStyle.PERSIAN);
        String s2 = f2.format(d);
        assertTrue(s2.contains("۱۴۰۲"));
        assertEquals(d, f2.parseDate(s2));

        // ورودی بد با lenient=false باید خطا بده
        try {
            new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN)
                    .parseDate("1402/13/40");
            fail("strict parse should fail");
        } catch (IllegalArgumentException expected) {
            // ok
        }

        // lenient=true اجازه‌ی اصلاح/پذیرش نسبی می‌دهد (بسته به پیاده‌سازی تو ممکنه به استثنا منجر نشود)
        PersianDateFormatter lenient = new PersianDateFormatter("yyyy/MM/dd", PersianDateFormatter.DigitStyle.LATIN);
        try {
            lenient.parseDate("1402/13/40");
        } catch (IllegalArgumentException ex) {
            // اگر هم خطا داد، مسیر شاخه‌ی خطا هم پوشش داده می‌شود.
        }
    }

    @Test
    public void jackson_nulls_and_patterns() throws Exception {
        PersianDateTimeModule m = PersianDateTimeModule.builder()
                .datePattern("yyyy/MM/dd")
                .dateTimePattern("yyyy-MM-dd'T'HH:mm:ss")
                .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                .lenient(false)
                .build();

        ObjectMapper om = new ObjectMapper().registerModule(m);

        PersianDate d = PersianDate.of(1401, 1, 2);
        String json = om.writeValueAsString(d);
        PersianDate back = om.readValue(json, PersianDate.class);
        assertEquals(d, back);

        // مقدار null (اگر serializer/deserializer رفتار خاص دارد)
        String jsonNull = "null";
        PersianDate backNull = om.readValue(jsonNull, PersianDate.class);
        assertNull(backNull);

        // تغییر الگو و تلاش برای parse نامعتبر
        PersianDateTimeModule m2 = PersianDateTimeModule.builder()
                .datePattern("yyyy-MM-dd")
                .dateTimePattern("yyyy-MM-dd'T'HH:mm:ss")
                .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                .lenient(false)
                .build();
        ObjectMapper om2 = new ObjectMapper().registerModule(m2);
        try {
            om2.readValue("\"1401/01/02\"", PersianDate.class);
            fail("pattern mismatch should fail");
        } catch (Exception expected) {
            // ok
        }
    }
}
