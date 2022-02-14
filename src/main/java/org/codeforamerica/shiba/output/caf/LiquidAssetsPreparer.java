package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ASSETS;
import java.util.List;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.springframework.stereotype.Component;

@Component
public class LiquidAssetsPreparer implements DocumentFieldPreparer {


  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    var liquidAssets = getFirstValue(application.getApplicationData().getPagesData(), ASSETS);
    return List.of(
        new DocumentField(
            "liquidAssets",
            "liquidAssetsValue",
            List.of(liquidAssets.isBlank() ? Money.ZERO.toString() : Money.parse(liquidAssets).toString()),
            DocumentFieldType.SINGLE_VALUE
        )
    );
  }
}
