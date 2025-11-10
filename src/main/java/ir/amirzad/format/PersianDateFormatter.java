/*
 * Project: jalali-date
 * File: ir/amirzad/format/PersianDateFormatter.java
 * Description: Formatter and parser for PersianDate/PersianDateTime with Persian/Latin digits.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.format;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;

import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Minimal, dependency-free formatter & parser for PersianDate / PersianDateTime.
 *
 * Supported pattern tokens (subset, pragmatic):
 *  Date:
 *   - yyyy  : 4-digit year (astronomical, no year 0)
 *   - yy    : 2-digit year (00..99)
 *   - M, MM : month number (1..12 / zero-padded)
 *   - MMM   : month text (فروردین..اسفند) — also accepts short forms (فرو..اسف)
 *   - d, dd : day of month
 *   - E     : weekday name (شنبه..جمعه) — formatting only (parser ignores/accepts optionally)
 *  Time (for PersianDateTime):
 *   - HH    : 00..23
 *   - mm    : 00..59
 *   - ss    : 00..59
 *   - SSS   : millisecond (000..999)
 *   - nnnnnnnnn : nanoseconds (9 digits) — if present, overrides SSS
 *
 * Literals: anything inside single quotes '...' prints as-is. To escape a single quote, use ''
 * Digits: use DigitStyle to control Persian/Latin numerals in output. Parser auto-detects both.
 */
public final class PersianDateFormatter {

    /* ======================== Digit style ======================== */
    public enum DigitStyle { LATIN, PERSIAN }

    public static final class Numerals {
        private static final char[] FA = {'۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'};
        private static final char[] EN = {'0','1','2','3','4','5','6','7','8','9'};

        public static String toPersianDigits(String s) {
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= '0' && c <= '9') sb.append(FA[c - '0']); else sb.append(c);
            }
            return sb.toString();
        }
        public static String toLatinDigits(String s) {
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                // Persian and Arabic-Indic blocks
                if (c >= '۰' && c <= '۹') sb.append(EN[c - '۰']);
                else if (c >= '٠' && c <= '٩') sb.append(EN[c - '٠']);
                else sb.append(c);
            }
            return sb.toString();
        }
    }

    /* ======================== Month / Weekday names ======================== */
    public static final class FarsiText {
        private static final String[] MONTH_FULL = {
                "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
                "مهر","آبان","آذر","دی","بهمن","اسفند"
        };
        private static final String[] MONTH_SHORT = {
                "فرو","ارد","خرد","تیر","مرد","شهر",
                "مهر","آبا","آذر","دی","بهم","اسف"
        };
        private static final String[] WEEKDAY = {
                "دوشنبه","سه‌شنبه","چهارشنبه","پنج‌شنبه","جمعه","شنبه","یک‌شنبه" // DayOfWeek order MON..SUN
        };
        private static final Map<String,Integer> MONTH_LOOKUP = new HashMap<>();
        static {
            for (int i = 0; i < 12; i++) {
                MONTH_LOOKUP.put(MONTH_FULL[i], i+1);
                MONTH_LOOKUP.put(MONTH_SHORT[i], i+1);
                // Accept Latin translit basics (optional):
                MONTH_LOOKUP.put(MONTH_FULL[i].toLowerCase(Locale.ROOT), i+1);
            }
        }
        public static String monthName(int m) { return MONTH_FULL[m-1]; }
        public static String weekdayName(DayOfWeek dow) { return WEEKDAY[dow.getValue()-1]; }
        public static Integer parseMonthName(String token) { return MONTH_LOOKUP.get(token); }
    }

    /* ======================== Instance fields ======================== */
    private final String pattern;
    private final DigitStyle digits;

    public PersianDateFormatter(String pattern, DigitStyle digits) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.digits = Objects.requireNonNull(digits, "digits");
    }

    public static PersianDateFormatter of(String pattern) {
        return new PersianDateFormatter(pattern, DigitStyle.PERSIAN);
    }

    /* ======================== Formatting ======================== */
    public String format(PersianDate d) {
        String out = applyPattern(d, null);
        return digits == DigitStyle.PERSIAN ? Numerals.toPersianDigits(out) : out;
    }

    public String format(PersianDateTime dt) {
        String out = applyPattern(dt.toDate(), dt);
        return digits == DigitStyle.PERSIAN ? Numerals.toPersianDigits(out) : out;
    }

    private String applyPattern(PersianDate d, PersianDateTime dtOpt) {
        StringBuilder sb = new StringBuilder(pattern.length() + 16);
        for (int i = 0; i < pattern.length();) {
            char ch = pattern.charAt(i);
            if (ch == '\'') { // literal
                int end = pattern.indexOf('\'', i+1);
                if (end == -1) throw new IllegalArgumentException("Unclosed quote in pattern");
                if (end == i+1) { // '' -> single quote
                    sb.append('\'');
                    i = end + 1;
                    continue;
                }
                sb.append(pattern, i+1, end);
                i = end + 1;
                continue;
            }
            int run = tokenRunLength(pattern, i, ch);
            String token = pattern.substring(i, i+run);
            switch (ch) {
                case 'y': sb.append(formatYear(d, run)); break;
                case 'M': sb.append(formatMonth(d, run)); break;
                case 'd': sb.append(pad(d.getDayOfMonth(), run)); break;
                case 'E': sb.append(FarsiText.weekdayName(d.getDayOfWeek())); break;
                case 'H': requireTime(dtOpt, token); sb.append(pad(dtOpt.getHour(), run)); break;
                case 'm': requireTime(dtOpt, token); sb.append(pad(dtOpt.getMinute(), run)); break;
                case 's': requireTime(dtOpt, token); sb.append(pad(dtOpt.getSecond(), run)); break;
                case 'S': requireTime(dtOpt, token); sb.append(pad(dtOpt.getNano()/1_000_000, run)); break;
                case 'n': requireTime(dtOpt, token); sb.append(pad(dtOpt.getNano(), run)); break;
                default: sb.append(token); // treat unknown letters as literals
            }
            i += run;
        }
        return sb.toString();
    }

    private static int tokenRunLength(String p, int i, char ch) {
        int j = i+1;
        while (j < p.length() && p.charAt(j) == ch) j++;
        return j - i;
    }

    private static void requireTime(PersianDateTime dt, String token) {
        if (dt == null)
            throw new IllegalArgumentException("Pattern contains time token '" + token + "' but value is PersianDate");
    }

    private static String pad(long v, int len) {
        String s = Long.toString(v);
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(len);
        for (int i = s.length(); i < len; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }

    private static String formatYear(PersianDate d, int run) {
        int y = d.getYear();
        if (run == 2) {
            int yy = Math.floorMod(y, 100);
            return pad(yy, 2);
        }
        return Integer.toString(y);
    }

    private static String formatMonth(PersianDate d, int run) {
        int m = d.getMonthValue();
        if (run == 3) return FarsiText.monthName(m);
        return pad(m, run >= 2 ? 2 : 1);
    }

    /* ======================== Parsing ======================== */
    public PersianDate parseDate(String text) {
        Objects.requireNonNull(text, "text");
        // Normalize numerals to Latin for parsing
        String s = Numerals.toLatinDigits(text.trim());
        ParserState st = new ParserState(s);

        Integer year = null, month = null, day = null;

        for (int i = 0; i < pattern.length();) {
            char ch = pattern.charAt(i);
            if (ch == '\'') {
                int end = pattern.indexOf('\'', i+1);
                if (end == -1) throw new IllegalArgumentException("Unclosed quote in pattern");
                if (end == i+1) { // '' -> literal '
                    st.expect("\'");
                    i = end + 1; continue;
                }
                String lit = pattern.substring(i+1, end);
                st.expect(lit);
                i = end + 1; continue;
            }
            int run = tokenRunLength(pattern, i, ch);
            switch (ch) {
                case 'y': year = st.readInt(run >= 3 ? 4 : 2); if (run == 2 && year < 100) year = twoDigitYearPivot(year); break;
                case 'M':
                    if (run == 3) month = st.readMonthName();
                    else month = st.readInt(run >= 2 ? 2 : 1);
                    break;
                case 'd': day = st.readInt(run >= 2 ? 2 : 1); break;
                case 'E': st.skipWord(); break; // accept and ignore weekday
                default:
                    // treat as literal sequence of that run
                    st.expect(pattern.substring(i, i+run));
            }
            i += run;
        }
        if (year == null || month == null || day == null)
            throw new IllegalArgumentException("Missing Y/M/D components in input: " + text);
        return PersianDate.of(year, month, day);
    }

    public PersianDateTime parseDateTime(String text) {
        Objects.requireNonNull(text, "text");
        String s = Numerals.toLatinDigits(text.trim());
        ParserState st = new ParserState(s);

        Integer year = null, month = null, day = null;
        int hour = 0, minute = 0, second = 0, nano = 0;

        for (int i = 0; i < pattern.length();) {
            char ch = pattern.charAt(i);
            if (ch == '\'') {
                int end = pattern.indexOf('\'', i+1);
                if (end == -1) throw new IllegalArgumentException("Unclosed quote in pattern");
                if (end == i+1) { st.expect("\'"); i = end + 1; continue; }
                st.expect(pattern.substring(i+1, end));
                i = end + 1; continue;
            }
            int run = tokenRunLength(pattern, i, ch);
            switch (ch) {
                case 'y': year = st.readInt(run >= 3 ? 4 : 2); if (run == 2 && year < 100) year = twoDigitYearPivot(year); break;
                case 'M': if (run == 3) month = st.readMonthName(); else month = st.readInt(run >= 2 ? 2 : 1); break;
                case 'd': day = st.readInt(run >= 2 ? 2 : 1); break;
                case 'H': hour = st.readInt(2); break;
                case 'm': minute = st.readInt(2); break;
                case 's': second = st.readInt(2); break;
                case 'S':
                    int ms = st.readInt(3);
                    nano = ms * 1_000_000; break;
                case 'n': nano = st.readInt(9); break;
                case 'E': st.skipWord(); break;
                default: st.expect(pattern.substring(i, i+run));
            }
            i += run;
        }
        if (year == null || month == null || day == null)
            throw new IllegalArgumentException("Missing Y/M/D in input: " + text);
        return PersianDateTime.of(year, month, day, hour, minute, second, nano);
    }

    private static int twoDigitYearPivot(int yy) {
        // Simple pivot: map 00..68 -> 1400..1468, 69..99 -> 1369..1399 (adjust if you need)
        return (yy <= 68) ? (1400 + yy) : (1300 + yy);
    }

    /* ======================== Parser helpers ======================== */
    static final class ParserState {
        final String s; int pos = 0;
        ParserState(String s) { this.s = s; }
        void expect(String lit) {
            if (!s.startsWith(lit, pos))
                throw error("Expected '" + lit + "'");
            pos += lit.length();
        }
        int readInt(int maxLen) {
            int start = pos; int end = Math.min(s.length(), pos + maxLen);
            while (pos < end && isAsciiDigit(s.charAt(pos))) pos++;
            if (pos == start) throw error("Expected number");
            return Integer.parseInt(s.substring(start, pos));
        }
        int readMonthName() {
            // Try longest match from current pos (up to ~6 chars)
            for (int len = Math.min(6, s.length() - pos); len >= 2; len--) {
                String tok = s.substring(pos, pos + len);
                Integer m = FarsiText.parseMonthName(tok);
                if (m != null) { pos += len; return m; }
            }
            throw error("Expected Persian month name");
        }
        void skipWord() {
            int start = pos;
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (Character.isWhitespace(c) || c == '/' || c == '-' || c == ',' || c == 'T') break;
                pos++;
            }
            if (pos == start) throw error("Expected word");
        }
        private static boolean isAsciiDigit(char c) { return c >= '0' && c <= '9'; }
        private IllegalArgumentException error(String msg) {
            String context = s.substring(Math.max(0,pos-8), Math.min(s.length(), pos+8));
            return new IllegalArgumentException(msg + " at pos " + pos + " around [" + context + "]");
        }
    }
}
