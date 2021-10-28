package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;

/**
 * Enrichments are used to take applicant inputs and create application data inputs based off of
 * that information.
 * <p>
 * Ex. An applicant enters dob, which we store as an array ["1", "10", "1990"]. For datascience
 * purposes, it is helpful to see this information as a single string "1/10/1990". So we create an
 * enrichment {@link PersonalInfoDateOfBirthEnrichment} and add it to the workflow in pages-config -
 * then when the applicant submits the personalInfo page, the string "1/10/1990" will be added as
 * part of the "enrichment".
 * <p>
 * {@code {dob: ["1", "10", "1990"]} --> {dob: ["1", "10", "1990"], dobAsDate: ["1/10/1990"]} }
 */
public interface Enrichment {

  PageData process(PagesData pagesData);
}
