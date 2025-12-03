package Calculation;

import java.util.Map;

public class CarbonCalculator {
	private final Map<String, Double> co2eFactors;
	
	public CarbonCalculator(Map<String, Double> co2eFactors) {
		this.co2eFactors = co2eFactors;
	}
	
	public double CalculateActionCarbon(String actionName, double count, String tabTitle) {
		double factor = co2eFactors.getOrDefault(actionName, 0.0);
		
		if (factor == 0.0) {
			return 0.0;
		}
		
		double adCount = count;
		
		if ("쓰레기".equals(tabTitle)) {
			adCount = count / 100.0;
		}
		
		return adCount * factor;
	}
}