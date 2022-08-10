package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class SelfEmploymentPreparerTest {

  private final SelfEmploymentPreparer selfEmploymentPreparer = new SelfEmploymentPreparer();

  @Test
  void shouldMapValuesIfSelfEmployedJobExists() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("false", "10", "10"),
            new PagesDataBuilder().withNonHourlyJob("true", "12", "EVERY_WEEK"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> actual =
        selfEmploymentPreparer.prepareDocumentFields(application, null, Recipient.CLIENT);
    
    assertThat(actual).containsExactlyInAnyOrder(
        new DocumentField("employee", "selfEmployed", "true", SINGLE_VALUE),
        new DocumentField("employee", "selfEmployedGrossMonthlyEarnings", "see question 9",
            SINGLE_VALUE),
        new DocumentField("selfEmployment_incomePerPayPeriod", "incomePerPayPeriod_EVERY_WEEK", "12",
            SINGLE_VALUE, 0),
        new DocumentField("selfEmployment_incomePerPayPeriod", "incomePerPayPeriod", "12",
            SINGLE_VALUE, 0),
        new DocumentField("selfEmployment_payPeriod", "payPeriod", "EVERY_WEEK", SINGLE_VALUE, 0),
        new DocumentField("selfEmployment_paidByTheHour", "paidByTheHour", "false", SINGLE_VALUE,
            0),
        new DocumentField("selfEmployment_selfEmployment", "selfEmployment", "true", SINGLE_VALUE,
            0)
    );
  }

  @Test
  void shouldMapFalseIfSelfEmployedJobDoesntExists() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("false", "10", "10"),
            new PagesDataBuilder().withNonHourlyJob("false", "10", "EVERY_WEEK"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentPreparer
        .prepareDocumentFields(application, null, Recipient.CLIENT
        ))
        .containsExactlyInAnyOrder(
            new DocumentField("employee", "selfEmployed", "false", SINGLE_VALUE),
            new DocumentField("employee", "selfEmployedGrossMonthlyEarnings", "", SINGLE_VALUE)
        );
  }

  @Test
  void shouldMapEmptyIfNoJobs() {
    ApplicationData applicationData = new ApplicationData();
    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentPreparer
        .prepareDocumentFields(application, null, Recipient.CLIENT
        ))
        .isEmpty();
  }
  
//TODO emj test1
  @Test
  void certainPopsApplicantOnlyHasSelfEmployment() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withNonHourlyJob("true", "12", "EVERY_WEEK"),//true for selfEmployed
            new PagesDataBuilder().withPageData("householdSelectionForIncome", "whoseJobIsIt", "Inga Walsh applicant"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();
    
    Document cpDoc = Document.CERTAIN_POPS;

    List<DocumentField> actual =
        selfEmploymentPreparer.prepareDocumentFields(application, cpDoc, Recipient.CLIENT);
    
    assertThat(actual).containsExactlyInAnyOrder(
        new DocumentField("employee", "selfEmployed", "true", SINGLE_VALUE),
//        new DocumentField("employee", "selfEmployedGrossMonthlyEarnings", "see question 9",
//            SINGLE_VALUE),
        new DocumentField("selfEmployment_incomePerPayPeriod", "incomePerPayPeriod_EVERY_WEEK", "12",
            SINGLE_VALUE, 0),
        new DocumentField("selfEmployment_incomePerPayPeriod", "incomePerPayPeriod", "12",
            SINGLE_VALUE, 0),
        new DocumentField("selfEmployment_payPeriod", "payPeriod", "EVERY_WEEK", SINGLE_VALUE, 0),
//        new DocumentField("selfEmployment_paidByTheHour", "paidByTheHour", "false", SINGLE_VALUE,
//            0),
        new DocumentField("selfEmployment_selfEmployment", "selfEmployment", "true", SINGLE_VALUE,
            0)
    );
  }
  

//TODO emj test2
  @Test
  void certainPopsApplicantHasSelfEmployment() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withNonHourlyJob("true", "12", "EVERY_WEEK") //true for selfEmployed
            .withPageData("householdSelectionForIncome", "whoseJobIsIt", "Tom Smith applicant"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();
    
    Document cpDoc = Document.CERTAIN_POPS;
    List<DocumentField> actual =
        selfEmploymentPreparer.prepareDocumentFields(application, cpDoc, Recipient.CLIENT);
    
    assertThat(actual).containsExactlyInAnyOrder(
        new DocumentField("employee", "selfEmployed", "true", SINGLE_VALUE),
        new DocumentField("employee","selfEmployedApplicantName","", SINGLE_VALUE));
  }

}
