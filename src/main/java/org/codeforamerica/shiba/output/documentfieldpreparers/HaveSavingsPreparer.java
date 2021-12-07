package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ASSETS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SAVINGS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.ArrayList;
import java.util.List;
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
      List<String> savingsAmountValue = getValues(application.getApplicationData().getPagesData(), ASSETS);
      String savingsAmount = savingsAmountValue.isEmpty() ? "" : savingsAmountValue.get(0);

      boolean shouldBeNo = hasSavings.equals("true") && savingsAmount.equals("0");

      DocumentField hasSavingsDocumentFieldDecision = shouldBeNo ?
          new DocumentField("savings", "haveSavings", List.of("No"),  DocumentFieldType.SINGLE_VALUE) :
          new DocumentField("savings", "haveSavings", List.of(hasSavings), DocumentFieldType.SINGLE_VALUE);
      haveSavingsDocumentField.add(hasSavingsDocumentFieldDecision);
    }
    return haveSavingsDocumentField;
  }
}
