/*
 * Project: jalali-date
 * File: ir/amirzad/util/PersianDateRange.java
 * Description: Immutable date range with contains/overlaps/iterate/intersection/union operations.
 * Author: amirzad
 * 2025
 */

package ir.amirzad.util;

import ir.amirzad.core.PersianDate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Immutable date range for the Persian (Jalali) calendar.
 *
 * <p>Supports all four boundary modes: closed [a,b], half-open [a,b), (a,b], and open (a,b).
 * Iteration is day-based and integer-only via {@link PersianDate#plusDays(long)}.
 */
public final class PersianDateRange implements Iterable<PersianDate>, Serializable {

    private static final long serialVersionUID = 1L;

    public enum Bound {OPEN, CLOSED}

    private final PersianDate start;     // not null
    private final PersianDate end;       // not null (conceptual end boundary value)
    private final Bound startBound;      // OPEN or CLOSED
    private final Bound endBound;        // OPEN or CLOSED

    private PersianDateRange(PersianDate start, Bound startBound,
                             PersianDate end, Bound endBound) {
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.startBound = Objects.requireNonNull(startBound, "startBound");
        this.endBound = Objects.requireNonNull(endBound, "endBound");
        // normalize to allow start <= end always (empty if violates inclusivity)
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("start must be <= end: " + start + " .. " + end);
        }
    }

    /* ======================= Factories ======================= */

    /**
     * Closed range [start, end].
     */
    public static PersianDateRange closed(PersianDate start, PersianDate end) {
        return new PersianDateRange(start, Bound.CLOSED, end, Bound.CLOSED);
    }

    /**
     * Half-open range [start, end).
     */
    public static PersianDateRange halfOpen(PersianDate start, PersianDate end) {
        return new PersianDateRange(start, Bound.CLOSED, end, Bound.OPEN);
    }

    /**
     * Half-open (start, end].
     */
    public static PersianDateRange openClosed(PersianDate start, PersianDate end) {
        return new PersianDateRange(start, Bound.OPEN, end, Bound.CLOSED);
    }

    /**
     * Open range (start, end).
     */
    public static PersianDateRange open(PersianDate start, PersianDate end) {
        return new PersianDateRange(start, Bound.OPEN, end, Bound.OPEN);
    }

    /* ======================= Accessors ======================= */

    public PersianDate getStart() {
        return start;
    }

    public PersianDate getEnd() {
        return end;
    }

    public Bound getStartBound() {
        return startBound;
    }

    public Bound getEndBound() {
        return endBound;
    }

    /**
     * True if the set of dates is empty under the boundary semantics.
     */
    public boolean isEmpty() {
        int cmp = start.compareTo(end);
        if (cmp < 0) return false; // start < end => non-empty regardless of bounds
        // cmp == 0: single day present only if both bounds are CLOSED
        return !(startBound == Bound.CLOSED && endBound == Bound.CLOSED);
    }

    /**
     * Inclusive-equivalent end date for iteration, i.e., last contained date if non-empty.
     */
    private PersianDate effectiveEndInclusive() {
        if (isEmpty()) return null;
        if (endBound == Bound.CLOSED) return end;
        // [s, e) => last is e - 1 day, (s, e) => last is e - 1 unless s==e which was handled by isEmpty
        return end.minusDays(1);
    }

    /**
     * First contained date if non-empty given bounds.
     */
    private PersianDate effectiveStartInclusive() {
        if (isEmpty()) return null;
        if (startBound == Bound.CLOSED) return start;
        // (s, e] or (s, e) => first is s + 1 day
        return start.plusDays(1);
    }

    /**
     * Number of contained days (0 if empty).
     */
    public long lengthInDays() {
        if (isEmpty()) return 0L;
        PersianDate a = effectiveStartInclusive();
        PersianDate b = effectiveEndInclusive();
        long len = b.toJulianDay() - a.toJulianDay() + 1; // inclusive difference
        return Math.max(0L, len);
    }

    /**
     * Whether the given date lies within the range according to bounds.
     */
    public boolean contains(PersianDate d) {
        Objects.requireNonNull(d, "d");
        int cs = d.compareTo(start);
        int ce = d.compareTo(end);
        boolean leftOk = (cs > 0) || (cs == 0 && startBound == Bound.CLOSED);
        boolean rightOk = (ce < 0) || (ce == 0 && endBound == Bound.CLOSED);
        return leftOk && rightOk;
    }

    /**
     * Whether the two ranges share at least one date in common.
     */
    public boolean overlaps(PersianDateRange other) {
        Objects.requireNonNull(other, "other");
        PersianDate a1 = this.effectiveStartInclusive();
        PersianDate b1 = this.effectiveEndInclusive();
        PersianDate a2 = other.effectiveStartInclusive();
        PersianDate b2 = other.effectiveEndInclusive();
        if (a1 == null || b1 == null || a2 == null || b2 == null) return false;
        return a1.compareTo(b2) <= 0 && a2.compareTo(b1) <= 0; // [a1,b1] intersects [a2,b2]
    }

    /**
     * Intersection (common dates). Returns empty range if disjoint. Uses closed semantics for the result.
     */
    public PersianDateRange intersection(PersianDateRange other) {
        Objects.requireNonNull(other, "other");
        PersianDate a1 = this.effectiveStartInclusive();
        PersianDate b1 = this.effectiveEndInclusive();
        PersianDate a2 = other.effectiveStartInclusive();
        PersianDate b2 = other.effectiveEndInclusive();
        if (a1 == null || b1 == null || a2 == null || b2 == null) return halfOpen(start, start); // empty
        PersianDate lo = (a1.compareTo(a2) >= 0) ? a1 : a2;
        PersianDate hi = (b1.compareTo(b2) <= 0) ? b1 : b2;
        if (lo.compareTo(hi) > 0) return halfOpen(start, start); // empty
        return closed(lo, hi);
    }

    /**
     * Union if contiguous or overlapping; otherwise returns null.
     * Result uses closed semantics.
     */
    public PersianDateRange unionIfContiguous(PersianDateRange other) {
        Objects.requireNonNull(other, "other");
        PersianDate a1 = this.effectiveStartInclusive();
        PersianDate b1 = this.effectiveEndInclusive();
        PersianDate a2 = other.effectiveStartInclusive();
        PersianDate b2 = other.effectiveEndInclusive();
        if (a1 == null || b1 == null) return other;
        if (a2 == null || b2 == null) return this;
        // check if there is a gap > 1 day between [a1,b1] and [a2,b2]
        PersianDate left = (a1.compareTo(a2) <= 0) ? a1 : a2;
        PersianDate leftEnd = (left == a1) ? b1 : b2;
        PersianDate right = (left == a1) ? a2 : a1;
        PersianDate rightEnd = (left == a1) ? b2 : b1;
        long gap = right.toJulianDay() - leftEnd.toJulianDay();
        if (gap > 1) return null; // not contiguous or overlapping
        PersianDate lo = left;
        PersianDate hi = (rightEnd.compareTo(leftEnd) >= 0) ? rightEnd : leftEnd;
        return closed(lo, hi);
    }

    /* ======================= Iteration ======================= */

    @Override
    public Iterator<PersianDate> iterator() {
        PersianDate first = effectiveStartInclusive();
        PersianDate last = effectiveEndInclusive();
        return new Iterator<PersianDate>() {
            PersianDate cur = first;

            @Override
            public boolean hasNext() {
                return cur != null && cur.compareTo(last) <= 0;
            }

            @Override
            public PersianDate next() {
                if (!hasNext()) throw new NoSuchElementException();
                PersianDate r = cur;
                cur = cur.plusDays(1);
                return r;
            }
        };
    }

    /**
     * Stream view over dates in the range (day step = 1).
     */
    public Stream<PersianDate> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    @Override
    public String toString() {
        String lb = (startBound == Bound.CLOSED) ? "[" : "(";
        String rb = (endBound == Bound.CLOSED) ? "]" : ")";
        return lb + start + " .. " + end + rb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersianDateRange)) return false;
        PersianDateRange that = (PersianDateRange) o;
        return start.equals(that.start) && end.equals(that.end)
                && startBound == that.startBound && endBound == that.endBound;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, startBound, endBound);
    }
}
