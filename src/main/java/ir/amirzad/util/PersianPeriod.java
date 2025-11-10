/*
 * Project: jalali-date
 * File: ir/amirzad/util/PersianPeriod.java
 * Description: Difference between two PersianDate values in years/months/days.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.util;

import ir.amirzad.core.PersianDate;

import java.io.Serializable;
import java.util.Objects;

/**
 * PersianPeriod: difference in years, months, days for the Persian (Jalali) calendar.
 * <p>
 * Java-8 compatible, immutable, integer-only.
 */
public final class PersianPeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int years;
    private final int months; // 0..11 or negative if overall negative
    private final int days;   // 0..30  depending on month length; sign follows period sign

    private PersianPeriod(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    public static PersianPeriod of(int years, int months, int days) {
        return new PersianPeriod(years, months, days);
    }

    /**
     * Compute normalized signed period from start (inclusive) to end (exclusive).
     */
    public static PersianPeriod between(PersianDate startInclusive, PersianDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive");
        Objects.requireNonNull(endExclusive, "endExclusive");
        if (startInclusive.equals(endExclusive)) return of(0, 0, 0);
        boolean negative = false;
        PersianDate a = startInclusive;
        PersianDate b = endExclusive;
        if (a.compareTo(b) > 0) { // swap and mark negative
            PersianDate tmp = a;
            a = b;
            b = tmp;
            negative = true;
        }
        int y = b.getYear() - a.getYear();
        int m = b.getMonthValue() - a.getMonthValue();
        int d = b.getDayOfMonth() - a.getDayOfMonth();

        if (d < 0) {
            // borrow from month
            PersianDate prevOfB = b.plusMonths(-1);
            d += PersianDate.lengthOfMonth(prevOfB.getYear(), prevOfB.getMonthValue());
            m -= 1;
        }
        if (m < 0) {
            m += 12;
            y -= 1;
        }
        if (negative) {
            y = -y;
            m = -m;
            d = -d;
        }
        return of(y, m, d);
    }

    /**
     * Add this period to the given PersianDate.
     */
    public PersianDate addTo(PersianDate date) {
        Objects.requireNonNull(date, "date");
        PersianDate d = date.plusYears(this.years).plusMonths(this.months).plusDays(this.days);
        return d;
    }

    /**
     * Subtract this period from the given PersianDate.
     */
    public PersianDate subtractFrom(PersianDate date) {
        Objects.requireNonNull(date, "date");
        PersianDate d = date.plusYears(-this.years).plusMonths(-this.months).plusDays(-this.days);
        return d;
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public boolean isZero() {
        return years == 0 && months == 0 && days == 0;
    }

    public boolean isNegative() {
        return years < 0 || (years == 0 && (months < 0 || (months == 0 && days < 0)));
    }

    public PersianPeriod plus(PersianPeriod other) {
        Objects.requireNonNull(other, "other");
        return of(this.years + other.years, this.months + other.months, this.days + other.days);
    }

    public PersianPeriod minus(PersianPeriod other) {
        Objects.requireNonNull(other, "other");
        return of(this.years - other.years, this.months - other.months, this.days - other.days);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersianPeriod)) return false;
        PersianPeriod that = (PersianPeriod) o;
        return years == that.years && months == that.months && days == that.days;
    }

    @Override
    public int hashCode() {
        return Objects.hash(years, months, days);
    }

    @Override
    public String toString() {
        return "P" + years + "Y" + months + "M" + days + "D"; // ISO-like
    }
}
