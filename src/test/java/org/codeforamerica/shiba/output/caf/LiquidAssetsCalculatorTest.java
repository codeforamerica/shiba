package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class LiquidAssetsCalculatorTest {

	private LiquidAssetsCalculator liquidAssetsCalculator = new LiquidAssetsCalculator();
	private TestApplicationDataBuilder applicationDataBuilder;

	@Test
	void liquidAssetsHouseholdShouldCalculateTo500() {
		applicationDataBuilder = new TestApplicationDataBuilder()
				.withPageData("cashAmount", "cashAmount", List.of("5,00"))
				.withApplicantPrograms(List.of("SNAP", "CERTAIN_POPS"));
		ApplicationData applicationData = applicationDataBuilder.build();
		String totalLiquidAssetsString = liquidAssetsCalculator.findTotalLiquidAssets(applicationData);
		assertThat(totalLiquidAssetsString).isEqualTo("500");
	}

	@Test
	void liquidAssetsApplicantShouldCalculateTo300() {
		applicationDataBuilder = new TestApplicationDataBuilder()
				.withPageData("liquidAssetsSingle", "liquidAssets", List.of("300"))
				.withApplicantPrograms(List.of("SNAP", "CERTAIN_POPS"));
		ApplicationData applicationData = applicationDataBuilder.build();
		String totalLiquidAssetsString = liquidAssetsCalculator.findTotalLiquidAssets(applicationData);
		assertThat(totalLiquidAssetsString).isEqualTo("300");
	}
	
	@Test
	void liquidAssetsApplicantShouldCalculateTo3000WithComma() {
		applicationDataBuilder = new TestApplicationDataBuilder()
				.withPageData("liquidAssetsSingle", "liquidAssets", List.of("3,000"))
				.withApplicantPrograms(List.of("SNAP", "CERTAIN_POPS"));
		ApplicationData applicationData = applicationDataBuilder.build();
		String totalLiquidAssetsString = liquidAssetsCalculator.findTotalLiquidAssets(applicationData);
		assertThat(totalLiquidAssetsString).isEqualTo("3000");
	}

}
