package com.qwic.bike.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qwic.bike.model.ProductionRun;
import com.qwic.bike.properties.QwicTestProperties;
import com.qwic.bike.util.DateTimeUtils;

@Service
public class PlannerService {

	private static final Logger LOG = LoggerFactory.getLogger(PlannerService.class);

	@Autowired
	private QwicTestProperties qwicTestProperties;

	/**
	 * Json mapper
	 */
	private final ObjectMapper mapper;

	public PlannerService() {
		this.mapper = new ObjectMapper();
		// register modules, for LocalDateTime parsing
		mapper.findAndRegisterModules();
	}

	public List<ProductionRun> maximiseNonClashingRuns(final String jsonInput)
			throws JsonParseException, JsonMappingException, IOException {
		return maximiseNonClashingRuns(jsonInput, LocalDateTime.now());
	}

	public List<ProductionRun> maximiseNonClashingRuns(List<ProductionRun> runs) {
		return maximiseNonClashingRuns(runs, LocalDateTime.now());
	}

	public List<ProductionRun> maximiseNonClashingRuns(final String jsonInput, final LocalDateTime currentDateTime)
			throws JsonParseException, JsonMappingException, IOException {
		List<ProductionRun> runs = parseJsonListOfProductionRuns(jsonInput);

		return maximiseNonClashingRuns(runs, currentDateTime);
	}

	/**
	 * Get the maximum amount of non-clashing runs.
	 * 
	 * Runs with start dates after currentDateTime are removed and not processed.
	 * 
	 * @param runs
	 * @param currentDateTime
	 * @return
	 */
	public List<ProductionRun> maximiseNonClashingRuns(List<ProductionRun> runs, final LocalDateTime currentDateTime) {

		assert runs.size() < qwicTestProperties.getMaxQuantityOfRuns();

		// remove invalid runs
		runs = removeInvalidRuns(runs, currentDateTime);

		// get groups of clashing runs
		final List<List<ProductionRun>> listsOfClashingRuns = getListsOfClashingRuns(runs);

		// debug print
		for (List<ProductionRun> clashingRuns : listsOfClashingRuns) {
			LOG.info("{} clashing runs: [{}]", clashingRuns.size(),
					clashingRuns.stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));
		}

		// for each group of clashing runs, remove least number of runs until no clash
		List<ProductionRun> listOfNonClashingRuns = getNonClashingRunsFromListsOfClashingRuns(listsOfClashingRuns);

		LOG.info("Answer: {}", listOfNonClashingRuns.size());
		LOG.trace("Runs:\n{}",
				listOfNonClashingRuns.stream().map(ProductionRun::toString).collect(Collectors.joining("\n")));

		return listOfNonClashingRuns;

	}

	private List<ProductionRun> removeInvalidRuns(final List<ProductionRun> runs, final LocalDateTime currentDateTime) {

		List<ProductionRun> validRuns = runs.stream().filter(r -> isRunValid(r, currentDateTime))
				.collect(Collectors.toList());

		LOG.warn("Removed {} invalid runs.", runs.size() - validRuns.size());

		return validRuns;
	}

	private boolean isRunValid(final ProductionRun run, final LocalDateTime currentDateTime) {
		return
		// remove runs that are less than or equal to the current date time
		run.getStartDateTime().isAfter(currentDateTime)
				// remove runs that have invalid duration
				|| run.getDurationDays() <= 0 && run.getDurationDays() >= qwicTestProperties.getMaxRunDuration();
	}

	public List<ProductionRun> parseJsonListOfProductionRuns(final String jsonListOfProductionRuns)
			throws JsonParseException, JsonMappingException, IOException {
		List<ProductionRun> runs = mapper.readValue(jsonListOfProductionRuns, new TypeReference<List<ProductionRun>>() {
		});
		return runs;
	}

	/**
	 * Given a list of runs, divide it up into chunks of clashing runs.
	 * 
	 * For example, if A only clashes with B, and B only clashes with C, and D
	 * clashes with nothing, and E only clashes with F the result will be
	 * <code>[[A, B, C], [D], [E, F]]</code>
	 * 
	 * @param runs
	 * @return
	 */
	private List<List<ProductionRun>> getListsOfClashingRuns(List<ProductionRun> runs) {
		// sort by start date, desc
		runs.sort(ProductionRun.COMPARATOR);
		final List<List<ProductionRun>> distinctListsOfClashes = new ArrayList<>();

		List<ProductionRun> clashingRuns = new ArrayList<>();
		ProductionRun previousRun = null;

		for (final ProductionRun productionRun : runs) {
			if (clashingRuns.isEmpty()) {
				// init first run
				clashingRuns.add(productionRun);
			} else {
				if (previousRun != null) {
					if (isClash(productionRun, previousRun)) {
						clashingRuns.add(productionRun);
					} else {
						// store this group of clashes
						distinctListsOfClashes.add(clashingRuns);
						// continue with a new group of clashes
						previousRun = null;
						clashingRuns = new ArrayList<>();
						clashingRuns.add(productionRun);
					}
				}
			}
			// set the previous run
			previousRun = productionRun;
		}
		// add the final list of clashes
		distinctListsOfClashes.add(clashingRuns);

		return distinctListsOfClashes;
	}

	private List<ProductionRun> getNonClashingRunsFromListsOfClashingRuns(List<List<ProductionRun>> listsOfClashes) {

		List<ProductionRun> answer = new ArrayList<>();

		// run through each group of clashes
		for (List<ProductionRun> clashes : listsOfClashes) {
			// for each group, get the largest non-clashing combo
			List<ProductionRun> combo = getLargestNonClashingCombo(clashes);
			// add to answer
			answer.addAll(combo);
		}

		return answer;
	}

	/**
	 * Greedy algorithm. Sort by end time, ascending. Iterate over runs. If it
	 * doesn't clash with 'answer' list, then add it to 'answer' list.
	 * 
	 * @param listOfClashingRuns
	 * @return
	 */
	private List<ProductionRun> getLargestNonClashingCombo(List<ProductionRun> listOfClashingRuns) {

		Collections.sort(listOfClashingRuns);

		List<ProductionRun> largestNonClashingRuns = new ArrayList<>();

		for (ProductionRun currentRun : listOfClashingRuns) {
			// if this run doesn't clash with list of non-clashing runs
			if (largestNonClashingRuns.stream().noneMatch(previousValidRun -> isClash(previousValidRun, currentRun))) {
				// then add it
				largestNonClashingRuns.add(currentRun);
			}
		}

		return largestNonClashingRuns;
	}

	static public boolean isClash(final ProductionRun thisRun, final ProductionRun thatRun) {

		LocalDateTime thisStart = thisRun.getStartDateTime();
		LocalDateTime thatStart = thatRun.getStartDateTime();
		LocalDateTime thisEnd = thisRun.getEndDateTime();
		LocalDateTime thatEnd = thatRun.getEndDateTime();

		// if this' start is between the other's start/end
		if (DateTimeUtils.isDateTimeInRange(thisStart, thatStart, thatEnd))
			return true;
		// if this' end is between the other's start/end
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

}
