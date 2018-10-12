package com.qwic.bike.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qwic.bike.model.ProductionRun;

@Service
public class PlannerService {

	private static final Logger LOG = LoggerFactory.getLogger(PlannerService.class);

	private final ObjectMapper mapper;

	public PlannerService() {
		this.mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
	}

	public void test(final String input) throws JsonParseException, JsonMappingException, IOException {
		List<ProductionRun> runs = parseJsonListOfProductionRuns(input);

		d(runs);
	}

	public List<ProductionRun> parseJsonListOfProductionRuns(final String jsonListOfProductionRuns)
			throws JsonParseException, JsonMappingException, IOException {
		List<ProductionRun> runs = mapper.readValue(jsonListOfProductionRuns, new TypeReference<List<ProductionRun>>() {
		});
		return runs;
	}

	public void d(List<ProductionRun> runs) {

		// get groups of clashing runs
		final List<SortedSet<ProductionRun>> answer = getListsOfClashingRuns(runs);

		for (SortedSet<ProductionRun> clashingRuns : answer) {
			LOG.info("{} clashing runs: [{}]", clashingRuns.size(),
					clashingRuns.stream().map(ProductionRun::toString).collect(Collectors.joining(", ")));
		}

		// for each group of clashing runs, remove least number of runs until no clash

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
					if (run.isClash(followingRun)) {
						clashes.add(followingRun);
					} else {
						// if they don't, break?
						//iterator = runs.listIterator(followingIterator.hasNext() ? followingIterator.nextIndex() : followingIterator.previousIndex());
					}
				}
			}

			answer.add(clashes);
		}

		return answer;
	}
}
