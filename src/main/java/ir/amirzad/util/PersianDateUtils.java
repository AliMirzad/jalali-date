/*
 * Project: jalali-date
 * File: ir/amirzad/util/PersianDateUtils.java
 * Description: Small utility helpers: numeric conversions, comparisons, parsing/formatting shortcuts.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.util;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

/**
 * PersianDateUtils: small static helpers for day-to-day usage.
 * Java 8 compatible. Zero dependencies besides JDK and the jalali core/format modules.
 */
public final class PersianDateUtils {

    private PersianDateUtils() { /* no instances */ }

    /* ===================== Now / Today ===================== */

    /** Today in system default zone. */
    public static PersianDate today() {
        LocalDate g = LocalDate.now();
        return PersianDate.fromGregorian(g);
    }

    /** Today in given zone. */
    public static PersianDate today(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        LocalDate g = LocalDate.now(zone);
        return PersianDate.fromGregorian(g);
    }

    /** Today using a specific Clock (useful for tests). */
    public static PersianDate today(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        LocalDate g = LocalDate.now(clock);
        return PersianDate.fromGregorian(g);
    }

    /** Now (date-time) in system default zone. */
    public static PersianDateTime now() {
        LocalDateTime ldt = LocalDateTime.now();
        return PersianDateTime.fromGregorian(ldt);
    }

    /** Now (date-time) in given zone. */
    public static PersianDateTime now(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        LocalDateTime ldt = LocalDateTime.now(zone);
        return PersianDateTime.fromGregorian(ldt);
    }

    /** Now (date-time) using a specific Clock (useful for tests). */
    public static PersianDateTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        LocalDateTime ldt = LocalDateTime.now(clock);
        return PersianDateTime.fromGregorian(ldt);
    }

    /* ===================== Parsing / Formatting ===================== */

    /** Parse date with pattern; throws IllegalArgumentException on failure. */
    public static PersianDate parseDate(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        PersianDateFormatter f = new PersianDateFormatter(pattern, PersianDateFormatter.DigitStyle.LATIN);
        return f.parseDate(text);
    }

    /** Parse date-time with pattern; throws IllegalArgumentException on failure. */
    public static PersianDateTime parseDateTime(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        PersianDateFormatter f = new PersianDateFormatter(pattern, PersianDateFormatter.DigitStyle.LATIN);
        return f.parseDateTime(text);
    }

    /** Try parse date, returning Optional.empty() on failure. */
    public static Optional<PersianDate> tryParseDate(String text, String pattern) {
        try { return Optional.of(parseDate(text, pattern)); } catch (RuntimeException ex) { return Optional.empty(); }
    }

    /** Try parse date-time, returning Optional.empty() on failure. */
    public static Optional<PersianDateTime> tryParseDateTime(String text, String pattern) {
        try { return Optional.of(parseDateTime(text, pattern)); } catch (RuntimeException ex) { return Optional.empty(); }
    }

    /** Format date using given pattern and digit style. */
    public static String format(PersianDate d, String pattern, PersianDateFormatter.DigitStyle digits) {
        Objects.requireNonNull(d, "d"); Objects.requireNonNull(pattern, "pattern"); Objects.requireNonNull(digits, "digits");
        PersianDateFormatter f = new PersianDateFormatter(pattern, digits);
        return f.format(d);
    }

    /** Format date-time using given pattern and digit style. */
    public static String format(PersianDateTime dt, String pattern, PersianDateFormatter.DigitStyle digits) {
        Objects.requireNonNull(dt, "dt"); Objects.requireNonNull(pattern, "pattern"); Objects.requireNonNull(digits, "digits");
        PersianDateFormatter f = new PersianDateFormatter(pattern, digits);
        return f.format(dt);
    }

    /* ===================== Math / Comparators ===================== */

    /** Minimum of two dates. */
    public static PersianDate min(PersianDate a, PersianDate b) {
        Objects.requireNonNull(a, "a"); Objects.requireNonNull(b, "b");
        return a.compareTo(b) <= 0 ? a : b;
    }

    /** Maximum of two dates. */
    public static PersianDate max(PersianDate a, PersianDate b) {
        Objects.requireNonNull(a, "a"); Objects.requireNonNull(b, "b");
        return a.compareTo(b) >= 0 ? a : b;
    }

    /** Clamp date to [lo, hi]. If date < lo returns lo; if date > hi returns hi; else returns date. */
    public static PersianDate clamp(PersianDate date, PersianDate lo, PersianDate hi) {
        Objects.requireNonNull(date, "date"); Objects.requireNonNull(lo, "lo"); Objects.requireNonNull(hi, "hi");
        if (lo.compareTo(hi) > 0) throw new IllegalArgumentException("lo must be <= hi");
        if (date.compareTo(lo) < 0) return lo;
        if (date.compareTo(hi) > 0) return hi;
        return date;
    }

    /* ===================== Bridges (explicit helpers) ===================== */

    /** Convert PersianDate to Gregorian LocalDate (explicit util). Same as d.toGregorian(). */
    public static LocalDate toGregorian(PersianDate d) {
        Objects.requireNonNull(d, "d");
        return d.toGregorian();
    }

    /** Convert Gregorian LocalDate to PersianDate (explicit util). */
    public static PersianDate fromGregorian(LocalDate g) {
        Objects.requireNonNull(g, "g");
        return PersianDate.fromGregorian(g);
    }

    /** Convert PersianDateTime to Gregorian LocalDateTime (explicit util). */
    public static LocalDateTime toGregorian(PersianDateTime dt) {
        Objects.requireNonNull(dt, "dt");
        return dt.toGregorian();
    }

    /** Convert Gregorian LocalDateTime to PersianDateTime (explicit util). */
    public static PersianDateTime fromGregorian(LocalDateTime ldt) {
        Objects.requireNonNull(ldt, "ldt");
        return PersianDateTime.fromGregorian(ldt);
    }

    /* ===================== Numeric conversions ===================== */

    /** Converts a PersianDate to Julian Day Number (JDN). */
    public static long toJulianDay(PersianDate date) {
        Objects.requireNonNull(date, "date");
        return date.toJulianDay();
    }

    /** Converts a Julian Day Number to PersianDate. */
    public static PersianDate fromJulianDay(long jdn) {
        return PersianDate.ofJulianDay(jdn);
    }

    /** Converts a PersianDate to epoch day (days since 1970-01-01 Gregorian). */
    public static long toEpochDay(PersianDate date) {
        Objects.requireNonNull(date, "date");
        return date.toGregorian().toEpochDay();
    }

    /** Creates a PersianDate from epoch day (days since 1970-01-01 Gregorian). */
    public static PersianDate fromEpochDay(long epochDay) {
        LocalDate g = LocalDate.ofEpochDay(epochDay);
        return PersianDate.fromGregorian(g);
    }

    /* ===================== Date difference ===================== */

    /** Number of days from {@code a} to {@code b}. (b - a) */
    public static long daysBetween(PersianDate a, PersianDate b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        return b.toJulianDay() - a.toJulianDay();
    }

    /** Returns the PersianPeriod difference (year/month/day) between a and b. */
    public static PersianPeriod periodBetween(PersianDate a, PersianDate b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        return PersianPeriod.between(a, b);
    }

    /* ===================== Comparison helpers ===================== */

    public static boolean isAfter(PersianDate a, PersianDate b) {
        return a.compareTo(b) > 0;
    }

    public static boolean isBefore(PersianDate a, PersianDate b) {
        return a.compareTo(b) < 0;
    }

    public static boolean isEqual(PersianDate a, PersianDate b) {
        return a.compareTo(b) == 0;
    }

    public static boolean isAfter(PersianDateTime a, PersianDateTime b) {
        return a.compareTo(b) > 0;
    }

    public static boolean isBefore(PersianDateTime a, PersianDateTime b) {
        return a.compareTo(b) < 0;
    }

    public static boolean isEqual(PersianDateTime a, PersianDateTime b) {
        return a.compareTo(b) == 0;
    }

}
