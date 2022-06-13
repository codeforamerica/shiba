package org.codeforamerica.shiba.output.documentfieldpreparers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.PageUtils;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ListNonUSCitizenPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    //Check the status of usCitizen
    ApplicationDataParser applicationDataParser = new ApplicationDataParser();
    List<DocumentField> nonUSCitizens = new ArrayList<>();
    if(applicationDataParser.getBooleanValue(application
                    .getApplicationData()
                    .getPagesData(), ApplicationDataParser.Field.EVERYONE_US_CITIZENS)){
      return List.of();
    }else {
      //get whoIsNonUSCitizen list
      List<String> applicant = applicationDataParser.getValues(application
              .getApplicationData()
              .getPagesData(), ApplicationDataParser.Field.WHO_ARE_NON_US_CITIZENS).stream().filter(nonCitizen -> nonCitizen.endsWith("applicant")).collect(Collectors.toList());

      if (!applicant.isEmpty()){
        List<String> nameList = Arrays.stream(applicant.get(0).split(" ")).collect(Collectors.toList());
        nameList.remove("applicant");
        nonUSCitizens.add(new DocumentField(
                "whoIsNonUsCitizen",
                "nameOfApplicantOrSpouse1",
                String.join(" ", nameList),
                DocumentFieldType.SINGLE_VALUE,
                null));
      }
    }

    //use the application data parser
    //How do we identify if someone is a citizen or not?

    //whoisNonuscitizen evalutes to true?

    return nonUSCitizens;
  }
}
