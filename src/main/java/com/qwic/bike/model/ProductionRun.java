package com.qwic.bike.model;

import java.time.LocalDateTime;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwic.bike.util.DateTimeUtils;

public class ProductionRun implements Comparable<ProductionRun> {
	
	/**
	 * JSON input format: yyyy-MM-ddTHH:mm:ss.SSSz
	 */
	@JsonProperty("startingDay")
	private LocalDateTime startDateTime;

	@JsonProperty("duration")
	private long durationDays;

	public static final Comparator<ProductionRun> COMPARATOR = new Comparator<ProductionRun>() {
		@Override
		public int compare(ProductionRun o1, ProductionRun o2) {
			final int O1_BEFORE = -1;
			final int EQUAL = 0;
			final int O1_AFTER = 1;

			if (o1 == o2 || (o1 == null && o2 == null))
				return EQUAL;
			if (o1 == null)
				return O1_BEFORE;
			if (o2 == null)
				return O1_AFTER;
			// if o1 is before o2, o1 should be first;
			if (o1.startDateTime.isBefore(o2.startDateTime))
				return O1_BEFORE;
			if (o2.startDateTime.isBefore(o1.startDateTime))
				return O1_AFTER;

			// if we're here, they're on the same start datetime
			// longest duration should be first
			if (o1.durationDays > o2.durationDays)
				return O1_BEFORE;
			if (o2.durationDays > o1.durationDays)
				return O1_AFTER;

			// all comparisons have yielded equality
			// verify that compareTo is consistent with equals (optional)
			assert o1.equals(o2) : "compareTo inconsistent with equals.";

			return EQUAL;
		}
	};

	public ProductionRun() {
	}

	public ProductionRun(LocalDateTime startDateTime, long durationDays) {
		this.startDateTime = startDateTime;
		this.durationDays = durationDays;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return startDateTime.plusDays(durationDays - 1);
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public long getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(long durationDays) {
		this.durationDays = durationDays;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (durationDays ^ (durationDays >>> 32));
		result = prime * result + ((startDateTime == null) ? 0 : startDateTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductionRun other = (ProductionRun) obj;
		if (durationDays != other.durationDays)
			return false;
		if (startDateTime == null) {
			if (other.startDateTime != null)
				return false;
		} else if (!startDateTime.equals(other.startDateTime))
			return false;
		return true;
	}

	@Override
	public int compareTo(ProductionRun that) {
		return COMPARATOR.compare(this, that);
	}
	
	public boolean isClash(final ProductionRun that) {
		
		LocalDateTime thisStart = this.getStartDateTime();
		LocalDateTime thatStart = that.getStartDateTime();
		LocalDateTime thisEnd = this.getEndDateTime();
		LocalDateTime thatEnd = that.getEndDateTime();

		// if this' start is between the other's start/end
		if (DateTimeUtils.isDateTimeInRange(thisStart, thatStart, thatEnd))
			return true;
		// if this' end  is between the other's start/end
		if (DateTimeUtils.isDateTimeInRange(thisEnd, thatStart, thatEnd))
			return true;
		// if that's start is between this' start/end
		if (DateTimeUtils.isDateTimeInRange(thatStart, thisStart, thisEnd))
			return true;
		// if that's end is between this' start/end
		if (DateTimeUtils.isDateTimeInRange(thatEnd, thisStart, thisEnd))
			return true;
		
		return false;
	}


	@Override
	public String toString() {
		return "ProductionRun [startDateTime=" + startDateTime + ", durationDays=" + durationDays
				+ ", getEndDateTime()=" + getEndDateTime() + "]";
	}

}
