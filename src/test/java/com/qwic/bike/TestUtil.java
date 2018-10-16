package com.qwic.bike;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator.OfLong;
import java.util.Random;

import com.qwic.bike.model.ProductionRun;

public abstract class TestUtil {

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

		return runs;
	}

	/**
	 * Create clashes between adjacent runs. Returns only the clashes, not the
	 * existing runs.
	 */
	public static List<ProductionRun> createAdjacentClashes(final List<ProductionRun> runs) {

		final List<ProductionRun> clashingRuns = new ArrayList<>();

		for (int i = 0; i < runs.size(); i++) {
			ProductionRun run = runs.get(i);

			if (i + 1 >= runs.size()) {
				break;
			} else {
				ProductionRun nextRun = runs.get(i + 1);

				LocalDateTime clashStart = run.getEndDateTime().minusDays(1);
				LocalDateTime clashEnd = nextRun.getStartDateTime().plusDays(1);

				long clashDuration = ChronoUnit.DAYS.between(clashStart, clashEnd);

				ProductionRun clashingRun = new ProductionRun(clashStart, clashDuration);
				clashingRuns.add(clashingRun);
			}
		}

		return clashingRuns;
	}

	public static List<ProductionRun> createSameStart(final long count, final LocalDateTime start,
			final Random random) {
		OfLong durations = random.longs(count, 1, 100).iterator();

		final List<ProductionRun> runs = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			ProductionRun pr = new ProductionRun(start, durations.nextLong());
			runs.add(pr);
		}

		return runs;
	}

	public static List<ProductionRun> createSameEnd(final long count, final LocalDateTime end, final Random random) {
		OfLong durations = random.longs(count, 1, 100).iterator();

		final List<ProductionRun> runs = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			long duration = durations.nextLong();
			ProductionRun pr = new ProductionRun(end.minusDays(duration - 1), duration);
			runs.add(pr);
		}

		return runs;
	}

}
