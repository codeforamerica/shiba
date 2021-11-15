package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class GrossMonthlyIncomePreparer implements DocumentFieldPreparer {

  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

  public GrossMonthlyIncomePreparer(GrossMonthlyIncomeParser grossMonthlyIncomeParser) {
    this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
  }

  private static boolean isApplicantIncomeInfo(JobIncomeInformation jobIncomeInformation) {
    PagesData pagesData = jobIncomeInformation.getIteration().getPagesData();
    return getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains("applicant");
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {

    List<JobIncomeInformation> jobsToIncludeInGrossIncome =
        getJobIncomeInformationToIncludeInThisDocument(application, document);

    // Scope for self-employment and non self-employment jobs
    int selfEmploymentIndex = 0, nonSelfEmploymentIndex = 0;
    int initialCapacity = jobsToIncludeInGrossIncome.size() * 2;
    List<DocumentField> fields = new ArrayList<>(initialCapacity);
    for (JobIncomeInformation job : jobsToIncludeInGrossIncome) {
      String pageName = "employee";
      String inputName = "grossMonthlyIncome";
      fields.add(new DocumentField(pageName, inputName, String.valueOf(job.grossMonthlyIncome()),
          SINGLE_VALUE, job.getIndexInJobsSubworkflow()));

      String prefix;
      int index;
      boolean isSelfEmployment = getBooleanValue(job.getIteration().getPagesData(),
          IS_SELF_EMPLOYMENT);
      if (isSelfEmployment) {
        prefix = "selfEmployment_";
        index = selfEmploymentIndex++;
      } else {
        prefix = "nonSelfEmployment_";
        index = nonSelfEmploymentIndex++;
      }
      fields.add(new DocumentField(
          prefix + pageName,
          inputName,
          String.valueOf(job.grossMonthlyIncome()),
          SINGLE_VALUE,
          index));
    }
    return fields;
  }

  private List<JobIncomeInformation> getJobIncomeInformationToIncludeInThisDocument(
      Application application,
      Document document) {
    List<JobIncomeInformation> grossIncomeInfoForAllHouseholdMembers =
        grossMonthlyIncomeParser.parse(application.getApplicationData());

    List<JobIncomeInformation> grossIncomeInfoToIncludeInThisDocument = grossIncomeInfoForAllHouseholdMembers;
    if (document == Document.CERTAIN_POPS) {
      // Only include income info for jobs held by the applicant
      grossIncomeInfoToIncludeInThisDocument = grossIncomeInfoForAllHouseholdMembers.stream()
          .filter(GrossMonthlyIncomePreparer::isApplicantIncomeInfo)
          .toList();
    }
    return grossIncomeInfoToIncludeInThisDocument;
  }
}

