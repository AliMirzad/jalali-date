package ir.amirzad;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.jackson.PersianDateTimeModule;
import org.junit.Test;

import static org.junit.Assert.*;

public class JacksonErrorTestsExpanded {

    @Test
    public void deserialize_invalid_strings_should_fail() {
        ObjectMapper mapper = new ObjectMapper();

        String[] bads = {
                "\"1402/13/01 00:00:00\"",
                "\"1402/12/32 00:00:00\"",
                "\"not-a-date\""
        };

        for (String s : bads) {
            boolean failed = false;
            try {
                mapper.readValue(s, PersianDateTime.class);
            } catch (Exception ex) {
                failed = true;
            }
            assertTrue("expected failure for " + s, failed);
        }
    }

    @Test
    public void deserialize_persian_date_invalid_should_fail() {
        ObjectMapper mapper = new ObjectMapper();

        boolean failed = false;
        try {
            mapper.readValue("\"1402/13/01\"", PersianDate.class);
        } catch (JsonProcessingException ex) {
            failed = true;
        } catch (Exception ex) {
            failed = true;
        }
        assertTrue(failed);
    }
}