package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME_CCAP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ASSETS;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;

/**
 * Basing this on class UnearnedIncomeCalculator.java.
 * 
 * @author pwemj35
 *
 */
public class LiquidAssetsCalculator {

	public static final Map<String, String> ASSETS = new HashMap<>();

	static {
		ASSETS.put("SOCIAL_SECURITY", "socialSecurityAmount");
	}

	/**
	 * unearnedAmount method in class UnearnedIncomeCalculator.java only has
	 * applicationData for a method parameter.
	 * 
	 * @param applicationData
	 * @param pageName
	 * @return
	 */
	public BigDecimal totalLiquidAssets(ApplicationData applicationData) {
		return getValues(applicationData.getPagesData(), Field.ASSETS).stream()
				.map(amount -> getLiquidAssetsAmount(amount, "liquidAssets", applicationData.getPagesData()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal getLiquidAssetsAmount(String fieldName, String pageName, PagesData pagesData) {
		String result = pagesData.getPageInputFirstValue(pageName, ASSETS.get(fieldName));
		String formattedResult = result == null || result.isBlank() ? "0" : result.replaceAll("[^\\d.]", "");
		return new BigDecimal(formattedResult);
	}
	
	// Example:
	  public BigDecimal unearnedAmount(ApplicationData applicationData) {
		    return unearnedAmount(applicationData, "unearnedIncomeSources", UNEARNED_INCOME)
		        .add(unearnedAmount(applicationData, "unearnedIncomeSourcesCcap", UNEARNED_INCOME_CCAP));
		  }

		  private BigDecimal unearnedAmount(ApplicationData applicationData, String pageName, Field field) {
		    return getValues(applicationData.getPagesData(), field)
		        .stream()
		        .map(amount -> getUnearnedAmount(amount, pageName, applicationData.getPagesData()))
		        .reduce(BigDecimal.ZERO, BigDecimal::add);
		  }

		  private BigDecimal getUnearnedAmount(String fieldName, String pageName, PagesData pagesData) {
			    String result = pagesData.getPageInputFirstValue(pageName,
			        ASSETS.get(fieldName));
			    String formattedResult =
			        result == null || result.isBlank() ? "0" : result.replaceAll("[^\\d.]", "");
			    return new BigDecimal(formattedResult);
			  }
}
