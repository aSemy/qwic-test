package com.qwic.bike.util;

import java.time.LocalDateTime;

public abstract class DateTimeUtils {

	/**
	 * If a given date is between two boundaries (inclusive, i.e. the given date can
	 * equal the boundary), then return true.
	 * <p>
	 * E.g. Is 2018-01-02 between 2018-01-01 and 2018-01-03? TRUE
	 * <p>
	 * Is 2018-04-01 between 2018-04-01 and 2018-04-05? TRUE
	 * <p>
	 * Is 2018-03-10 between 2018-03-11 and 2018-03-15? FALSE
	 * 
	 * @param dt
	 * @param startInclusive
	 * @param endInclusive
	 * @return
	 */
	public static boolean isDateTimeInRange(LocalDateTime dt, LocalDateTime startInclusive,
			LocalDateTime endInclusive) {
		if (dt.isBefore(startInclusive) || dt.isAfter(endInclusive))
			return false;
		else
			return true;
	}

}
