package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.math.BigDecimal;

import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class LiquidAssetsCalculator {

	public String findTotalLiquidAssets(ApplicationData applicationData) {
		 BigDecimal applicantAssets =   getValues(applicationData.getPagesData(), Field.APPLICANT_ASSETS).stream()
				 .filter(value -> ! value.isBlank())
				 .map(value -> new BigDecimal(value.replace(",", "")))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		 BigDecimal houseHoldAssets =   getValues(applicationData.getPagesData(), Field.HOUSEHOLD_ASSETS).stream()
				 .filter(value -> ! value.isBlank())
				 .map(value -> new BigDecimal(value.replace(",", "")))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		 BigDecimal result = applicantAssets.add(houseHoldAssets);
		return  result.toPlainString();
	}

}
