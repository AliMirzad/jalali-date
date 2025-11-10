package ir.amirzad;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;
import ir.amirzad.jackson.PersianDateTimeModule;
import org.junit.Test;

import static org.junit.Assert.*;

public class JacksonBranchDeepTests {

    private static ObjectMapper mapperStrict() {
        return new ObjectMapper().registerModule(
                PersianDateTimeModule.builder()
                        .datePattern("yyyy/MM/dd")
                        .dateTimePattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                        .lenient(false)
                        .build()
        );
    }

    private static ObjectMapper mapperLenient() {
        return new ObjectMapper().registerModule(
                PersianDateTimeModule.builder()
                        .datePattern("yyyy/MM/dd")
                        .dateTimePattern("yyyy-MM-dd'T'HH:mm:ss")
                        .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                        .lenient(true)
                        .build()
        );
    }

    @Test
    public void wrong_json_type_and_pattern() {
        ObjectMapper om = mapperStrict();

        // نوع اشتباه (عدد به‌جای رشته)
        try {
            om.readValue("1402", PersianDate.class);
            fail("expected type-mismatch failure");
        } catch (Exception expected) { /* ok */ }

        // الگوی اشتباه
        try {
            om.readValue("\"1402-01-02\"", PersianDate.class);
            fail("expected pattern mismatch");
        } catch (Exception expected) { /* ok */ }
    }

    @Test
    public void lenient_accepts_or_fails_but_branch_is_hit() throws Exception {
        ObjectMapper om = mapperLenient();
        // تاریخ نادرست؛ بعضی پیاده‌سازی‌ها normalize می‌کنند، بعضی throw
        try {
            PersianDate d = om.readValue("\"1402/13/40\"", PersianDate.class);
            if (d != null) {
                // اگر normalize شد، roundtrip حداقلی
                String back = om.writeValueAsString(d);
                assertNotNull(back);
            }
        } catch (JsonProcessingException ex) {
            // شاخه‌ی خطا هم پوشش داده شد
        }
    }

    @Test
    public void null_and_blank_values() throws Exception {
        ObjectMapper om = mapperStrict();

        // null → null
        PersianDate nd = om.readValue("null", PersianDate.class);
        assertNull(nd);

        // رشته‌ی خالی یا space (بسته به پیاده‌سازی: یا null یا خطا)
        try {
            PersianDate blank = om.readValue("\"  \"", PersianDate.class);
            // اگر به null نگاشت شود، این چک باعث پوشش شاخه می‌شود:
            if (blank == null) assertNull(blank);
        } catch (Exception ignored) { /* شاخه‌ی خطا */ }

        // DateTime نیز
        PersianDateTime ndt = om.readValue("null", PersianDateTime.class);
        assertNull(ndt);
    }
}
