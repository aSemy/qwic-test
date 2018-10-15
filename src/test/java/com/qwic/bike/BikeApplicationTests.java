package com.qwic.bike;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

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
public class BikeApplicationTests {

	@Autowired
	private PlannerService plannerService;

	@Test
	public void contextLoads() {
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

		List<ProductionRun> answer = plannerService.maximiseNonClashingRuns(inputJson);
		
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

		List<ProductionRun> answer = plannerService.maximiseNonClashingRuns(inputJson);
		
		assertEquals(4, answer.size());
	}
}
