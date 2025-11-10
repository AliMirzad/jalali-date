/*
 * Project: jalali-date
 * File: ir/amirzad/jackson/PersianDateTimeModule.java
 * Description: Jackson module for (de)serializing PersianDate and PersianDateTime.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;

import java.io.IOException;
import java.util.Objects;

/**
 * Jackson module for (de)serializing PersianDate and PersianDateTime.
 *
 * <p>Defaults (overridable):
 * <ul>
 *   <li>Date pattern: {@code yyyy-MM-dd}</li>
 *   <li>DateTime pattern: {@code yyyy-MM-dd'T'HH:mm:ss.SSS}</li>
 *   <li>Digits: LATIN (برای سازگاری JSON)</li>
 *   <li>Parsing: strict (lenient=false) — در صورت نیاز قابل تغییر است.</li>
 * </ul>
 *
 * <p>نمونهٔ استفاده:
 * <pre>{@code
 * ObjectMapper mapper = new ObjectMapper()
 *     .registerModule(PersianDateTimeModule.defaults());
 *
 * // یا با الگوهای سفارشی:
 * mapper.registerModule(
 *     PersianDateTimeModule.builder()
 *         .datePattern("yyyy/MM/dd")
 *         .dateTimePattern("yyyy/MM/dd'T'HH:mm:ss.SSS")
 *         .digitStyle(PersianDateFormatter.DigitStyle.LATIN)
 *         .lenient(true)
 *         .build()
 * );
 * }</pre>
 */
public final class PersianDateTimeModule extends SimpleModule {

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private final PersianDateFormatter dateFormatter;
    private final PersianDateFormatter dateTimeFormatter;
    private final boolean lenient;

    private PersianDateTimeModule(Builder b) {
        super("PersianDateTimeModule", Version.unknownVersion());
        this.dateFormatter = new PersianDateFormatter(
                b.datePattern, b.digitStyle
        );
        this.dateTimeFormatter = new PersianDateFormatter(
                b.dateTimePattern, b.digitStyle
        );
        this.lenient = b.lenient;

        // Register serializers/deserializers
        addSerializer(PersianDate.class, new PDSerializer(dateFormatter));
        addDeserializer(PersianDate.class, new PDDeserializer(dateFormatter, lenient));
        addSerializer(PersianDateTime.class, new PDTSerializer(dateTimeFormatter));
        addDeserializer(PersianDateTime.class, new PDTDeserializer(dateTimeFormatter, lenient));
    }

    /** ماژول پیش‌فرض با پترن‌های استاندارد و LATIN digits. */
    public static PersianDateTimeModule defaults() {
        return builder().build();
    }

    /** سازندهٔ سفارشی (ساده) با رشتهٔ پترن‌ها. */
    public PersianDateTimeModule(String datePattern, String dateTimePattern) {
        this(builder().datePattern(datePattern).dateTimePattern(dateTimePattern));
    }

    /** Builder توصیه‌شده برای پیکربندی کامل. */
    public static Builder builder() {
        return new Builder();
    }

    // --------------------- Builder ---------------------
    public static final class Builder {
        private String datePattern = DEFAULT_DATE_PATTERN;
        private String dateTimePattern = DEFAULT_DATETIME_PATTERN;
        private PersianDateFormatter.DigitStyle digitStyle = PersianDateFormatter.DigitStyle.LATIN;
        private boolean lenient = false;

        public Builder datePattern(String p) {
            this.datePattern = Objects.requireNonNull(p, "datePattern");
            return this;
        }

        public Builder dateTimePattern(String p) {
            this.dateTimePattern = Objects.requireNonNull(p, "dateTimePattern");
            return this;
        }

        public Builder digitStyle(PersianDateFormatter.DigitStyle style) {
            this.digitStyle = Objects.requireNonNull(style, "digitStyle");
            return this;
        }

        /**
         * اگر true: در هنگام parse ورودی‌های خالی یا whitespace را null می‌کند
         * و پیام‌های خطا نرم‌تر می‌شوند. اگر false: خطاها strict پرتاب می‌شوند.
         */
        public Builder lenient(boolean v) {
            this.lenient = v;
            return this;
        }

        public PersianDateTimeModule build() {
            return new PersianDateTimeModule(this);
        }
    }

    // --------------------- Serializers ---------------------
    private static final class PDSerializer extends JsonSerializer<PersianDate> {
        private final PersianDateFormatter fmt;
        private PDSerializer(PersianDateFormatter fmt) { this.fmt = fmt; }

        @Override
        public void serialize(PersianDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            gen.writeString(fmt.format(value));
        }
    }

    private static final class PDTSerializer extends JsonSerializer<PersianDateTime> {
        private final PersianDateFormatter fmt;
        private PDTSerializer(PersianDateFormatter fmt) { this.fmt = fmt; }

        @Override
        public void serialize(PersianDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            gen.writeString(fmt.format(value));
        }
    }

    // --------------------- Deserializers ---------------------
    private static final class PDDeserializer extends JsonDeserializer<PersianDate> {
        private final PersianDateFormatter fmt;
        private final boolean lenient;

        private PDDeserializer(PersianDateFormatter fmt, boolean lenient) {
            this.fmt = fmt; this.lenient = lenient;
        }

        @Override
        public PersianDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken t = p.getCurrentToken();
            if (t == JsonToken.VALUE_STRING) {
                String text = p.getText();
                if (lenient && (text == null || text.trim().isEmpty())) return null;
                try {
                    return fmt.parseDate(text.trim());
                } catch (IllegalArgumentException ex) {
                    throw JsonMappingException.from(p, "Cannot parse PersianDate: " + ex.getMessage(), ex);
                }
            } else if (t == JsonToken.VALUE_NULL && lenient) {
                return null;
            }
            return (PersianDate) ctxt.handleUnexpectedToken(PersianDate.class, p);
        }
    }

    private static final class PDTDeserializer extends JsonDeserializer<PersianDateTime> {
        private final PersianDateFormatter fmt;
        private final boolean lenient;

        private PDTDeserializer(PersianDateFormatter fmt, boolean lenient) {
            this.fmt = fmt; this.lenient = lenient;
        }

        @Override
        public PersianDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken t = p.getCurrentToken();
            if (t == JsonToken.VALUE_STRING) {
                String text = p.getText();
                if (lenient && (text == null || text.trim().isEmpty())) return null;
                try {
                    return fmt.parseDateTime(text.trim());
                } catch (IllegalArgumentException ex) {
                    throw JsonMappingException.from(p, "Cannot parse PersianDateTime: " + ex.getMessage(), ex);
                }
            } else if (t == JsonToken.VALUE_NULL && lenient) {
                return null;
            }
            return (PersianDateTime) ctxt.handleUnexpectedToken(PersianDateTime.class, p);
        }
    }
}
