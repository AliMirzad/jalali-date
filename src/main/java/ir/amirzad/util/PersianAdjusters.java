/*
 * Project: jalali-date
 * File: ir/amirzad/util/PersianAdjusters.java
 * Description: Common adjusters for month/week/day-of-week boundaries in the Jalali calendar.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.util;

import ir.amirzad.core.PersianDate;

import java.time.DayOfWeek;
import java.util.Objects;

/**
 * PersianAdjusters: utility methods for common date calculations on {@link PersianDate}.
 * <p>
 * Java 8 compatible, no dependencies, integer-only under the hood via PersianDate.
 */
public final class PersianAdjusters {

    private PersianAdjusters() { /* no instances */ }

    /* ===================== Month boundaries ===================== */

    /** First day of the month containing {@code d}. */
    public static PersianDate firstDayOfMonth(PersianDate d) {
        Objects.requireNonNull(d, "d");
        return PersianDate.of(d.getYear(), d.getMonthValue(), 1);
    }

    /** Last day of the month containing {@code d}. */
    public static PersianDate lastDayOfMonth(PersianDate d) {
        Objects.requireNonNull(d, "d");
        int last = PersianDate.lengthOfMonth(d.getYear(), d.getMonthValue());
        return PersianDate.of(d.getYear(), d.getMonthValue(), last);
    }

    /** First day of next month after {@code d}. */
    public static PersianDate firstDayOfNextMonth(PersianDate d) {
        Objects.requireNonNull(d, "d");
        PersianDate nm = d.plusMonths(1);
        return PersianDate.of(nm.getYear(), nm.getMonthValue(), 1);
    }

    /** Last day of previous month before {@code d}. */
    public static PersianDate lastDayOfPreviousMonth(PersianDate d) {
        Objects.requireNonNull(d, "d");
        PersianDate pm = d.plusMonths(-1);
        int last = PersianDate.lengthOfMonth(pm.getYear(), pm.getMonthValue());
        return PersianDate.of(pm.getYear(), pm.getMonthValue(), last);
    }

    /* ===================== Week helpers ===================== */

    /** Start of week containing {@code d}, with a configurable week start (e.g., SATURDAY for Iran, MONDAY for ISO). */
    public static PersianDate startOfWeek(PersianDate d, DayOfWeek weekStart) {
        Objects.requireNonNull(d, "d");
        Objects.requireNonNull(weekStart, "weekStart");
        DayOfWeek cur = d.getDayOfWeek();
        int delta = daysBack(cur, weekStart);
        return d.plusDays(-delta);
    }

    /** End of week containing {@code d}, with a configurable week start. */
    public static PersianDate endOfWeek(PersianDate d, DayOfWeek weekStart) {
        PersianDate sow = startOfWeek(d, weekStart);
        return sow.plusDays(6);
    }

    /** Convenience: start of week assuming Saturday as week start (common in Iran). */
    public static PersianDate startOfWeekSaturday(PersianDate d) { return startOfWeek(d, DayOfWeek.SATURDAY); }

    /** Convenience: end of week assuming Saturday as week start (common in Iran). */
    public static PersianDate endOfWeekSaturday(PersianDate d) { return endOfWeek(d, DayOfWeek.SATURDAY); }

    private static int daysBack(DayOfWeek current, DayOfWeek targetWeekStart) {
        int c = current.getValue();         // MON=1..SUN=7
        int t = targetWeekStart.getValue();
        int diff = (c - t);
        if (diff < 0) diff += 7;
        return diff;
    }

    /* ===================== Day-of-week adjusters ===================== */

    /** Next date strictly after {@code d} matching {@code target}. */
    public static PersianDate next(PersianDate d, DayOfWeek target) {
        Objects.requireNonNull(d, "d"); Objects.requireNonNull(target, "target");
        int cur = d.getDayOfWeek().getValue();
        int tar = target.getValue();
        int diff = tar - cur;
        if (diff <= 0) diff += 7;
        return d.plusDays(diff);
    }

    /** Next date on or after {@code d} matching {@code target}. */
    public static PersianDate nextOrSame(PersianDate d, DayOfWeek target) {
        Objects.requireNonNull(d, "d"); Objects.requireNonNull(target, "target");
        if (d.getDayOfWeek() == target) return d;
        return next(d, target);
    }

    /** Previous date strictly before {@code d} matching {@code target}. */
    public static PersianDate previous(PersianDate d, DayOfWeek target) {
        Objects.requireNonNull(d, "d"); Objects.requireNonNull(target, "target");
        int cur = d.getDayOfWeek().getValue();
        int tar = target.getValue();
        int diff = cur - tar;
        if (diff <= 0) diff += 7;
        return d.plusDays(-diff);
    }

    /** Previous date on or before {@code d} matching {@code target}. */
    public static PersianDate previousOrSame(PersianDate d, DayOfWeek target) {
        Objects.requireNonNull(d, "d"); Objects.requireNonNull(target, "target");
        if (d.getDayOfWeek() == target) return d;
        return previous(d, target);
    }

    /* ===================== In-month day-of-week ===================== */

    /** First occurrence of {@code target} in the month of {@code d}. */
    public static PersianDate firstInMonth(PersianDate d, DayOfWeek target) {
        Objects.requireNonNull(d, "d"); Objects.requireNonNull(target, "target");
        PersianDate first = firstDayOfMonth(d);
        return nextOrSame(first, target);
    }

    /** Last occurrence of {@code target} in the month of {@code d}. */
    public static PersianDate lastInMonth(PersianDate d, DayOfWeek target) {
        Objects.requireNonNull(d, "d"); Objects.requireNonNull(target, "target");
        PersianDate last = lastDayOfMonth(d);
        return previousOrSame(last, target);
    }

    /** N-th occurrence (1-based) of {@code target} in the month of {@code d}. Returns null if not present. */
    public static PersianDate nthInMonth(PersianDate d, DayOfWeek target, int n) {
        if (n <= 0) throw new IllegalArgumentException("n must be >= 1");
        PersianDate first = firstInMonth(d, target);
        PersianDate nth = first.plusDays(7L * (n - 1));
        // verify still same month
        if (nth.getMonthValue() != d.getMonthValue() || nth.getYear() != d.getYear()) return null;
        return nth;
    }
}
