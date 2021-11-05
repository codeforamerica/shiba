package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

class ExpeditedEligibilityMapperTest {

  SnapExpeditedEligibilityDecider mockSnapDecider = mock(SnapExpeditedEligibilityDecider.class);
  CcapExpeditedEligibilityDecider mockCcapDecider = mock(CcapExpeditedEligibilityDecider.class);

  @Test
  void shouldReturnEligibleWhenDeciderDecidesEligible() {
    ExpeditedEligibilityMapper mapper = new ExpeditedEligibilityMapper(mockSnapDecider,
        mockCcapDecider);
    ApplicationData appData = new ApplicationData();
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(appData)
        .county(County.Other)
        .timeToComplete(null)
        .build();

    when(mockSnapDecider.decide(any())).thenReturn(SnapExpeditedEligibility.ELIGIBLE);
    when(mockCcapDecider.decide(any())).thenReturn(CcapExpeditedEligibility.ELIGIBLE);

    assertThat(
        mapper.prepareDocumentFields(application, null, Recipient.CLIENT, null)).containsExactly(
        new DocumentField(
            "snapExpeditedEligibility",
            "snapExpeditedEligibility",
            List.of("SNAP"),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "ccapExpeditedEligibility",
            "ccapExpeditedEligibility",
            List.of("CCAP"),
            DocumentFieldType.SINGLE_VALUE
        )
    );
  }

  @Test
  void shouldReturnNotEligibleWhenDeciderDecidesNotEligible() {
    ExpeditedEligibilityMapper mapper = new ExpeditedEligibilityMapper(mockSnapDecider,
        mockCcapDecider);
    ApplicationData appData = new ApplicationData();
    PagesData pagesData = new PagesData();
    pagesData.put("page1", new PageData());
    appData.setPagesData(pagesData);
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(appData)
        .county(County.Other)
        .timeToComplete(null)
        .build();

    when(mockSnapDecider.decide(any())).thenReturn(SnapExpeditedEligibility.NOT_ELIGIBLE);
    when(mockCcapDecider.decide(any())).thenReturn(CcapExpeditedEligibility.NOT_ELIGIBLE);

    List<DocumentField> result = mapper.prepareDocumentFields(application, null, Recipient.CLIENT,
        null);

    verify(mockSnapDecider).decide(appData);
    assertThat(result).containsExactly(
        new DocumentField(
            "snapExpeditedEligibility",
            "snapExpeditedEligibility",
            List.of(""),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "ccapExpeditedEligibility",
            "ccapExpeditedEligibility",
            List.of(""),
            DocumentFieldType.SINGLE_VALUE
        )

    );
  }
}
