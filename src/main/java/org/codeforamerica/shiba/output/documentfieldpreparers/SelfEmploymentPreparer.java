package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

@Component
public class SelfEmploymentPreparer extends SubworkflowScopePreparer {

  @Override
  protected ScopedParams getParams(Document _document, Application application) {
    return new ScopedParams(
        pagesData -> getBooleanValue(pagesData, IS_SELF_EMPLOYMENT),
        JOBS,
        "selfEmployment_");
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient _recipient) {
	ApplicationData applicationData = application.getApplicationData();
	List<DocumentField> results = new ArrayList<DocumentField>();
    results.addAll(super.prepareDocumentFields(application, document));

	List<Iteration> selfEmploymentJobs = getSelfEmploymentJobs(applicationData);
	if (selfEmploymentJobs.isEmpty()) {
		results.add(new DocumentField("employee", "selfEmployed", "false", SINGLE_VALUE));
	    return results;
	} else {
		results.add(new DocumentField("employee", "selfEmployed", "true", SINGLE_VALUE));
	}
 
    // generate self-employment DocumentFields for the Certain Pops document.
    if (document == Document.CERTAIN_POPS) {
    	Subworkflow jobsWorkflow = getGroup(applicationData, ApplicationDataParser.Group.JOBS);

    	// iterate jobs, add a pair of document fields for each self-employment job
		int selfEmploymentJobNo = 0;
		for (Iteration job : selfEmploymentJobs) {
			PagesData pagesData = job.getPagesData();
			PageData pageData = pagesData.getPage("householdSelectionForIncome");
			String employee = "";
			if (pageData != null) {
				employee = pageData.get("whoseJobIsItFormatted").getValue(0);
			} else { // when there is no whoseJobIsItFormatted then it has to be the applicant's job
				employee = applicantName(applicationData);
			}
			GrossMonthlyIncomeParser grossMonthlyIncomeParser = new GrossMonthlyIncomeParser();
			JobIncomeInformation jobIncomeInformation = grossMonthlyIncomeParser.parse(jobsWorkflow, job);
			String grossMonthly = jobIncomeInformation.grossMonthlyIncome().toPlainString();
			
			results.add(new DocumentField("selfEmployment_employee", "name", employee, SINGLE_VALUE, selfEmploymentJobNo));
			results.add(new DocumentField("selfEmployment_employee", "grossMonthlyIncome", grossMonthly, SINGLE_VALUE, selfEmploymentJobNo));
			selfEmploymentJobNo++;
			}
    } else { 
      results.add(new DocumentField("employee", "selfEmployedGrossMonthlyEarnings",
    		  !selfEmploymentJobs.isEmpty() ? "see question 9" : "", SINGLE_VALUE));
    }
    return results;
  }
  
  public List<Iteration> getSelfEmploymentJobs(ApplicationData applicationData) {
    List<Iteration> selfEmploymentJobs = new ArrayList<Iteration>();
	Subworkflow jobsWorkflow = getGroup(applicationData, ApplicationDataParser.Group.JOBS);
	if (jobsWorkflow != null) {
		for (Iteration job : jobsWorkflow) {
			PagesData pagesData = job.getPagesData();
			PageData pageData = pagesData.getPage("selfEmployment");
			if (pageData != null) {
				InputData inputData = pageData.get("selfEmployment");
				if (inputData.getValue(0).equalsIgnoreCase("true")) {
						selfEmploymentJobs.add(job);
				}
			}
		}
	}
	return selfEmploymentJobs;
  }
  
  public String applicantName(ApplicationData applicationData) {
		PageData personalInfoPage = applicationData.getPagesData().getPage("personalInfo");
		return personalInfoPage.get("firstName").getValue(0) + " " + personalInfoPage.get("lastName").getValue(0);
  }
  
}
