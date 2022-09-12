package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ALIEN_ID;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ALIEN_IDS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ALIEN_ID_MAP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.EVERYONE_US_CITIZENS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHO_ARE_NON_US_CITIZENS;
import static org.codeforamerica.shiba.output.FullNameFormatter.getFullName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class ListNonUSCitizenPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> nonUSCitizens = new ArrayList<>();
    List<NonUSCitizen> allApplicantsNonCitizen = getNonUSCitizens(application, document, recipient);
      int index = 0;
      for(NonUSCitizen person: allApplicantsNonCitizen) {
        nonUSCitizens.add(new DocumentField(
            "whoIsNonUsCitizen",
            "nameOfApplicantOrSpouse",
            String.join(" ", person.fullName),
            DocumentFieldType.SINGLE_VALUE, index ));
        nonUSCitizens.add(new DocumentField(
            "whoIsNonUsCitizen",
            "alienId",
            String.join(" ", person.alienId),
            DocumentFieldType.SINGLE_VALUE, index ));
        index++;
      }
    return nonUSCitizens;
  }

    public List<NonUSCitizen> getNonUSCitizens(Application application, Document document,
        Recipient recipient) {

      List<NonUSCitizen> allApplicantsNonCitizen = new ArrayList<NonUSCitizen>();
      PagesData pagesData = application.getApplicationData().getPagesData();

      boolean allApplicantsAreCitizens = getBooleanValue(pagesData, EVERYONE_US_CITIZENS);
      if (allApplicantsAreCitizens) {
        return List.of();
      } else {
        List<String> nonCitizens = getValues(pagesData, WHO_ARE_NON_US_CITIZENS).stream().toList();

        if (nonCitizens.size() == 0) {// For Individual flow
          String alienId = getFirstValue(pagesData, ALIEN_ID);
          allApplicantsNonCitizen
              .add(new NonUSCitizen(getFullName(application), alienId == null ? "" : alienId));
        } else {
          nonCitizens.stream().forEach(name -> {
            List<String> nameList = Arrays.stream(name.split(" ")).collect(Collectors.toList());
            String id = nameList.get(nameList.size() - 1);
            String alienNumber = getAlienNumber(pagesData, id);
            nameList.remove(id);
            String fullName = String.join(" ", nameList);
            allApplicantsNonCitizen.add(new NonUSCitizen(fullName, alienNumber));
          });
        }
      }
      return allApplicantsNonCitizen;
    }
  private String getAlienNumber(PagesData pagesData, String condition) {
    String result = "";
    List<String> alienIdMap = getValues(pagesData, ALIEN_ID_MAP);
    int index = alienIdMap.stream().collect(Collectors.toList()).indexOf(condition);
    List<String> alienNumbers = getValues(pagesData, ALIEN_IDS);
    result = alienNumbers.size()!=0?alienNumbers.get(index):result; 
    return result;
  }
 
  public class NonUSCitizen{
    String fullName = "";
    String alienId = "";
    
    public NonUSCitizen(String fullName, String alienId) {
      this.fullName = fullName;
      this.alienId = alienId;
    }
    
   
  }
  
}
