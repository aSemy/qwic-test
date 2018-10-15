package com.qwic.bike.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.qwic.bike.model.ProductionRun;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PlannerServiceTest {

	@Test
	public void testNoClash() {

		final long runDuration = 3;
		final long daysSpacing = 1;

		ProductionRun first = new ProductionRun(LocalDateTime.of(2018, 1, 1, 0, 0), runDuration);
		ProductionRun second = new ProductionRun(first.getEndDateTime().plusDays(daysSpacing), runDuration);

		// check conditions are set correctly
		assert first.getEndDateTime().isBefore(second.getStartDateTime());

		assertFalse(PlannerService.isClash(first, second));
		// inverse should be the same
		assertFalse(PlannerService.isClash(second, first));
	}

	@Test
	public void testClashSameStart() {

		final long runDuration = 3;
		final long daysSpacing = 0;

		ProductionRun first = new ProductionRun(LocalDateTime.of(2018, 1, 1, 0, 0), runDuration);
		ProductionRun second = new ProductionRun(first.getEndDateTime().plusDays(daysSpacing), runDuration);

		// check conditions are set correctly
		assert first.getEndDateTime().isEqual(second.getStartDateTime());

		assertTrue(PlannerService.isClash(first, second));
		// inverse should be the same
		assertTrue(PlannerService.isClash(second, first));
	}

	@Test
	public void testClash_FirstEndIsAfterSecondStart() {

		final long runDuration = 3;
		final long daysSpacing = -1;

		ProductionRun first = new ProductionRun(LocalDateTime.of(2018, 1, 1, 0, 0), runDuration);
		ProductionRun second = new ProductionRun(first.getEndDateTime().plusDays(daysSpacing), runDuration);

		// check conditions are set correctly
		assert first.getEndDateTime().isAfter(second.getStartDateTime());

		assertTrue(PlannerService.isClash(first, second));
		// inverse should be the same
		assertTrue(PlannerService.isClash(second, first));
	}

	@Test
	public void testClash_SecondInsideFirst() {

		final long runDuration = 5;

		ProductionRun first = new ProductionRun(LocalDateTime.of(2018, 1, 1, 0, 0), runDuration);
		ProductionRun second = new ProductionRun(first.getStartDateTime().plusDays(1), 1);

		// check conditions are set correctly
		assert first.getStartDateTime().isBefore(second.getStartDateTime())
				&& first.getEndDateTime().isAfter(second.getEndDateTime());

		assertTrue(PlannerService.isClash(first, second));
		// inverse should be the same
		assertTrue(PlannerService.isClash(second, first));
	}

}
