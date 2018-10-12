package com.qwic.bike;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	public void test() throws JsonParseException, JsonMappingException, IOException {
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

		plannerService.test(input);
	}
}
