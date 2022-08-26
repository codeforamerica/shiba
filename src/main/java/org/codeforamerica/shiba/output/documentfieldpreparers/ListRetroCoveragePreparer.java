package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RETROACTIVE_TIME_INDIVIDUAL;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RETROACTIVE_COVERAGE_MONTH;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RETROACTIVE_COVERAGE_MAP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RETROACTIVE_COVERAGE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RETROACTIVE_COVERAGE_SOURCE;
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
public class ListRetroCoveragePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> retroCoverageList = new ArrayList<>();
    PagesData pagesData = application.getApplicationData().getPagesData();

    boolean isRetroactiveCoverage = getBooleanValue(pagesData, RETROACTIVE_COVERAGE);
    if (!isRetroactiveCoverage) {
      return List.of();
    } else {
      List<String> retroCoverageMembers = getValues(pagesData, RETROACTIVE_COVERAGE_SOURCE).stream().toList();
      List<RetroCoverageMember> allApplicants = new ArrayList<RetroCoverageMember>();
      if(retroCoverageMembers.size() == 0) {//For Individual flow
        String month = getFirstValue(pagesData, RETROACTIVE_TIME_INDIVIDUAL);
        allApplicants.add(new RetroCoverageMember(getFullName(application),month));
      } else {
        retroCoverageMembers.stream().forEach(name ->{
          List<String> nameList = Arrays.stream(name.split(" ")).collect(Collectors.toList());
          String id = nameList.get(nameList.size()-1);
          String month = getRetroactiveMonths(pagesData, id);
          nameList.remove(id);
          String fullName =  String.join(" ", nameList);
          allApplicants.add(new RetroCoverageMember(fullName,month));
        });
      }
      int index = 0;
      for(RetroCoverageMember person: allApplicants) {
        retroCoverageList.add(new DocumentField(
            "retroactiveCoverage",
            "applicantName",
            String.join(" ", person.fullName),
            DocumentFieldType.SINGLE_VALUE, index ));
        retroCoverageList.add(new DocumentField(
            "retroactiveCoverage",
            "month",
            String.join(" ", person.month),
            DocumentFieldType.SINGLE_VALUE, index ));
        index++;
      }
      
    }

    return retroCoverageList;
  }
  private String getRetroactiveMonths(PagesData pagesData, String condition) {
    String result = "";
    List<String> retroactiveMonthMap = getValues(pagesData, RETROACTIVE_COVERAGE_MAP);
    int index = retroactiveMonthMap.stream().collect(Collectors.toList()).indexOf(condition);
    List<String> coverageMonth = getValues(pagesData, RETROACTIVE_COVERAGE_MONTH);
    result = coverageMonth.size()!=0?coverageMonth.get(index):result; 
    return result;
  }
 
  public class RetroCoverageMember{
    String fullName = "";
    String month = "";
    
    public RetroCoverageMember(String fullName, String month) {
      this.fullName = fullName;
      this.month = month;
    }
    
   
  }
  
}
