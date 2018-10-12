package com.qwic.bike.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductionRun {

	/**
	 * JSON input format: yyyy-MM-ddTHH:mm:ss.SSSz
	 */
	@JsonProperty("startingDay")
	private LocalDateTime startDateTime;
	
	@JsonProperty("duration")
	private long durationDays;

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
		return startDateTime.plusDays(durationDays);
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
}
