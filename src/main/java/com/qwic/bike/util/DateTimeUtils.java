package com.qwic.bike.util;

import java.time.LocalDateTime;

public abstract class DateTimeUtils {

	public static boolean isDateTimeInRange(LocalDateTime dt, LocalDateTime startExclusive, LocalDateTime endExclusive) {
		if (dt.isBefore(startExclusive) || dt.isEqual(startExclusive))
			return false;
		if (dt.isAfter(endExclusive) || dt.equals(endExclusive))
			return false;
		return true;
	}

}
