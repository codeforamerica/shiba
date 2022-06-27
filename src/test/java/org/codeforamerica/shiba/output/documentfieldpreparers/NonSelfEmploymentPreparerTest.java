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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NonSelfEmploymentPreparerTest {

  private NonSelfEmploymentPreparer preparer;

  @BeforeEach
  public void setup() {
    preparer = new NonSelfEmploymentPreparer();
  }

  @Test
  public void shouldMapValuesIfNonSelfEmployedJobExists() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("true", "10", "10"),
            new PagesDataBuilder().withNonHourlyJob("false", "12", "EVERY_WEEK"))
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> actual =
        preparer.prepareDocumentFields(application, null, Recipient.CLIENT);

    assertThat(actual).containsExactlyInAnyOrder(
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod", "12",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod_EVERY_WEEK", "12",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_payPeriod", "payPeriod", "EVERY_WEEK",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_paidByTheHour", "paidByTheHour", "false",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_selfEmployment", "selfEmployment", "false",
            SINGLE_VALUE, 0)
    );
  }

  @Test
  public void shouldMapApplicantValuesIfNonSelfEmployedJobExistsForCertainPops() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withMultipleHouseholdMembers()
        .withPageData("addHouseholdMembers", "addHouseholdMembers", "true")
        .withSubworkflow("jobs",
            // Don't add fields because this is a self-employed job
            new PagesDataBuilder()
                .withHourlyJob("true", "10", "10")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "me the applicant"),
            // Don't add fields because this is not the applicant
            new PagesDataBuilder()
                .withNonHourlyJob("false", "11", "EVERY_WEEK")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "someone else 12356"),
            // Add this
            new PagesDataBuilder()
                .withNonHourlyJob("false", "12", "EVERY_MONTH")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "me the applicant"))
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> actual =
        preparer.prepareDocumentFields(application, Document.CERTAIN_POPS, Recipient.CLIENT);

    assertThat(actual).containsExactlyInAnyOrder(
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod", "12",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_payPeriod", "payPeriod", "EVERY_MONTH",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_paidByTheHour", "paidByTheHour", "false",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_selfEmployment", "selfEmployment", "false",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod_EVERY_MONTH", "12",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_householdSelectionForIncome", "whoseJobIsIt",
            "me the applicant", SINGLE_VALUE, 0)
    );
  }
  
  @Test
  public void shouldMapSpouseValuesIfNonSelfEmployedJobExistsForCertainPops() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
          .withPageData("addHouseholdMembers", "addHouseholdMembers", "true")
        .withMultipleHouseholdMembers()
        .withSubworkflow("jobs",
            // Don't add fields because this is a self-employed job
            new PagesDataBuilder()
                .withHourlyJob("true", "10", "10")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "me the applicant"),
            // Don't add fields because this is not the applicant
            new PagesDataBuilder()
                .withNonHourlyJob("false", "11", "EVERY_WEEK")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "someone else 12356"),
            // Add this
            new PagesDataBuilder()
                .withNonHourlyJob("false", "21", "EVERY_MONTH")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "Daria Agàta someGuid"))
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> actual =
        preparer.prepareDocumentFields(application, Document.CERTAIN_POPS, Recipient.CLIENT);

    assertThat(actual).containsExactlyInAnyOrder(
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod", "21",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod_EVERY_MONTH", "21",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_payPeriod", "payPeriod", "EVERY_MONTH",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_paidByTheHour", "paidByTheHour", "false",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_selfEmployment", "selfEmployment", "false",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_householdSelectionForIncome", "whoseJobIsIt",
            "Daria Agàta someGuid", SINGLE_VALUE, 0)
    );
  }
  
  @Test
  public void shouldMapPayAmountWithPayFrequencyNonSelfEmployedJobExists() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("addHouseholdMembers", "addHouseholdMembers", "true")
        .withMultipleHouseholdMembers()
        .withSubworkflow("jobs",
            // Don't add fields because this is a self-employed job
            new PagesDataBuilder()
                .withHourlyJob("true", "10", "10")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "me the applicant"),
            // Don't add fields because this is not the applicant
            new PagesDataBuilder()
                .withNonHourlyJob("false", "11", "EVERY_WEEK")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "someone else 12356"),
            // Add this
            new PagesDataBuilder()
                .withNonHourlyJob("false", "1500", "EVERY_MONTH")
                .withPageData("householdSelectionForIncome", "whoseJobIsIt", "Daria Agàta someGuid"))
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> actual =
        preparer.prepareDocumentFields(application, Document.CERTAIN_POPS, Recipient.CLIENT);

    assertThat(actual).containsExactlyInAnyOrder(
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod", "1500",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_payPeriod", "payPeriod", "EVERY_MONTH",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_paidByTheHour", "paidByTheHour", "false",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_selfEmployment", "selfEmployment", "false",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_incomePerPayPeriod", "incomePerPayPeriod_EVERY_MONTH", "1500",
            SINGLE_VALUE, 0),
        new DocumentField("nonSelfEmployment_householdSelectionForIncome", "whoseJobIsIt",
            "Daria Agàta someGuid", SINGLE_VALUE, 0)
    );
  }

  @Test
  void shouldMapEmptyIfNoJobs() {
    ApplicationData applicationData =
        new TestApplicationDataBuilder().withPersonalInfo().withMultipleHouseholdMembers().build();
    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(preparer.prepareDocumentFields(application, null, Recipient.CLIENT)).isEmpty();
  }

}
