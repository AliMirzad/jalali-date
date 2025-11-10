package ir.amirzad.core;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

/*
 * Project: jalali-date
 * File: ir/amirzad/core/PersianDate.java
 * Description: Jalali (Persian) calendar date — core arithmetic and conversions (integer-only, JDK8)
 * Author: amirzad
 * 2025
 */

/**
 * Core Jalali (Persian) calendar date type; integer-only arithmetic, full JDN bridge.
 * <p>
 * - Java 8 compatible
 * - No Calendar/SimpleDateFormat usage
 * - Precise conversions Jalali ⇄ Gregorian ⇄ JDN (jalaali.js family)
 * - Basic ops: compare, plus/minus days/months/years, month length, leap year
 *
 * <p>All computations are integer-only.
 */
public final class PersianDate implements Comparable<PersianDate> {

    private final int year;   // e.g., 1404
    private final int month;  // 1..12
    private final int day;    // 1..29/30/31 (depends on month & leap)

    private PersianDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        validate(year, month, day);
    }

    /* ======================= Factory ======================= */

    public static PersianDate of(int year, int month, int day) {
        return new PersianDate(year, month, day);
    }

    /* ======================= Accessors ======================= */

    public int getYear() { return year; }
    public int getMonthValue() { return month; }
    public int getDayOfMonth() { return day; }

    /* ======================= Validation ======================= */

    private static void validate(int y, int m, int d) {
        if (m < 1 || m > 12) throw new IllegalArgumentException("Month out of range: " + m);
        int len = lengthOfMonth(y, m);
        if (d < 1 || d > len) {
            throw new IllegalArgumentException("Day out of range: " + d + " for y/m=" + y + "/" + m + " (len=" + len + ")");
        }
    }

    /* ======================= Month length / leap ======================= */

    /** Length of a Jalali month. */
    public static int lengthOfMonth(int jy, int jm) {
        if (jm >= 1 && jm <= 6) return 31;
        if (jm >= 7 && jm <= 11) return 30;
        // month 12:
        return isLeapYear(jy) ? 30 : 29;
    }

    /** Is Jalali year leap? via Farvardin-1 JDN delta. */
    public static boolean isLeapYear(int jy) {
        int[] c1 = jalCal(jy);
        int[] c2 = jalCal(jy + 1);
        long j1 = gregorianToJdn(c1[0], 3, c1[1]);
        long j2 = gregorianToJdn(c2[0], 3, c2[1]);
        return (j2 - j1) == 366;
    }

    /* ======================= JDN bridge ======================= */

    /** Astronomical Julian Day Number (JDN) for this Jalali date. */
    public long toJulianDay() {
        return jalaliToJdn(this.year, this.month, this.day);
    }

    /** PersianDate from JDN. */
    public static PersianDate ofJulianDay(long jdn) {
        int[] j = jdnToJalali(jdn);
        return of(j[0], j[1], j[2]);
    }

    /* ======================= Gregorian bridge ======================= */

    /** Convert to Gregorian LocalDate. */
    public LocalDate toGregorian() {
        long jdn = this.toJulianDay();
        int[] g = jdnToGregorian(jdn);
        return LocalDate.of(g[0], g[1], g[2]);
    }

    /** Create from Gregorian LocalDate. */
    public static PersianDate fromGregorian(LocalDate g) {
        Objects.requireNonNull(g, "g");
        long jdn = gregorianToJdn(g.getYear(), g.getMonthValue(), g.getDayOfMonth());
        int[] j = jdnToJalali(jdn);
        return of(j[0], j[1], j[2]);
    }

    /* ======================= Arithmetic ======================= */

    public PersianDate plusDays(long days) {
        long j = this.toJulianDay() + days;
        return ofJulianDay(j);
    }

    public PersianDate minusDays(long days) {
        return plusDays(-days);
    }

    public PersianDate plusMonths(int months) {
        if (months == 0) return this;
        int totalMonths = (this.year * 12) + (this.month - 1) + months;
        int ny = floorDiv(totalMonths, 12);
        int nm = mod(totalMonths, 12) + 1;
        int nd = Math.min(this.day, lengthOfMonth(ny, nm));
        return of(ny, nm, nd);
    }

    public PersianDate plusYears(int years) {
        if (years == 0) return this;
        int ny = this.year + years;
        int nd = Math.min(this.day, lengthOfMonth(ny, this.month));
        return of(ny, this.month, nd);
    }

    /* ======================= Comparison / DayOfWeek ======================= */

    @Override
    public int compareTo(PersianDate o) {
        if (this == o) return 0;
        int cy = Integer.compare(this.year, o.year);
        if (cy != 0) return cy;
        int cm = Integer.compare(this.month, o.month);
        if (cm != 0) return cm;
        return Integer.compare(this.day, o.day);
    }

    public DayOfWeek getDayOfWeek() {
        // Map to Gregorian and reuse JDK
        return toGregorian().getDayOfWeek();
    }

    /* ======================= Object basics ======================= */

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PersianDate)) return false;
        PersianDate other = (PersianDate) obj;
        return this.year == other.year && this.month == other.month && this.day == other.day;
    }

    @Override public int hashCode() {
        int h = year;
        h = h * 31 + month;
        h = h * 31 + day;
        return h;
    }

    @Override public String toString() {
        String mm = (month < 10 ? "0" : "") + month;
        String dd = (day < 10 ? "0" : "") + day;
        return year + "-" + mm + "-" + dd;
    }

    /* ===========================================================
       ========  Internal: Gregorian <-> JDN (proleptic)  ========
       =========================================================== */

    private static long gregorianToJdn(int gy, int gm, int gd) {
        int a = (14 - gm) / 12;
        int y = gy + 4800 - a;
        int m = gm + 12 * a - 3;
        return gd
                + (153L * m + 2) / 5
                + 365L * y
                + y / 4
                - y / 100
                + y / 400
                - 32045;
    }

    private static int[] jdnToGregorian(long jdn) {
        long a = jdn + 32044;
        long b = (4 * a + 3) / 146097;
        long c = a - (146097 * b) / 4;
        long d = (4 * c + 3) / 1461;
        long e = c - (1461 * d) / 4;
        long m = (5 * e + 2) / 153;

        int day = (int)(e - (153 * m + 2) / 5 + 1);
        int month = (int)(m + 3 - 12 * (m / 10));
        int year = (int)(100 * b + d - 4800 + (m / 10));
        return new int[]{year, month, day};
    }

    /* ===========================================================
       ========  Internal: Jalali core (jalaali.js family)  ======
       =========================================================== */

    /**
     * Break points used by the Jalaali calendar.
     * These are years where the leap-year pattern shifts.
     */
    private static final int[] JALAALI_BREAKS = {
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394,
            2456, 3178
    };

    /**
     * jalCal(jy): compute corresponding Gregorian year and March day for Farvardin 1.
     * <p>Returns int[]{gy, marchDay}.
     * Implemented per jalaali.js approach.
     */
    private static int[] jalCal(int jy) {
        int gy = jy + 621;

        int leapJ = -14;
        int jp = JALAALI_BREAKS[0];
        int jm, jump;

        for (int i = 1; i < JALAALI_BREAKS.length; i++) {
            jm = JALAALI_BREAKS[i];
            jump = jm - jp;
            if (jy < jm) break;
            // each 33-year block has 8 leap years
            leapJ += (jump / 33) * 8 + ((jump % 33) + 3) / 4;
            jp = jm;
        }

        int n = jy - jp;
        leapJ += (n / 33) * 8 + ((n % 33) + 3) / 4;

        // Gregorian leap days since 1600
        int g = gy - 1600;
        int leapG = g / 4 - g / 100 + g / 400;

        // Farvardin 1 corresponds to March (20 + leapJ - leapG)
        int march = 20 + leapJ - leapG;

        return new int[]{gy, march};
    }

    /** JDN from jalali y/m/d (using zero-based day-of-year offset). */
    private static long jalaliToJdn(int jy, int jm, int jd) {
        int[] cal = jalCal(jy);
        int gy = cal[0];
        int marchDay = cal[1];

        long jdn1Farvardin = gregorianToJdn(gy, 3, marchDay);

        int dayOfYear0; // zero-based index within the Jalali year
        if (jm <= 7) {
            dayOfYear0 = (jm - 1) * 31 + (jd - 1);
        } else {
            dayOfYear0 = 6 * 31 + (jm - 7) * 30 + (jd - 1);
        }
        return jdn1Farvardin + dayOfYear0;
    }

    /** jalali y/m/d from JDN (inverse; zero-based offset to avoid month=13). */
    private static int[] jdnToJalali(long jdn) {
        // Convert JDN -> Gregorian to locate the current gy and then Farvardin 1 JDN
        int[] g = jdnToGregorian(jdn);
        int gy = g[0];
        int jy = gy - 621;

        int[] cal = jalCal(jy);
        int gyMarch = cal[0];
        int marchDay = cal[1];

        long jdn1Farvardin = gregorianToJdn(gyMarch, 3, marchDay);

        int d0 = (int)(jdn - jdn1Farvardin); // zero-based day index (0..365)
        int jm, jd;

        if (d0 >= 0 && d0 <= 185) {
            // Farvardin..Shahrivar: 6 months * 31 days
            jm = (d0 / 31) + 1;
            jd = (d0 % 31) + 1;
        } else {
            int d1;
            if (d0 >= 0) {
                d1 = d0 - 186;
            } else {
                // If JDN is before Farvardin 1 of jy, go back to previous jalali year
                jy -= 1;
                cal = jalCal(jy);
                gyMarch = cal[0];
                marchDay = cal[1];
                jdn1Farvardin = gregorianToJdn(gyMarch, 3, marchDay);
                d1 = (int)(jdn - jdn1Farvardin) - 186;
            }
            jm = (d1 / 30) + 7;     // Mehr..Esfand
            jd = (d1 % 30) + 1;
        }

        return new int[]{jy, jm, jd};
    }

    /* ======================= Small integer helpers ======================= */

    private static int floorDiv(int x, int y) {
        int r = x / y;
        if ((x ^ y) < 0 && (x % y != 0)) r--;
        return r;
    }

    private static int mod(int x, int y) {
        int m = x % y;
        if (m < 0) m += y;
        return m;
    }
}
