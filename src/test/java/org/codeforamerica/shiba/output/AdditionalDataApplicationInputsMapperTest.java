package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdditionalDataApplicationInputsMapperTest {

    @Test
    void shouldProduceAnApplicationInputForAnAdditionalDatum() {
        String pageName = "somePageName";
        String additionalDatumName = "someAdditionalDatumName";

        PagesConfiguration pagesConfiguration = new PagesConfiguration();
        PageConfiguration pageConfiguration = new PageConfiguration();
        AdditionalDatum additionalDatum = new AdditionalDatum();
        additionalDatum.setName(additionalDatumName);
        pageConfiguration.setAdditionalData(List.of(additionalDatum));
        pagesConfiguration.getPages().put(pageName, pageConfiguration);

        PagesData pagesData = new PagesData();
        List<String> additionalDatumValue = List.of("someValue");
        pagesData.putPage(pageName, new FormData(Map.of("someAdditionalDatumName", new InputData(additionalDatumValue))));

        AdditionalDataApplicationInputsMapper applicationInputsMapper =
                new AdditionalDataApplicationInputsMapper(pagesConfiguration);

        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(pagesData);

        List<ApplicationInput> applicationInputs = applicationInputsMapper.map(applicationData);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        pageName,
                        additionalDatumValue,
                        additionalDatumName,
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE));
    }
}