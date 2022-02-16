package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PERSONAL_INFO_NO_SSN;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PERSONAL_INFO_SSN;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.List;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class NoSocialSecurityNumberPreparer implements DocumentFieldPreparer {

  /**
   * No SSN Checkbox is only on the CERTAIN_POPS PDF, this preparer does not add fields for other
   * application types.
   *
   * @return DocumentField for NO_SSN pdf value
   */
  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    PagesData pagesData = application.getApplicationData().getPagesData();
    String noSSNCheckbox = getFirstValue(pagesData, PERSONAL_INFO_NO_SSN);
    String ssn = getFirstValue(pagesData, PERSONAL_INFO_SSN);
    if (noSSNCheckbox == null && (ssn == null
        || ssn.isBlank())) { //checkbox not selected and ssn is blank
      return List.of(); //do not check either radio button
    } else if (noSSNCheckbox == null) {
      return List.of(new DocumentField("personalInfo", "noSSNCheck", "true",
          SINGLE_VALUE)); // yes radio button
    } else {
      return List.of(new DocumentField("personalInfo", "noSSNCheck", "false",
          SINGLE_VALUE)); // no radio button
    }
  }

}
