package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;

class LiquidAssetsPreparerTest {

  private final LiquidAssetsPreparer liquidAssetsPreparer = new LiquidAssetsPreparer();

  @Test
  void returnsZeroLiquidAssetsOnBlank() {
    ApplicationData appData = new ApplicationData();
    PagesData pagesData =
        new PagesDataBuilder()
            .withPageData("liquidAssetsSingle", Map.of("liquidAssets", "")).build();
    appData.setPagesData(pagesData);
    Application application = Application.builder().applicationData(appData).build();


    assertThat(liquidAssetsPreparer.prepareDocumentFields(application, null, Recipient.CLIENT))
        .isEqualTo(List.of(new DocumentField("liquidAssets", "liquidAssetsValue", List.of("0.00"),
            DocumentFieldType.SINGLE_VALUE)));
  }
  
  
  @Test
  void returnsValueLiquidAssets() {
    ApplicationData appData = new ApplicationData();
    PagesData pagesData =
        new PagesDataBuilder()
            .withPageData("liquidAssetsSingle", Map.of("liquidAssets", "100"))
            .withPageData("cashAmount", Map.of("cashAmount","200")).build();
    appData.setPagesData(pagesData);
    Application application = Application.builder().applicationData(appData).build();


    assertThat(liquidAssetsPreparer.prepareDocumentFields(application, null, Recipient.CLIENT))
        .isEqualTo(List.of(new DocumentField("liquidAssets", "liquidAssetsValue", List.of("300.00"),
            DocumentFieldType.SINGLE_VALUE)));
  }
}
