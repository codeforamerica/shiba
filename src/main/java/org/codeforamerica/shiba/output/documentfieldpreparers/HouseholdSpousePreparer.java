package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_DOB;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_LAST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_MARITAL_STATUS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_RELATIONSHIP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_SEX;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_SSN;
import static org.codeforamerica.shiba.output.DocumentFieldType.DATE_VALUE;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class HouseholdSpousePreparer implements DocumentFieldPreparer {
  
  
  private final Map<String, String> personalDataMappings;

  
  public HouseholdSpousePreparer(Map<String, String> personalDataMappings) {
    super();
    this.personalDataMappings = personalDataMappings;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    
    
    
    var spouseInfo = getSpouseInfo(application, recipient);
    
    return spouseInfo;

  }
  
  private List<DocumentField> getSpouseInfo(Application application, Recipient recipient) {
    var householdSubworkflow =
        ofNullable(getGroup(application.getApplicationData(), Group.HOUSEHOLD));
    return householdSubworkflow.map(subworkflow -> getApplicationInputsForSubworkflow(subworkflow, recipient)).orElse(emptyList());
  }


@NotNull
private List<DocumentField> getApplicationInputsForSubworkflow(Subworkflow subworkflow, Recipient recipient) {
  List<Iteration> householdMemberWithoutSpouseInfo = subworkflow.stream().filter(householdData -> !getValues(householdData.getPagesData(),
      Field.HOUSEHOLD_INFO_RELATIONSHIP).equals(List.of("spouse"))).collect(Collectors.toList());
  int index = 0;
  List<DocumentField> inputsForSubworkflow = new ArrayList<>();
  for (Iteration i : householdMemberWithoutSpouseInfo) {
    var pageData = i.getPagesData();
    var relationship = getFirstValue(pageData, HOUSEHOLD_INFO_RELATIONSHIP);
    var pgFirstName = getFirstValue(pageData, HOUSEHOLD_INFO_FIRST_NAME);
    var pgLastName = getFirstValue(pageData, HOUSEHOLD_INFO_LAST_NAME);
    var pgDOB = getValues(pageData, HOUSEHOLD_INFO_DOB);
    var pgSex = getFirstValue(pageData, HOUSEHOLD_INFO_SEX);
    var pgMaritalStatus = getFirstValue(pageData, HOUSEHOLD_INFO_MARITAL_STATUS);
    var pgSSN = getFirstValue(pageData, HOUSEHOLD_INFO_SSN);
    inputsForSubworkflow.add(
        new DocumentField("householdMemberWithoutSpouseInfo", "firstName", pgFirstName, SINGLE_VALUE, index));
    inputsForSubworkflow
        .add(new DocumentField("householdMemberWithoutSpouseInfo", "lastName", pgLastName, SINGLE_VALUE, index));
    inputsForSubworkflow.add(new DocumentField("householdMemberWithoutSpouseInfo", "relationship", relationship,
        SINGLE_VALUE, index));
    inputsForSubworkflow
        .add(new DocumentField("householdMemberWithoutSpouseInfo", "dateOfBirth", pgDOB, DATE_VALUE, index));
    inputsForSubworkflow.add(
        new DocumentField("householdMemberWithoutSpouseInfo", "sex", pgSex, ENUMERATED_SINGLE_VALUE, index));
    inputsForSubworkflow.add(new DocumentField("householdMemberWithoutSpouseInfo", "maritalStatus",
        pgMaritalStatus, ENUMERATED_SINGLE_VALUE, index));
    if (pgSSN.length() > 0) {
      inputsForSubworkflow
          .add(new DocumentField("householdMemberWithoutSpouseInfo", "ssnYesNo", "Yes", SINGLE_VALUE, index));
      if (Recipient.CLIENT.equals(recipient) && personalDataMappings.get("ssn") != null) {
        pgSSN = personalDataMappings.get("ssn");
      }
      inputsForSubworkflow
          .add(new DocumentField("householdMemberWithoutSpouseInfo", "ssn", pgSSN, SINGLE_VALUE, index));

    }
    index++;
  }
  
  Optional<Iteration> spouseHouseholdMemberInfo = subworkflow.stream().filter(householdData -> getValues(householdData.getPagesData(),
      Field.HOUSEHOLD_INFO_RELATIONSHIP).equals(List.of("spouse"))).findFirst();
  
  spouseHouseholdMemberInfo.ifPresent(iteration -> {
    var relationship = getFirstValue(iteration.getPagesData(), HOUSEHOLD_INFO_RELATIONSHIP);
    var pgFirstName = getFirstValue(iteration.getPagesData(), HOUSEHOLD_INFO_FIRST_NAME);
    var pgLastName = getFirstValue(iteration.getPagesData(), HOUSEHOLD_INFO_LAST_NAME);
    var pgDOB = getValues(iteration.getPagesData(), HOUSEHOLD_INFO_DOB);
    var pgSex = getFirstValue(iteration.getPagesData(), HOUSEHOLD_INFO_SEX);
    var pgMaritalStatus = getFirstValue(iteration.getPagesData(), HOUSEHOLD_INFO_MARITAL_STATUS);
    var pgSSN = getFirstValue(iteration.getPagesData(), HOUSEHOLD_INFO_SSN);
    inputsForSubworkflow
        .add(new DocumentField("spouseInfo", "firstName", pgFirstName, SINGLE_VALUE));
    inputsForSubworkflow.add(new DocumentField("spouseInfo", "lastName", pgLastName, SINGLE_VALUE));
    inputsForSubworkflow
        .add(new DocumentField("spouseInfo", "relationship", relationship, SINGLE_VALUE));
    inputsForSubworkflow.add(new DocumentField("spouseInfo", "dateOfBirth", pgDOB, DATE_VALUE));
    inputsForSubworkflow
        .add(new DocumentField("spouseInfo", "sex", pgSex, ENUMERATED_SINGLE_VALUE));
    inputsForSubworkflow.add(
        new DocumentField("spouseInfo", "maritalStatus", pgMaritalStatus, ENUMERATED_SINGLE_VALUE));
    if (pgSSN.length() > 0) {
      inputsForSubworkflow.add(new DocumentField("spouseInfo", "ssnYesNo", "Yes", SINGLE_VALUE));
      if (Recipient.CLIENT.equals(recipient) && personalDataMappings.get("ssn") != null) {
        pgSSN = personalDataMappings.get("ssn");
      }
      inputsForSubworkflow.add(new DocumentField("spouseInfo", "ssn", pgSSN, SINGLE_VALUE));
    }

  });
  return inputsForSubworkflow;
}

}

