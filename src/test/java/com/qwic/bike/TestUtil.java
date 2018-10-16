package com.qwic.bike;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.qwic.bike.model.ProductionRun;

public abstract class TestUtil {

	private final static SecureRandom random = new SecureRandom();
	
	public static List<ProductionRun> createNonClashingRuns(final long count, final LocalDateTime originalStart) {
		final long gap = 3;
		final long duration = 5;

		final List<ProductionRun> runs = new ArrayList<>();

		LocalDateTime start = originalStart;
		for (int i = 0; i < count; i++) {
			ProductionRun pr = new ProductionRun(start, duration);
			runs.add(pr);

			start = pr.getEndDateTime().plusDays(gap);
		}
		
		// make our tests work harder! Shuffle the list
		Collections.shuffle(runs, random);
		
		return runs;
	}

}
