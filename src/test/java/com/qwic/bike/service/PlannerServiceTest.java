package com.qwic.bike.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.qwic.bike.TestUtil;
import com.qwic.bike.model.ProductionRun;
import com.qwic.bike.properties.QwicTestProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PlannerServiceTest {

	@Autowired
	private PlannerService plannerService;

	private SecureRandom random;

	@Autowired
	private QwicTestProperties qtProps;

	// so that the tests always run, assume that the current date is earlier than
	// the data.
	private static final LocalDateTime validCurrentDate = LocalDateTime.of(2018, 1, 1, 0, 0);

	@Before
	public void beforeEachTest() {
		this.random = new SecureRandom();
	}

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

	@Test
	public void testClash_SecondEndInsideFirst() {

		ProductionRun first = new ProductionRun(LocalDateTime.of(2018, 1, 15, 0, 0), 10);
		ProductionRun second = new ProductionRun(LocalDateTime.of(2018, 1, 10, 0, 0), 8);

		// check conditions are set correctly
		assert second.getStartDateTime().isBefore(first.getStartDateTime())
				&& second.getEndDateTime().isAfter(first.getStartDateTime())
				&& second.getEndDateTime().isBefore(first.getEndDateTime());

		assertTrue(PlannerService.isClash(first, second));
		// inverse should be the same
		assertTrue(PlannerService.isClash(second, first));
	}

	@Test
	public void test1() throws JsonParseException, JsonMappingException, IOException {
		// json input to be tested
		final String inputJson = "[ " //
				+ "{ " //
				+ "\"startingDay\": \"2018-01-02T00:00:00.000Z\", \"duration\": 5 " //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-09T00:00:00.000Z\", \"duration\": 7" //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-15T00:00:00.000Z\", \"duration\": 6" //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-09T00:00:00.000Z\", \"duration\": 3" //
				+ "}" //
				+ "]";

		List<ProductionRun> answer = plannerService.maximiseNonClashingRuns(inputJson, validCurrentDate);

		assertEquals(3, answer.size());
	}

	@Test
	public void test2() throws JsonParseException, JsonMappingException, IOException {
		// json input to be tested
		final String inputJson = "[ " //
				+ "{ " //
				+ "\"startingDay\": \"2018-01-03T00:00:00.000Z\", \"duration\": 5 " //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-09T00:00:00.000Z\", \"duration\": 2" //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-24T00:00:00.000Z\", \"duration\": 5" //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-16T00:00:00.000Z\", \"duration\": 9" //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-11T00:00:00.000Z\", \"duration\": 6" //
				+ "}" //
				+ "]";

		List<ProductionRun> answer = plannerService.maximiseNonClashingRuns(inputJson, validCurrentDate);

		assertEquals(4, answer.size());
	}

	@Test
	public void testStartDaysInPast() throws JsonParseException, JsonMappingException, IOException {

		final LocalDateTime currentDateAfterInputStartDates = LocalDateTime.of(2018, 1, 15, 0, 1);

		// json input to be tested
		final String inputJson = "[ " //
				+ "{ " //
				+ "\"startingDay\": \"2018-01-02T00:00:00.000Z\", \"duration\": 5 " //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-09T00:00:00.000Z\", \"duration\": 7" //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-15T00:00:00.000Z\", \"duration\": 6" //
				+ "}," //
				+ "{" //
				+ "\"startingDay\": \"2018-01-09T00:00:00.000Z\", \"duration\": 3" //
				+ "}" //
				+ "]";

		List<ProductionRun> answer = plannerService.maximiseNonClashingRuns(inputJson, currentDateAfterInputStartDates);

		assertEquals(0, answer.size());
	}

	@Test
	public void testNoClash_30runs() {

		LocalDateTime now = LocalDateTime.of(2018, 10, 10, 10, 10);

		List<ProductionRun> runs = TestUtil.createNonClashingRuns(30, now.plusDays(1));
		// make our test work harder! Shuffle the list
		Collections.shuffle(runs, random);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runs, now);

		// should have the same number
		assertEquals("Expected 30 runs, not " + maximisedRuns.size(), runs.size(), maximisedRuns.size());

		// should have no duplicates
		assertEquals(maximisedRuns.size(), new HashSet<ProductionRun>(maximisedRuns).size());

		// no clashes should be found
		for (ProductionRun run : runs) {
			assertTrue(maximisedRuns.contains(run));
		}
	}

	@Test
	public void testNoClash_MaxMinus1runs() throws InterruptedException {
		// try running almost the max number of runs

		LocalDateTime now = LocalDateTime.of(2018, 10, 10, 10, 10);

		List<ProductionRun> runs = TestUtil.createNonClashingRuns(qtProps.getMaxQuantityOfRuns() - 1, now.plusDays(1));
		// make our test work harder! Shuffle the list
		Collections.shuffle(runs, random);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runs, now);

		// should have the same number
		assertEquals(runs.size(), maximisedRuns.size());

		// should have no duplicates
		assertEquals(maximisedRuns.size(), new HashSet<ProductionRun>(maximisedRuns).size());

		// all runs should be found
		assertTrue(maximisedRuns.containsAll(runs));
	}

	@Test(expected = AssertionError.class)
	public void testTooManyRuns() {
		// too many runs! expect failure

		@SuppressWarnings("unchecked")
		List<ProductionRun> runs = mock(List.class);
		when(runs.size()).thenReturn((int) qtProps.getMaxQuantityOfRuns());

		plannerService.maximiseNonClashingRuns(runs, validCurrentDate);
	}

	@Test
	public void testAdjacentClashes() {

		LocalDateTime now = LocalDateTime.of(2018, 10, 10, 10, 10);

		List<ProductionRun> runs = TestUtil.createNonClashingRuns(50, now.plusDays(1));
		List<ProductionRun> clashes = TestUtil.createAdjacentClashes(runs);

		assert runs.size() - 1 == clashes.size();

		List<ProductionRun> runsToTest = new ArrayList<>();
		runsToTest.addAll(runs);
		runsToTest.addAll(clashes);
		// make our test work harder! Shuffle the list
		Collections.shuffle(runsToTest, random);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runsToTest, now);

		// should have the same number
		assertEquals(runs.size(), maximisedRuns.size());

		// no clashes should be found
		for (ProductionRun clash : clashes) {
			assertFalse(maximisedRuns.contains(clash));
		}
		// all runs should be found
		maximisedRuns.containsAll(runs);
	}

	@Test
	public void testAdjacentClashes2() {

		LocalDateTime now = LocalDateTime.of(2018, 10, 10, 10, 10);

		List<ProductionRun> runs = TestUtil.createNonClashingRuns(200, now.plusDays(1));
		List<ProductionRun> clashes1 = TestUtil.createAdjacentClashes(runs);
		List<ProductionRun> clashes2 = TestUtil.createAdjacentClashes(clashes1);

		assert runs.size() - 1 == clashes1.size();
		assert clashes1.size() - 1 == clashes2.size();

		List<ProductionRun> runsToTest = new ArrayList<>();
		runsToTest.addAll(runs);
		runsToTest.addAll(clashes1);
		runsToTest.addAll(clashes2);
		// make our test work harder! Shuffle the list
		Collections.shuffle(runsToTest, random);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runsToTest, now);

		// should have the same number
		assertEquals(runs.size(), maximisedRuns.size());
	}

	/**
	 * A clashes with B, B clashes with C, C clashes with D
	 */
	@Test
	public void test4ChainedClashes() {

		LocalDateTime now = LocalDateTime.of(2018, 01, 01, 0, 0);

		ProductionRun a = new ProductionRun(now, 5);
		ProductionRun b = new ProductionRun(a.getEndDateTime().minusDays(1), 5);
		ProductionRun c = new ProductionRun(b.getEndDateTime().minusDays(1), 5);
		ProductionRun d = new ProductionRun(c.getEndDateTime().minusDays(1), 5);

		List<ProductionRun> runsToTest = Arrays.asList(a, b, c, d);

		// check conditions are right
		assert PlannerService.isClash(a, b);
		assert PlannerService.isClash(b, c);
		assert PlannerService.isClash(c, d);

		assert !PlannerService.isClash(a, c);
		assert !PlannerService.isClash(a, d);
		assert !PlannerService.isClash(b, d);

		// make our test work harder! Shuffle the list
		Collections.shuffle(runsToTest, random);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runsToTest, now.minusDays(5));

		assertEquals(2, maximisedRuns.size());

		// there are 3 valid solutions, so this is a bit messy
		assertTrue((
		// Solution A and C
		maximisedRuns.containsAll(Arrays.asList(a, c)) && !maximisedRuns.contains(b) && !maximisedRuns.contains(d))
				// Solution A and D
				|| (maximisedRuns.containsAll(Arrays.asList(a, d)) && !maximisedRuns.contains(b)
						&& !maximisedRuns.contains(c))
				// Solution B and D
				|| (maximisedRuns.containsAll(Arrays.asList(b, d)) && !maximisedRuns.contains(a)
						&& !maximisedRuns.contains(c)));

	}

	/**
	 * A clashes with B, B clashes with C, C clashes with D
	 */
	@Test
	public void test5ChainedClashes() {

		LocalDateTime now = LocalDateTime.of(2018, 01, 01, 0, 0);

		ProductionRun a = new ProductionRun(now, 5);
		ProductionRun b = new ProductionRun(a.getEndDateTime().minusDays(1), 5);
		ProductionRun c = new ProductionRun(b.getEndDateTime().minusDays(1), 5);
		ProductionRun d = new ProductionRun(c.getEndDateTime().minusDays(1), 5);
		ProductionRun e = new ProductionRun(d.getEndDateTime().minusDays(1), 5);

		List<ProductionRun> runsToTest = Arrays.asList(a, b, c, d, e);

		// make our test work harder! Shuffle the list
		Collections.shuffle(runsToTest, random);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runsToTest, now.minusDays(5));

		assertEquals(3, maximisedRuns.size());

		// only one maximised solution
		assertTrue(maximisedRuns.containsAll(Arrays.asList(a, c, e)));
		assertFalse(maximisedRuns.contains(b));
		assertFalse(maximisedRuns.contains(d));
	}

	@Test
	public void testSameStartClash() {

		List<ProductionRun> sameStarts = TestUtil.createSameStart(100, validCurrentDate, random);

		// assert initial condition is correct
		for (ProductionRun run : sameStarts) {
			assert run.getStartDateTime().isEqual(validCurrentDate);
		}

		LocalDateTime latestEnd = validCurrentDate.plusDays(
				sameStarts.stream().mapToLong(r -> r.getDurationDays()).max().orElseThrow(NoSuchElementException::new)
						+ 5);
		List<ProductionRun> nonClashingRuns = TestUtil.createNonClashingRuns(5, latestEnd);

		List<ProductionRun> runsToTest = new ArrayList<>();
		runsToTest.addAll(sameStarts);
		runsToTest.addAll(nonClashingRuns);
		// make our test work harder! Shuffle the list
		Collections.shuffle(runsToTest, random);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runsToTest,
				validCurrentDate.minusDays(5));

		// should have the same number
		assertEquals(nonClashingRuns.size() + 1, maximisedRuns.size());

		// only one of the original 'sameStarts' should be found
		Set<ProductionRun> intersection = new HashSet<>(sameStarts);
		intersection.retainAll(maximisedRuns);
		assertEquals(1, intersection.size());
		// all runs should be found
		maximisedRuns.containsAll(nonClashingRuns);
	}

	@Test
	public void testSameEndClash() {

		List<ProductionRun> sameEnds = TestUtil.createSameEnd(100, validCurrentDate, random);

		for (ProductionRun run : sameEnds) {
			assert run.getEndDateTime().isEqual(validCurrentDate) : "End date is "
					+ run.getEndDateTime().format(DateTimeFormatter.ISO_DATE) + " but should be "
					+ validCurrentDate.format(DateTimeFormatter.ISO_DATE);
		}

		List<ProductionRun> nonClashingRuns = TestUtil.createNonClashingRuns(5, validCurrentDate.plusDays(5));

		List<ProductionRun> runsToTest = new ArrayList<>();
		runsToTest.addAll(sameEnds);
		runsToTest.addAll(nonClashingRuns);
		// make our test work harder! Shuffle the list
		Collections.shuffle(runsToTest, random);

		// get the earliest start date
		LocalDateTime earliestStart = runsToTest.stream().map(r -> r.getStartDateTime())
				.min(Comparator.comparing(LocalDateTime::toLocalDate)).orElseThrow(NoSuchElementException::new)
				.minusDays(5);

		List<ProductionRun> maximisedRuns = plannerService.maximiseNonClashingRuns(runsToTest, earliestStart);

		// should have the same number
		assertEquals(nonClashingRuns.size() + 1, maximisedRuns.size());

		// only one of the original 'sameEnds' should be found
		sameEnds.retainAll(maximisedRuns);
		assertEquals(1, sameEnds.size());

		// all runs should be found
		maximisedRuns.containsAll(nonClashingRuns);
	}

}
