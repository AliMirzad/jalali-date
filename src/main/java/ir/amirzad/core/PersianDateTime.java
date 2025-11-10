/*
 * Project: jalali-date
 * File: ir/amirzad/core/PersianDateTime.java
 * Description: Jalali date-time with nano-of-day — core time arithmetic and conversions.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.core;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * PersianDateTime: Jalali date + time-of-day with integer-only arithmetic.
 *
 * - Relies on {@link PersianDate} for calendar arithmetic and JDN bridging.
 * - Stores time as nano-of-day (long), avoiding floating point and spill errors.
 * - Provides conversions to/from ISO Gregorian LocalDateTime via PersianDate bridge.
 */
public final class PersianDateTime implements Comparable<PersianDateTime>, Serializable {

    private static final long serialVersionUID = 1L;

    // ======= Time constants =======
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long NANOS_PER_MINUTE = 60L * NANOS_PER_SECOND;
    private static final long NANOS_PER_HOUR   = 60L * NANOS_PER_MINUTE;
    private static final long NANOS_PER_DAY    = 24L * NANOS_PER_HOUR; // 86,400,000,000,000

    private final PersianDate date; // non-null
    private final long nanoOfDay;   // 0 <= nanoOfDay < NANOS_PER_DAY

    // ======= Construction =======
    private PersianDateTime(PersianDate date, long nanoOfDay) {
        this.date = Objects.requireNonNull(date, "date");
        if (nanoOfDay < 0 || nanoOfDay >= NANOS_PER_DAY)
            throw new IllegalArgumentException("nanoOfDay out of range: " + nanoOfDay);
        this.nanoOfDay = nanoOfDay;
    }

    public static PersianDateTime of(int year, int month, int day, int hour, int minute, int second, int nano) {
        PersianDate d = PersianDate.of(year, month, day);
        long nod = toNanoOfDay(hour, minute, second, nano);
        return new PersianDateTime(d, nod);
    }

    public static PersianDateTime of(PersianDate date, int hour, int minute, int second, int nano) {
        long nod = toNanoOfDay(hour, minute, second, nano);
        return new PersianDateTime(date, nod);
    }

    public static PersianDateTime of(PersianDate date, long nanoOfDay) {
        return new PersianDateTime(date, nanoOfDay);
    }

    private static long toNanoOfDay(int hour, int minute, int second, int nano) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("hour: " + hour);
        if (minute < 0 || minute > 59) throw new IllegalArgumentException("minute: " + minute);
        if (second < 0 || second > 59) throw new IllegalArgumentException("second: " + second);
        if (nano < 0 || nano > 999_999_999) throw new IllegalArgumentException("nano: " + nano);
        return hour * NANOS_PER_HOUR + (long) minute * NANOS_PER_MINUTE + (long) second * NANOS_PER_SECOND + nano;
    }

    // ======= Accessors =======
    public PersianDate toDate() { return date; }
    public int getYear() { return date.getYear(); }
    public int getMonthValue() { return date.getMonthValue(); }
    public int getDayOfMonth() { return date.getDayOfMonth(); }

    public long getNanoOfDay() { return nanoOfDay; }
    public int getHour() { return (int) (nanoOfDay / NANOS_PER_HOUR); }
    public int getMinute() { return (int) ((nanoOfDay % NANOS_PER_HOUR) / NANOS_PER_MINUTE); }
    public int getSecond() { return (int) ((nanoOfDay % NANOS_PER_MINUTE) / NANOS_PER_SECOND); }
    public int getNano() { return (int) (nanoOfDay % NANOS_PER_SECOND); }

    // ======= Conversions =======
    /**
     * JDN of the date part (delegates to PersianDate).
     */
    public long toJulianDay() { return date.toJulianDay(); }

    /** Convert to ISO Gregorian LocalDateTime using PersianDate bridge. */
    public LocalDateTime toGregorian() {
        LocalDate g = date.toGregorian();
        return LocalDateTime.of(g.getYear(), g.getMonthValue(), g.getDayOfMonth(), getHour(), getMinute(), getSecond(), getNano());
    }

    /** Construct from ISO Gregorian LocalDateTime (bridge through PersianDate). */
    public static PersianDateTime fromGregorian(LocalDateTime ldt) {
        PersianDate d = PersianDate.fromGregorian(ldt.toLocalDate());
        return PersianDateTime.of(d, ldt.getHour(), ldt.getMinute(), ldt.getSecond(), ldt.getNano());
    }

    // ======= Arithmetic =======
    public PersianDateTime plusDays(long days) {
        if (days == 0) return this;
        return new PersianDateTime(this.date.plusDays(days), this.nanoOfDay);
    }

    public PersianDateTime plusHours(long hours) { return plusNanos(hours * NANOS_PER_HOUR); }
    public PersianDateTime plusMinutes(long minutes) { return plusNanos(minutes * NANOS_PER_MINUTE); }
    public PersianDateTime plusSeconds(long seconds) { return plusNanos(seconds * NANOS_PER_SECOND); }

    public PersianDateTime plusNanos(long nanos) {
        if (nanos == 0) return this;
        long sum = this.nanoOfDay + nanos;
        long daysOverflow = floorDiv(sum, NANOS_PER_DAY); // negative-safe
        long nod = floorMod(sum, NANOS_PER_DAY);
        PersianDate newDate = (daysOverflow == 0) ? this.date : this.date.plusDays(daysOverflow);
        return new PersianDateTime(newDate, nod);
    }

    public PersianDateTime minusDays(long days) { return plusDays(-days); }
    public PersianDateTime minusHours(long hours) { return plusHours(-hours); }
    public PersianDateTime minusMinutes(long minutes) { return plusMinutes(-minutes); }
    public PersianDateTime minusSeconds(long seconds) { return plusSeconds(-seconds); }
    public PersianDateTime minusNanos(long nanos) { return plusNanos(-nanos); }

    // ======= Utilities =======
    private static long floorDiv(long x, long y) { return Math.floorDiv(x, y); }
    private static long floorMod(long x, long y) { return Math.floorMod(x, y); }

    @Override public int compareTo(PersianDateTime o) {
        int c = this.date.compareTo(o.date);
        if (c != 0) return c;
        return Long.compare(this.nanoOfDay, o.nanoOfDay);
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PersianDateTime)) return false;
        PersianDateTime other = (PersianDateTime) obj;
        return this.nanoOfDay == other.nanoOfDay && this.date.equals(other.date);
    }

    @Override public int hashCode() { return Objects.hash(date, nanoOfDay); }

    @Override public String toString() {
        // Example: 1404-08-19T13:05:09.123456789(persian)
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%09d(persian)",
                getYear(), getMonthValue(), getDayOfMonth(),
                getHour(), getMinute(), getSecond(), getNano());
    }
}
