package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
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
import org.codeforamerica.shiba.output.documentfieldpreparers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.output.documentfieldpreparers.SubworkflowIterationScopeTracker.IterationScopeInfo;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class GrossMonthlyIncomePreparer implements DocumentFieldPreparer {

  private final ApplicationConfiguration applicationConfiguration;
  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

  public GrossMonthlyIncomePreparer(GrossMonthlyIncomeParser grossMonthlyIncomeParser,
      ApplicationConfiguration applicationConfiguration) {
    this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    this.applicationConfiguration = applicationConfiguration;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {

    List<JobIncomeInformation> jobsToIncludeInGrossIncome =
        getJobIncomeInformationToIncludeInThisDocument(application, document);

    int initialCapacity = jobsToIncludeInGrossIncome.size() * 2;
    List<DocumentField> fields = new ArrayList<>(initialCapacity);
    jobsToIncludeInGrossIncome.forEach(job -> {
          String pageName = "employee";
          String inputName = "grossMonthlyIncome";
          fields.add(new DocumentField(pageName, inputName, String.valueOf(job.grossMonthlyIncome()),
              SINGLE_VALUE, job.getIndexInJobsSubworkflow()));

          PageGroupConfiguration pageGroupConfiguration =
              applicationConfiguration.getPageGroups().get("jobs");
          IterationScopeInfo scopeInfo =
              scopeTracker.getIterationScopeInfo(pageGroupConfiguration, job.getIteration());
          if (scopeInfo != null) {
            String groupName = scopeInfo.getScope() + "_" + pageName;
            fields.add(new DocumentField(groupName, inputName, String.valueOf(job.grossMonthlyIncome()),
                SINGLE_VALUE, scopeInfo.getIndex()));
          }
        }
    );
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

  private static boolean isApplicantIncomeInfo(JobIncomeInformation jobIncomeInformation) {
    PagesData pagesData = jobIncomeInformation.getIteration().getPagesData();
    return getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains("applicant");
  }
}

