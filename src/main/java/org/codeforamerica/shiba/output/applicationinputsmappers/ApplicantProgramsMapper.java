package org.codeforamerica.shiba.output.applicationinputsmappers;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class ApplicantProgramsMapper implements ApplicationInputsMapper {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {

    List<String> programs = application.getApplicationData().getPagesData()
        .safeGetPageInputValue("choosePrograms", "programs");

    boolean isMFIP = application.getApplicationData().getPagesData()
        .safeGetPageInputValue("applyForMFIP", "applyForMFIP").contains("true");

    if (isMFIP && !programs.contains("CASH")) {
      programs.add("CASH");
    }

    List<DocumentField> programSelections = new ArrayList<>();

    programs.forEach(program -> {
      programSelections.add(
          new DocumentField("applicantPrograms", program, List.of("Yes"),
              DocumentFieldType.SINGLE_VALUE)
      );
    });

    return programSelections;
  }
}
