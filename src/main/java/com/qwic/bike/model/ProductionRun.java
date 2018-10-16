package com.qwic.bike.model;

import java.time.LocalDateTime;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonProperty;

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
			// sort by end time, earliest end time first
			if (o1.getEndDateTime().isBefore(o2.getEndDateTime()))
				return O1_BEFORE;
			if (o2.getEndDateTime().isBefore(o1.getEndDateTime()))
				return O1_AFTER;

			// sort by start time, earliest first
			if (o1.getStartDateTime().isBefore(o2.getStartDateTime()))
				return O1_BEFORE;
			if (o2.getStartDateTime().isBefore(o1.getStartDateTime()))
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

	@Override
	public String toString() {
		return "ProductionRun [startDateTime=" + startDateTime + ", durationDays=" + durationDays
				+ ", getEndDateTime()=" + getEndDateTime() + "]";
	}

}
