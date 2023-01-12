package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import static org.mockito.Mockito.when;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrossMonthlyIncomePreparerTest {

  private GrossMonthlyIncomePreparer grossMonthlyIncomePreparer;
  private ApplicationData applicationData;
  @Mock
  private GrossMonthlyIncomeParser grossMonthlyIncomeParser;

  @BeforeEach
  void setUp() {
    applicationData = new ApplicationData();
    grossMonthlyIncomePreparer = new GrossMonthlyIncomePreparer(grossMonthlyIncomeParser);
  }

  @Test
  void shouldMapJobIncomeInformationToInputs() {
    Application application = Application.builder().applicationData(applicationData).build();
    when(grossMonthlyIncomeParser.parse(applicationData)).thenReturn(List.of(
        new HourlyJobIncomeInformation("12", "30", 0, new Iteration()),
        new HourlyJobIncomeInformation("6", "45", 1, new Iteration())
    ));
    List<DocumentField> documentFields = grossMonthlyIncomePreparer
        .prepareDocumentFields(application, null, null);

    assertThat(documentFields).containsOnly(
        new DocumentField("employee", "grossMonthlyIncome", "1440.00", SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_employee", "grossMonthlyIncome", "1440.00",
            SINGLE_VALUE, 0),
        new DocumentField("employee", "grossMonthlyIncome", "1080.00", SINGLE_VALUE, 1),
        new DocumentField("nonSelfEmployment_employee", "grossMonthlyIncome", "1080.00",
            SINGLE_VALUE, 1)
    );
  }

  /**
   * For certain-pops, we want to map gross monthly income for applicant's and household member's self-employment
   */
  @Test
  void shouldMapAllJobIncomeInformationForCertainPops() {
    Application application = Application.builder().applicationData(applicationData).build();
    PagesData applicantJob = new TestApplicationDataBuilder()
        .withPageData(
            "householdSelectionForIncome", "whoseJobIsIt", "the applicant")
        .build().getPagesData();
    PagesData nonApplicantJob = new TestApplicationDataBuilder()
        .withPageData(
            "householdSelectionForIncome", "whoseJobIsIt", "someone else")
        .build().getPagesData();
    when(grossMonthlyIncomeParser.parse(applicationData)).thenReturn(List.of(
        new HourlyJobIncomeInformation("12", "30", 0, new Iteration(applicantJob)),
        new HourlyJobIncomeInformation("6", "45", 1, new Iteration(nonApplicantJob))
    ));
    List<DocumentField> documentFields = grossMonthlyIncomePreparer
        .prepareDocumentFields(application, Document.CERTAIN_POPS, null);

    assertThat(documentFields).containsOnly(
        new DocumentField("employee", "grossMonthlyIncome", "1440.00", SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_employee", "grossMonthlyIncome", "1440.00",
            SINGLE_VALUE, 0),
        new DocumentField("employee", "grossMonthlyIncome", "1080.00", SINGLE_VALUE, 1),
        new DocumentField("nonSelfEmployment_employee", "grossMonthlyIncome", "1080.00",
            SINGLE_VALUE, 1)
    );
  }
}
