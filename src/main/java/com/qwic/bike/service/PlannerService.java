package com.qwic.bike.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

	private final ObjectMapper mapper;

	public PlannerService() {
		this.mapper = new ObjectMapper();
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
		LOG.debug("Runs:\n{}",
				listOfNonClashingRuns.stream().map(ProductionRun::toString).collect(Collectors.joining("\n")));

		return listOfNonClashingRuns;

	}

	private List<ProductionRun> removeInvalidRuns(final List<ProductionRun> runs, final LocalDateTime currentDateTime) {

		List<ProductionRun> validRuns = runs.stream().filter(

				r ->
				// remove runs that are less than or equal to the current date time
				r.getStartDateTime().isAfter(currentDateTime)
						// remove runs that have invalid duration
						|| r.getDurationDays() <= 0 && r.getDurationDays() >= qwicTestProperties.getMaxRunDuration()

		).collect(Collectors.toList());

		LOG.warn("Removed {} invalid runs.", runs.size() - validRuns.size());

		return validRuns;
	}

	public List<ProductionRun> parseJsonListOfProductionRuns(final String jsonListOfProductionRuns)
			throws JsonParseException, JsonMappingException, IOException {
		List<ProductionRun> runs = mapper.readValue(jsonListOfProductionRuns, new TypeReference<List<ProductionRun>>() {
		});
		return runs;
	}

	private List<List<ProductionRun>> getListsOfClashingRuns(List<ProductionRun> runs) {
		// sort by start date, desc
		runs.sort(ProductionRun.COMPARATOR);
		final List<List<ProductionRun>> answer = new ArrayList<>();

		List<ProductionRun> clashes2 = new ArrayList<>();
		ProductionRun previousRun = null;

		for (Iterator<ProductionRun> iterator = runs.iterator(); iterator.hasNext();) {
			ProductionRun productionRun = iterator.next();

			if (clashes2.isEmpty()) {
				// init first run
				clashes2.add(productionRun);
			} else {
				if (previousRun != null) {
					if (isClash(productionRun, previousRun)) {
						clashes2.add(productionRun);
					} else {
						// store this group of clashes
						answer.add(clashes2);
						// continue with a new group of clashes
						previousRun = null;
						clashes2 = new ArrayList<>();
						clashes2.add(productionRun);
					}
				}
			}

			previousRun = productionRun;
		}
		answer.add(clashes2);

//		for (ListIterator<ProductionRun> iterator = runs.listIterator(); iterator.hasNext();) {
//			LOG.info("Current index: {}", iterator.nextIndex());
//			ProductionRun run = iterator.next();
//
//			// if this run has already been processed, don't check for further clashes
//			if (answer.stream().filter(clashes -> clashes.contains(run)).findFirst().isPresent())
//				continue;
//
//			List<ProductionRun> clashes = new ArrayList<>();
//			clashes.add(run);
//
//			if (iterator.hasNext()) {
//				followingLoop: for (ListIterator<ProductionRun> followingIterator = runs
//						.listIterator(iterator.nextIndex()); followingIterator.hasNext();) {
//					LOG.info("     following index: {}", followingIterator.nextIndex());
//					ProductionRun followingRun = followingIterator.next();
//
//					// check following elements to see if they clash
//					// if they do, add to current list
//					if (isClash(run, followingRun)) {
//						LOG.info("    \nClash found between \n[{}] and\n[{}]", run, followingRun);
//						clashes.add(followingRun);
//					} else {
//						LOG.debug("    PR does NOT [{}] clash with [{}]", run, followingRun);
//
//						// iterator = runs.listIterator(followingIterator.nextIndex() - clashes.size());
//
//						// as the runs are ordered, if one run doesn't clash the following won't either
//						// so break this loop
//						// break followingLoop;
//						// iterator = runs.listIterator(followingIterator.hasNext() ?
//						// followingIterator.nextIndex() : runs.indexOf(followingRun));
//					}
//				}
//			}
//
//			clashes.sort(ProductionRun.COMPARATOR);
//			answer.add(clashes);
//		}

		return answer;
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

	private List<ProductionRun> getLargestNonClashingCombo(List<ProductionRun> listOfClashingRuns) {

		Collections.sort(listOfClashingRuns);

		List<ProductionRun> largestNonClashingRuns = new ArrayList<>();

		for (ProductionRun currentRun : listOfClashingRuns) {
			if (largestNonClashingRuns.stream().noneMatch(previousValidRun -> isClash(previousValidRun, currentRun))) {
				largestNonClashingRuns.add(currentRun);
			}
		}

		return largestNonClashingRuns;

//		// go from large subsets to small
//		for (int subsetSize = listOfClashingRuns.size(); subsetSize > 0; subsetSize--) {
//			// generate all combinations for this clash
//			IGenerator<List<ProductionRun>> genCombos = Generator.combination(listOfClashingRuns).simple(subsetSize);
//
//			// for each combo, find the first that doesn't clash
//			Optional<List<ProductionRun>> findFirst = genCombos.stream().filter(l -> !doRunsClash(l)).findFirst();
//
//			if (findFirst.isPresent()) {
//				LOG.info("Found non clashing subset: {}",
//						findFirst.get().stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));
//				return findFirst.get();
//			}
//			// else, there's a clash, so try a smaller subset
//		}
//		LOG.error("Couldn't find non clashing combo!!! This shouldn't be possible. Input: {}",
//				listOfClashingRuns.stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));
//		return new ArrayList<>();
	}

//	private boolean doRunsClash(List<ProductionRun> runs) {
//		List<List<ProductionRun>> listsOfClashingRuns = getListsOfClashingRuns(runs);
//		for (List<ProductionRun> clashes : listsOfClashingRuns) {
//			if (clashes.size() > 1)
//				return true;
//		}
//		return false;
//	}

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
