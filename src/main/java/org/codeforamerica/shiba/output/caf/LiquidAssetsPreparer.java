package org.codeforamerica.shiba.output.caf;

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

	private final LiquidAssetsCalculator liquidAssetsCalculator;
	
	LiquidAssetsPreparer(){
		this.liquidAssetsCalculator = new LiquidAssetsCalculator();
	}

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    String totalAmount = liquidAssetsCalculator.findTotalLiquidAssets(application.getApplicationData());
    System.out.println("totalAmount = " + totalAmount);
    return List.of(
        new DocumentField(
            "liquidAssets",
            "liquidAssetsValue",
            List.of(totalAmount.isBlank() ? Money.ZERO.toString() : Money.parse(totalAmount).toString()),// List of one element
            DocumentFieldType.SINGLE_VALUE
        )
    );
  }
}
