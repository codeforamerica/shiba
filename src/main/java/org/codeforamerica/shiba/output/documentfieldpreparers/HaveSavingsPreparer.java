package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLICANT_ASSETS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_ASSETS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SAVINGS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class HaveSavingsPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {

    List<DocumentField> haveSavingsDocumentField = new ArrayList<>();
    List<String> hasSavingsValue = getValues(application.getApplicationData().getPagesData(), SAVINGS);
    if (!hasSavingsValue.isEmpty()) {
      String hasSavings = getValues(application.getApplicationData().getPagesData(), SAVINGS).get(0);
      List<String> householdSavingsAmountValue = getValues(application.getApplicationData().getPagesData(), HOUSEHOLD_ASSETS);
      List<String> applicantSavingsAmountValue = getValues(application.getApplicationData().getPagesData(), APPLICANT_ASSETS);
      List<String> savingsAmountValue = Stream.concat(householdSavingsAmountValue.parallelStream(), applicantSavingsAmountValue.parallelStream())
    		  .collect(Collectors.toList());
      
      String savingsAmount = savingsAmountValue.isEmpty() ? "" : savingsAmountValue.get(0);

      boolean shouldBeNo = hasSavings.equals("true") && savingsAmount.equals("0");

      DocumentField hasSavingsDocumentFieldDecision = shouldBeNo ?
          new DocumentField("savings", "haveNonZeroSavings", List.of("No"),  DocumentFieldType.SINGLE_VALUE) :
          new DocumentField("savings", "haveNonZeroSavings", List.of(hasSavings), DocumentFieldType.SINGLE_VALUE);
      DocumentField savingsAmountFieldDecision = hasSavings.equals("false") ?
              new DocumentField("savings", "cashAmount", "0",  DocumentFieldType.SINGLE_VALUE) :
              new DocumentField("savings", "cashAmount", savingsAmountValue, DocumentFieldType.SINGLE_VALUE);
      haveSavingsDocumentField.add(hasSavingsDocumentFieldDecision);
      haveSavingsDocumentField.add(savingsAmountFieldDecision);
    }
    return haveSavingsDocumentField;
  }
}
