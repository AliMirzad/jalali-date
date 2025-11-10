/*
 * Project: jalali-date
 * File: ir/amirzad/interop/DateInterop.java
 * Description: Bridge utilities to interoperate with java.util.Date and ZoneId.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.interop;

import ir.amirzad.core.PersianDate;
import ir.amirzad.core.PersianDateTime;
import ir.amirzad.format.PersianDateFormatter;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * DateInterop — Java 8 compatible bridge for working with {@link java.util.Date}
 * while leveraging the integer-only Jalali core (PersianDate / PersianDateTime).
 *
 * <p>Design goals:
 * <ul>
 *   <li>No use of {@code Calendar} or {@code SimpleDateFormat}.</li>
 *   <li>Thread-safe, deterministic behavior.</li>
 *   <li>Explicit ZoneId for conversions that depend on time zone.</li>
 * </ul>
 *
 * <p>Default digits in formatted output are LATIN for maximum interoperability.
 */
public final class DateInterop {

    public static final String DEFAULT_PATTERN = "yyyy/MM/dd";

    private DateInterop() { /* no instances */ }

    /* =============================================================
     * Formatting: Date -> Jalali text
     * ============================================================= */

    /** Format a legacy {@link Date} as a Jalali string with default pattern and system zone. */
    public static String toJalaliString(Date date) {
        return toJalaliString(date, DEFAULT_PATTERN, ZoneId.systemDefault(), PersianDateFormatter.DigitStyle.LATIN);
    }

    /** Format a legacy {@link Date} as a Jalali string with given pattern and system zone. */
    public static String toJalaliString(Date date, String pattern) {
        return toJalaliString(date, pattern, ZoneId.systemDefault(), PersianDateFormatter.DigitStyle.LATIN);
    }

    /** Format a legacy {@link Date} as a Jalali string with pattern + zone + digit style. */
    public static String toJalaliString(Date date,
                                        String pattern,
                                        ZoneId zone,
                                        PersianDateFormatter.DigitStyle digits) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(digits, "digits");
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), zone);
        PersianDateTime pdt = PersianDateTime.fromGregorian(ldt);
        return new PersianDateFormatter(pattern, digits).format(pdt);
    }

    /* =============================================================
     * Parsing: Jalali text -> Date (at start-of-day)
     * ============================================================= */

    /** Parse Jalali text (default pattern, system zone) to {@link Date} at start-of-day. */
    public static Date parseJalaliToDate(String text) {
        return parseJalaliToDate(text, DEFAULT_PATTERN, ZoneId.systemDefault());
    }

    /** Parse Jalali text (pattern, zone) to {@link Date} at start-of-day. */
    public static Date parseJalaliToDate(String text, String pattern, ZoneId zone) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(zone, "zone");
        PersianDate d = new PersianDateFormatter(pattern, PersianDateFormatter.DigitStyle.LATIN).parseDate(text);
        LocalDate g = d.toGregorian();
        ZonedDateTime zdt = g.atStartOfDay(zone);
        return Date.from(zdt.toInstant());
    }

    /** Try-parse variant; returns Optional.empty() if parsing fails. */
    public static Optional<Date> tryParseJalaliToDate(String text, String pattern, ZoneId zone) {
        try { return Optional.of(parseJalaliToDate(text, pattern, zone)); }
        catch (RuntimeException ex) { return Optional.empty(); }
    }

    /* =============================================================
     * Getters / computations based on Date
     * ============================================================= */

    /** Jalali year for the given legacy {@link Date} in the system default zone. */
    public static int jalaliYearOf(Date date) {
        Objects.requireNonNull(date, "date");
        LocalDate g = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return PersianDate.fromGregorian(g).getYear();
    }

    /** Date with Jalali year from 'base' and provided {@code month1Based} (1..12) and day. */
    public static Date dayOfYear(Date base, int month1Based, int dayOfMonth) {
        Objects.requireNonNull(base, "base");
        if (month1Based < 1 || month1Based > 12) throw new IllegalArgumentException("month must be 1..12");
        if (dayOfMonth < 1 || dayOfMonth > 31) throw new IllegalArgumentException("day out of range: " + dayOfMonth);
        LocalDate g = base.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        PersianDate pd = PersianDate.fromGregorian(g);
        int max = PersianDate.lengthOfMonth(pd.getYear(), month1Based);
        if (dayOfMonth > max) throw new IllegalArgumentException("day out of range for month: " + dayOfMonth + "/" + month1Based);
        PersianDate out = PersianDate.of(pd.getYear(), month1Based, dayOfMonth);
        return Date.from(out.toGregorian().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** First day of the Jalali year that contains the given Date. */
    public static Date firstDayOfYear(Date base) {
        Objects.requireNonNull(base, "base");
        LocalDate g = base.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        PersianDate pd = PersianDate.fromGregorian(g);
        PersianDate first = PersianDate.of(pd.getYear(), 1, 1);
        return Date.from(first.toGregorian().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Last day of the Jalali year that contains the given Date. */
    public static Date lastDayOfYear(Date base) {
        Objects.requireNonNull(base, "base");
        LocalDate g = base.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        PersianDate pd = PersianDate.fromGregorian(g);
        int lastDay = PersianDate.lengthOfMonth(pd.getYear(), 12);
        PersianDate last = PersianDate.of(pd.getYear(), 12, lastDay);
        return Date.from(last.toGregorian().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /* =============================================================
     * Builders: Jalali -> Date
     * ============================================================= */

    public static Date fromJalali(int year, int month1Based, int day) {
        return fromJalali(year, month1Based, day, 0, 0, 0, ZoneId.systemDefault());
    }

    public static Date fromJalali(int year, int month1Based, int day, int hour, int minute) {
        return fromJalali(year, month1Based, day, hour, minute, 0, ZoneId.systemDefault());
    }

    public static Date fromJalali(int year, int month1Based, int day, int hour, int minute, int second) {
        return fromJalali(year, month1Based, day, hour, minute, second, ZoneId.systemDefault());
    }

    public static Date fromJalali(int year, int month1Based, int day, int hour, int minute, int second, ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        PersianDateTime pdt = PersianDateTime.of(year, month1Based, day, hour, minute, second, 0);
        LocalDateTime ldt = pdt.toGregorian();
        return Date.from(ldt.atZone(zone).toInstant());
    }

    /* =============================================================
     * Optional Calendar-style month helpers (0-based months)
     * ============================================================= */

    /** For code that still uses 0-based months (Calendar style). */
    public static Date fromJalaliMonth0(int year, int month0Based, int day) {
        return fromJalali(year, month0Based + 1, day);
    }

    /** For code that still uses 0-based months (Calendar style). */
    public static Date dayOfYearMonth0(Date base, int month0Based, int dayOfMonth) {
        return dayOfYear(base, month0Based + 1, dayOfMonth);
    }

    /* =============================================================
     * Convenience: now/today as Date using a Zone/Clock
     * ============================================================= */

    public static Date todayAsDate(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        LocalDate today = LocalDate.now(zone);
        return Date.from(today.atStartOfDay(zone).toInstant());
    }

    public static Date nowAsDate(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        Instant now = clock.instant();
        return Date.from(now);
    }
}
