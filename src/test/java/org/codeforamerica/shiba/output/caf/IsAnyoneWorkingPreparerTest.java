package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;
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
class IsAnyoneWorkingPreparerTest {

  private IsAnyoneWorkingPreparer isAnyoneWorkingPreparer;
  private ApplicationData applicationData;
  @Mock
  private GrossMonthlyIncomeParser grossMonthlyIncomeParser;

  @BeforeEach
  void setUp() {
    applicationData = new ApplicationData();
    isAnyoneWorkingPreparer = new IsAnyoneWorkingPreparer(grossMonthlyIncomeParser);
  }
  
  @Test
  void shouldReturnYesWhenOneWageJobExists() {
	  Application application = Application.builder().applicationData(applicationData).build();
	  
	    PagesData applicantJob1 = new TestApplicationDataBuilder()
	            .withPageData("selfEmployment", "selfEmployment", "true")
	            .build().getPagesData();
	    PagesData applicantJob2 = new TestApplicationDataBuilder()
	            .withPageData("selfEmployment", "selfEmployment", "false")
	            .build().getPagesData();
	    PagesData householdMemberJob1 = new TestApplicationDataBuilder()
	            .withPageData("selfEmployment", "selfEmployment", "true")
	            .build().getPagesData();
	    PagesData householdMemberJob2 = new TestApplicationDataBuilder()
	            .withPageData("selfEmployment", "selfEmployment", "true")
	            .build().getPagesData();
	    PagesData householdMemberJob3 = new TestApplicationDataBuilder()
	            .withPageData("selfEmployment", "selfEmployment", "true")
	            .build().getPagesData();
	    PagesData householdMemberJob4 = new TestApplicationDataBuilder()
	            .withPageData("selfEmployment", "selfEmployment", "true")
	            .build().getPagesData();
	    when(grossMonthlyIncomeParser.parse(applicationData)).thenReturn(List.of(
	            new HourlyJobIncomeInformation("12", "30", 0, new Iteration(applicantJob1)),
	            new HourlyJobIncomeInformation("6", "45", 1, new Iteration(householdMemberJob1)),
	            new HourlyJobIncomeInformation("12", "30", 0, new Iteration(applicantJob2)),
	            new HourlyJobIncomeInformation("6", "45", 1, new Iteration(householdMemberJob2)),
	            new HourlyJobIncomeInformation("12", "30", 0, new Iteration(householdMemberJob3)),
	            new HourlyJobIncomeInformation("6", "45", 1, new Iteration(householdMemberJob4))
	        ));
	  List<DocumentField> documentFields = isAnyoneWorkingPreparer			  
		        .prepareDocumentFields(application, Document.CCAP, null);
	    
	  assertThat(documentFields).containsOnly(
	  new DocumentField("employmentStatus", "isAnyoneWorking", "true", ENUMERATED_SINGLE_VALUE, null));
  }
  
  @Test 
  void shouldReturnNoWhenAllJobsAreSelfEmployed() {
	  Application application = Application.builder().applicationData(applicationData).build();
	    PagesData applicantJob = new TestApplicationDataBuilder()
	            .withPageData(
	                "selfEmployment", "selfEmployment", "true")
	            .build().getPagesData();
	        PagesData householdMemberJob = new TestApplicationDataBuilder()
	            .withPageData(
	                "selfEmployment", "selfEmployment", "true")
	            .build().getPagesData();
	    when(grossMonthlyIncomeParser.parse(applicationData)).thenReturn(List.of(
	            new HourlyJobIncomeInformation("12", "30", 0, new Iteration(applicantJob)),
	            new HourlyJobIncomeInformation("6", "45", 1, new Iteration(householdMemberJob))
	        ));
	  List<DocumentField> documentFields = isAnyoneWorkingPreparer
		        .prepareDocumentFields(application, null, null);
	  //false sets the radio button to No
	  assertThat(documentFields).containsOnly(new DocumentField("employmentStatus", "isAnyoneWorking", "false", ENUMERATED_SINGLE_VALUE, null));
  }
  
  @Test 
  void shouldReturnNoWhenNobodyIsWorking() {
	  Application application = Application.builder().applicationData(applicationData).build();
	  List<DocumentField> documentFields = isAnyoneWorkingPreparer
		        .prepareDocumentFields(application, null, null);
	  //false sets the radio button to No
	  assertThat(documentFields).containsOnly(new DocumentField("employmentStatus", "isAnyoneWorking", "false", ENUMERATED_SINGLE_VALUE, null));
  }

}
