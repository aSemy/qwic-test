package com.qwic.bike.api.response;

public class ProductionCycleResponse {
	private long productionCycle;

	public ProductionCycleResponse() {
	}
	
	public ProductionCycleResponse(long productionCycle) {
		this.productionCycle = productionCycle;
	}

	public long getProductionCycle() {
		return productionCycle;
	}

	public void setProductionCycle(long productionCycle) {
		this.productionCycle = productionCycle;
	}
}
