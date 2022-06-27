package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HAS_HOUSE_HOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_LAST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_RELATIONSHIP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import java.util.function.Predicate;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

/**
 * Create enumerated document fields just for non self-employment jobs.
 * <p>
 * Ex. [{employer: jobA, self-employed: false}, {employer: jobB, self-employed: true}, {employer:
 * jobC, self-employed: false}]
 * <p>
 * Will return 2 document fields for the 2 non-self employed jobs --> [{nonSelfEmployed_employer:
 * jobA, 0}, {nonSelfEmployed_employer: jobC, 1}]
 */
@Component
public class NonSelfEmploymentPreparer extends SubworkflowScopePreparer {

  @Override
  protected ScopedParams getParams(Document document, Application application) {
    // Add non-self-employment scope for certain-pops only
    Predicate<PagesData> isSelfEmployed =
        pagesData -> !getBooleanValue(pagesData, IS_SELF_EMPLOYMENT);
    boolean hasHouseHold = getValues(application.getApplicationData().getPagesData(),HAS_HOUSE_HOLD).contains("true");
    String spouseName = hasHouseHold?getSpouseFullName(application):"";
    Predicate<PagesData> isSelfEmployedAndApplicantOrSpouse = isSelfEmployed
        .and(pagesData -> (getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains("applicant")
            || (!spouseName.isEmpty()?getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains(spouseName):false)));
    
    

    return new ScopedParams(
        document == Document.CERTAIN_POPS ? isSelfEmployedAndApplicantOrSpouse : isSelfEmployed,
        JOBS,
        "nonSelfEmployment_");
  }
  
  private String getSpouseFullName(Application application) {
    String fullName = "";
    Subworkflow householdSubworkflow =
        getGroup(application.getApplicationData(), Group.HOUSEHOLD);  
    if(householdSubworkflow != null) {
      for (Iteration iteration : householdSubworkflow) {
        PagesData pagesData = iteration.getPagesData();
        if (getFirstValue(pagesData, HOUSEHOLD_INFO_RELATIONSHIP).equals("spouse")) {
          fullName = getFirstValue(pagesData, HOUSEHOLD_INFO_FIRST_NAME) + " "
              + getFirstValue(pagesData, HOUSEHOLD_INFO_LAST_NAME);
        }
      }
    }
    return fullName;
  }

}
