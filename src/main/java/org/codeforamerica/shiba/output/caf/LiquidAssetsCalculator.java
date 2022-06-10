package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class LiquidAssetsCalculator {

	public BigDecimal totalLiquidAssets(ApplicationData applicationData) {
		return getValues(applicationData.getPagesData(), Field.ASSETS).stream()
				.map(amount -> getLiquidAssetsAmount(amount, "liquidAssets", applicationData.getPagesData()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	public String findTotalLiquidAssets(ApplicationData applicationData) {
		 BigDecimal result =   getValues(applicationData.getPagesData(), Field.ASSETS).stream()
				 .map(x -> new BigDecimal(x))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return  result.toPlainString();
	}
	
	private BigDecimal getLiquidAssetsAmount(String fieldName, String pageName, PagesData pagesData) {
		String result = pagesData.getPageInputFirstValue(pageName, "liquidAssets");
		List<String> results = pagesData.safeGetPageInputValue(pageName, "liquidAssets");
		System.out.println("results = " + results);
		String formattedResult = result == null || result.isBlank() ? "0" : result.replaceAll("[^\\d.]", "");
		return new BigDecimal(formattedResult);
	}
}
