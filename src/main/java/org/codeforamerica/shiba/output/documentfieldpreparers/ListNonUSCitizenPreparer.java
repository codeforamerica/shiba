package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.EVERYONE_US_CITIZENS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHO_ARE_NON_US_CITIZENS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.HOUSEHOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class ListNonUSCitizenPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> nonUSCitizens = new ArrayList<>();
    PagesData pagesData = application.getApplicationData().getPagesData();

    boolean allApplicantsAreCitizens = getBooleanValue(pagesData, EVERYONE_US_CITIZENS);
    if (allApplicantsAreCitizens) {
      return List.of();
    } else {
      List<String> nonCitizens = getValues(pagesData, WHO_ARE_NON_US_CITIZENS).stream().toList();
      Optional<String> applicant = nonCitizens.stream()
          .filter(nonCitizen -> nonCitizen.endsWith("applicant")).findFirst();

      applicant.ifPresent(applicantName -> {
        List<String> nameList = Arrays.stream(applicantName.split(" "))
            .collect(Collectors.toList());
        nameList.remove("applicant");
        nonUSCitizens.add(new DocumentField(
            "whoIsNonUsCitizen",
            "nameOfApplicantOrSpouse1",
            String.join(" ", nameList),
            DocumentFieldType.SINGLE_VALUE)
        );
      });

      Optional<Iteration> spouseHouseholdMemberInfo = getGroup(application.getApplicationData(),
          HOUSEHOLD).stream().filter(householdData -> getValues(householdData.getPagesData(),
                  Field.HOUSEHOLD_INFO_RELATIONSHIP)
                  .equals(List.of("spouse")))
          .findFirst();

      spouseHouseholdMemberInfo.ifPresent(iteration -> nonUSCitizens.add(
          new DocumentField(
              "whoIsNonUsCitizen",
              "nameOfApplicantOrSpouse2",
              String.join(" ", List.of(
                  getFirstValue(iteration.getPagesData(), Field.HOUSEHOLD_INFO_FIRST_NAME),
                  getFirstValue(iteration.getPagesData(), Field.HOUSEHOLD_INFO_LAST_NAME)
              )),
              DocumentFieldType.SINGLE_VALUE)
      ));
    }

    return nonUSCitizens;
  }
}