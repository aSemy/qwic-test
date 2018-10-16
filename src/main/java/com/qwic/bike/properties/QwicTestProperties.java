package com.qwic.bike.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qwic-test-props")
public class QwicTestProperties {

	/**
	 * Default: 100000
	 */
	private long maxRunDuration = 100000;
	/**
	 * Default: 1000
	 */
	private long maxQuantityOfRuns = 1000;

	public long getMaxRunDuration() {
		return maxRunDuration;
	}

	public long getMaxQuantityOfRuns() {
		return maxQuantityOfRuns;
	}

	public void setMaxRunDuration(long maxRunDuration) {
		this.maxRunDuration = maxRunDuration;
	}

	public void setMaxQuantityOfRuns(long maxQuantityOfRuns) {
		this.maxQuantityOfRuns = maxQuantityOfRuns;
	}
}
