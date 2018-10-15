package com.qwic.bike.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qwic.bike.model.ProductionRun;
import com.qwic.bike.util.DateTimeUtils;

@Service
public class PlannerService {

	private static final Logger LOG = LoggerFactory.getLogger(PlannerService.class);

	private final ObjectMapper mapper;

	public PlannerService() {
		this.mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
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

		// remove runs that are less than or equal to the current date time
		runs = runs.stream().filter(r -> r.getStartDateTime().isAfter(currentDateTime)).collect(Collectors.toList());

		// get groups of clashing runs
		final List<SortedSet<ProductionRun>> listsOfClashingRuns = getListsOfClashingRuns(runs);

		// debug print
		for (SortedSet<ProductionRun> clashingRuns : listsOfClashingRuns) {
			LOG.info("{} clashing runs: [{}]", clashingRuns.size(),
					clashingRuns.stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));
		}

		// for each group of clashing runs, remove least number of runs until no clash
		List<ProductionRun> listOfNonClashingRuns = getNonClashingRunsFromListsOfClashingRuns(listsOfClashingRuns);

		LOG.info("Answer: {}. Runs: [{}]", listOfNonClashingRuns.size(),
				listOfNonClashingRuns.stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));

		return listOfNonClashingRuns;

	}

	public List<ProductionRun> parseJsonListOfProductionRuns(final String jsonListOfProductionRuns)
			throws JsonParseException, JsonMappingException, IOException {
		List<ProductionRun> runs = mapper.readValue(jsonListOfProductionRuns, new TypeReference<List<ProductionRun>>() {
		});
		return runs;
	}

	private List<SortedSet<ProductionRun>> getListsOfClashingRuns(List<ProductionRun> runs) {
		// sort by start date, desc
		runs.sort(ProductionRun.COMPARATOR);
		final List<SortedSet<ProductionRun>> answer = new ArrayList<>();

		for (ListIterator<ProductionRun> iterator = runs.listIterator(); iterator.hasNext();) {
			ProductionRun run = iterator.next();

			if (answer.stream().flatMap(SortedSet::stream).collect(Collectors.toList()).contains(run)) {
				// this run has already been processed
				continue;
			}

			TreeSet<ProductionRun> clashes = new TreeSet<>();
			clashes.add(run);

			if (iterator.hasNext()) {
				for (ListIterator<ProductionRun> followingIterator = runs
						.listIterator(iterator.nextIndex()); followingIterator.hasNext();) {
					ProductionRun followingRun = followingIterator.next();

					// check following elements to see if they clash
					// if they do, add to current list
					if (isClash(run, followingRun)) {
						LOG.info("Clash found between \n[{}] and\n[{}]", run, followingRun);
						clashes.add(followingRun);
					} else {
						LOG.info("PR does NOT [{}] clash with [{}]", run, followingRun);
						// if they don't, break?
						// iterator = runs.listIterator(followingIterator.hasNext() ?
						// followingIterator.nextIndex() : runs.indexOf(followingRun));
					}
				}
			}

			answer.add(clashes);
		}

		return answer;
	}

	private List<ProductionRun> getNonClashingRunsFromListsOfClashingRuns(
			List<SortedSet<ProductionRun>> listsOfClashes) {

		List<ProductionRun> answer = new ArrayList<>();

		// run through each group of clashes
		for (SortedSet<ProductionRun> clashes : listsOfClashes) {
			// for each group, get the largest non-clashing combo
			List<ProductionRun> combo = getLargestNonClashingCombo(clashes);
			// add to answer
			answer.addAll(combo);
		}

		return answer;
	}

	private List<ProductionRun> getLargestNonClashingCombo(SortedSet<ProductionRun> clashes) {
		// go from large subsets to small
		for (int subsetSize = clashes.size(); subsetSize > 0; subsetSize--) {
			// generate all combinations for this clash
			IGenerator<List<ProductionRun>> genCombos = Generator.combination(clashes).simple(subsetSize);

			// for each combo, find the first that doesn't clash
			Optional<List<ProductionRun>> findFirst = genCombos.stream().filter(l -> !doRunsClash(l)).findFirst();

			if (findFirst.isPresent()) {
				LOG.info("Found non clashing subset: {}",
						findFirst.get().stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));
				return findFirst.get();
			}
			// else, there's a clash, so try a smaller subset
		}
		LOG.error("Couldn't find non clashing combo!!! This shouldn't be possible. Input: {}",
				clashes.stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));
		return new ArrayList<>();
	}

	private boolean doRunsClash(List<ProductionRun> runs) {
		List<SortedSet<ProductionRun>> listsOfClashingRuns = getListsOfClashingRuns(runs);
		for (SortedSet<ProductionRun> clashes : listsOfClashingRuns) {
			if (clashes.size() > 1)
				return true;
		}
		return false;
	}

	static public boolean isClash(final ProductionRun aRun, final ProductionRun otherRun) {

		LocalDateTime thisStart = aRun.getStartDateTime();
		LocalDateTime thatStart = otherRun.getStartDateTime();
		LocalDateTime thisEnd = aRun.getEndDateTime();
		LocalDateTime thatEnd = otherRun.getEndDateTime();

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
