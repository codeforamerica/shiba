package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.function.Predicate;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.PagesData;
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
  protected ScopedParams getParams(Document document) {
    // Add non-self-employment scope for certain-pops only
    Predicate<PagesData> isSelfEmployed =
        pagesData -> !getBooleanValue(pagesData, IS_SELF_EMPLOYMENT);
    Predicate<PagesData> isSelfEmployedAndApplicant = isSelfEmployed
        .and(pagesData -> getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains("applicant"));

    return new ScopedParams(
        document == Document.CERTAIN_POPS ? isSelfEmployedAndApplicant : isSelfEmployed,
        JOBS,
        "nonSelfEmployment_");
  }

}
