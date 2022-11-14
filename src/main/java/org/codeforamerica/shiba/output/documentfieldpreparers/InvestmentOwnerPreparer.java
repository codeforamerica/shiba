package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HAS_HOUSE_HOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_LAST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.INVESTMENT_TYPE_INDIVIDUAL;
import static org.codeforamerica.shiba.output.FullNameFormatter.getFullName;
import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class InvestmentOwnerPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> results = new ArrayList<>();
    List<Investment> investmentOwners = getInvestmentOwners(application, document, recipient);
    int i = 0;
    for(Investment inv: investmentOwners) {
      results.add(new DocumentField("assetOwnerSource", "investmentOwners", List.of(inv.fullName), DocumentFieldType.SINGLE_VALUE, i));
      results.add(new DocumentField("assetOwnerSource", "investmentType", 
          inv.investmentType.stream().map(Object::toString).collect(Collectors.joining(", ")), DocumentFieldType.SINGLE_VALUE, i));
      i++;
    }
 
    return results;
  }
  
  public List<Investment> getInvestmentOwners(Application application, Document document, Recipient recipient){
    
    boolean hasHouseHold = getValues(application.getApplicationData().getPagesData(),HAS_HOUSE_HOLD).contains("true");

    List<Investment> investmentOwners = new ArrayList<Investment>();
    if(hasHouseHold) {
      List<String> stockOwners = getListOfSelectedFullNames(application, "stocksHouseHoldSource", "stocksHouseHoldSource");
      List<String> bondOwners = getListOfSelectedFullNames(application, "bondsHouseHoldSource", "bondsHouseHoldSource");
      List<String> retirementAccountOwners = getListOfSelectedFullNames(application, "retirementAccountsHouseHoldSource", "retirementAccountsHouseHoldSource");
     var householdSubworkflow = ofNullable(getGroup(application.getApplicationData(), Group.HOUSEHOLD));
     List<String> allHouseholdNames = householdSubworkflow.map(subworkflow -> getApplicationInputsForSubworkflow(subworkflow, application)).orElse(emptyList());
    
     for(String fullName: allHouseholdNames) {
       List<String> investmentType = new ArrayList<String>();
       stockOwners.stream().forEach(name ->{
         if(name.equals(fullName)) {
           investmentType.add("stocks");
         }
       });
       bondOwners.stream().forEach(name ->{
         if(name.equals(fullName)) {
           investmentType.add("bonds");
         }
       });
       retirementAccountOwners.stream().forEach(name ->{
         if(name.equals(fullName)) {
           investmentType.add("retirement accounts");
         }
        });
       if(!investmentType.isEmpty()) {
           investmentOwners.add(new Investment(fullName, investmentType));
       }
       
     }
    }else {
      List<String> investmentType = getValues(application.getApplicationData().getPagesData(),INVESTMENT_TYPE_INDIVIDUAL);
      investmentType = investmentType.stream().map(String::toLowerCase).map(type->type.replace("_", " ")).collect(Collectors.toList());
      if (!investmentType.isEmpty()) {
    	  investmentOwners.add(new Investment(getFullName(application), investmentType));
      }
    }
    return investmentOwners;
  }
  @NotNull
  private List<String> getApplicationInputsForSubworkflow(Subworkflow subworkflow, Application application) {
   
    List<String> householdFullNames = new ArrayList<>();
    householdFullNames.add(getFullName(application));
    for (Iteration i : subworkflow) {
      var pageData = i.getPagesData();
      var pgFirstName = getFirstValue(pageData, HOUSEHOLD_INFO_FIRST_NAME);
      var pgLastName = getFirstValue(pageData, HOUSEHOLD_INFO_LAST_NAME);
      householdFullNames.add(pgFirstName+" "+pgLastName);
    }
    return householdFullNames;
  }
  
  public class Investment{
    String fullName = "";
    List<String> investmentType = List.of("");
    
    public Investment(String fullName, List<String> investmentType) {
      this.fullName = fullName;
      this.investmentType = investmentType;
    }
  }
  
}
