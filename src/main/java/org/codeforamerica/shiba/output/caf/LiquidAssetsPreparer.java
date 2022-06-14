package org.codeforamerica.shiba.output.caf;

import java.util.List;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.springframework.stereotype.Component;

@Component
public class LiquidAssetsPreparer implements DocumentFieldPreparer {

	private final LiquidAssetsCalculator liquidAssetsCalculator;
	//private final ApplicationDataParser applicationDataParser;
	
	LiquidAssetsPreparer(){
		this.liquidAssetsCalculator = new LiquidAssetsCalculator();
	//	this.applicationDataParser = new ApplicationDataParser();
	}

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
	  
	String x =  ApplicationDataParser.getFirstValue(application.getApplicationData().getPagesData(), Field.ASSETS);
	System.out.println("x = " + x);
    String totalAmount = liquidAssetsCalculator.findTotalLiquidAssets(application.getApplicationData());
    System.out.println("LiquidAssetsPreparer totalAmount = " + totalAmount);//TODO emj
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
