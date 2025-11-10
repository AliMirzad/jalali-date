package ir.amirzad;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.amirzad.core.PersianDate;
import ir.amirzad.format.PersianDateFormatter;
import ir.amirzad.jackson.PersianDateTimeModule;
import org.junit.Test;

import static org.junit.Assert.*;

public class JacksonErrorTests {

    @Test
    public void serialize_deserialize_null_and_wrong_pattern() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(PersianDateTimeModule.builder()
                        .datePattern("yyyy/MM/dd")
                        .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                        .lenient(false)
                        .build());

        PersianDate d = PersianDate.of(1401, 2, 3);
        String json = mapper.writeValueAsString(d);
        assertTrue(json.contains("1401"));
        assertEquals(d, mapper.readValue(json, PersianDate.class));

        // Wrong pattern => error
        ObjectMapper wrong = new ObjectMapper()
                .registerModule(PersianDateTimeModule.builder()
                        .datePattern("MM-dd-yyyy")
                        .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
                        .lenient(false)
                        .build());
        try {
            wrong.readValue("\"1401/02/03\"", PersianDate.class);
            fail("expected failure on wrong pattern");
        } catch (Exception expected) {
            // expected
        }

        // Null handling
        assertNull(mapper.readValue("null", PersianDate.class));
    }
}
