package com.qwic.bike.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qwic.bike.model.ProductionRun;

@Service
public class PlannerService {

	private final ObjectMapper mapper;

	public PlannerService() {
		this.mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
	}

	public List<ProductionRun> parseJsonListOfProductionRuns(final String jsonListOfProductionRuns)
			throws JsonParseException, JsonMappingException, IOException {
		List<ProductionRun> runs = mapper.readValue(jsonListOfProductionRuns, new TypeReference<List<ProductionRun>>() {
		});
		return runs;
	}
}
