package com.qwic.bike;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.qwic.bike.model.ProductionRun;
import com.qwic.bike.service.PlannerService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ParseTest {

	@Autowired
	private PlannerService plannerService;

	@Test
	public void testParseJsonListOfProductionRuns1() throws JsonParseException, JsonMappingException, IOException {

		// create expected results
		final Map<LocalDateTime, List<Long>> mapOfExpectedDatesToDurations = new HashMap<>();
		mapOfExpectedDatesToDurations.put(LocalDateTime.of(2018, 1, 2, 0, 0), Arrays.asList(5l));
		mapOfExpectedDatesToDurations.put(LocalDateTime.of(2018, 1, 9, 0, 0), Arrays.asList(7l, 3l));
		mapOfExpectedDatesToDurations.put(LocalDateTime.of(2018, 1, 15, 0, 0), Arrays.asList(6l));

		// json input to be tested
		final String input = "[ " //
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

		// test method
		final List<ProductionRun> runs = plannerService.parseJsonListOfProductionRuns(input);

		// test output
		assertNotNull(runs);
		assertEquals(mapOfExpectedDatesToDurations.values().stream().mapToInt(List::size).sum(), runs.size());

		// convert output to map dates to durations
		Map<LocalDateTime, List<ProductionRun>> mapOfOutputStartDateToProductionRuns = runs.stream()
				.collect(Collectors.groupingBy(ProductionRun::getStartDateTime));

		// should have same number of keys
		assertEquals(mapOfExpectedDatesToDurations.keySet().size(),
				mapOfOutputStartDateToProductionRuns.keySet().size());

		for (LocalDateTime expectedDate : mapOfExpectedDatesToDurations.keySet()) {
			final List<Long> value = mapOfExpectedDatesToDurations.get(expectedDate);

			final String expectedDateFormattedString = expectedDate.format(DateTimeFormatter.ISO_DATE);

			assertTrue("Parsed runs contains " + expectedDateFormattedString,
					mapOfOutputStartDateToProductionRuns.containsKey(expectedDate));

			assertEquals("Correct number of runs for " + expectedDateFormattedString, value.size(),
					mapOfOutputStartDateToProductionRuns.get(expectedDate).size());

			assertTrue("Correct duration" + (value.size() > 1 ? "s" : "") + " for " + expectedDateFormattedString,
					value.containsAll(mapOfOutputStartDateToProductionRuns.get(expectedDate).stream()
							.map(ProductionRun::getDurationDays).collect(Collectors.toList())));
		}
	}
}
