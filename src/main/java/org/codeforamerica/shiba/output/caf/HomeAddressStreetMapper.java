package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class HomeAddressStreetMapper implements ApplicationInputsMapper {

  private final static String NO_PERMANENT_ADDRESS = "No permanent address";

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    PagesData pagesData = application.getApplicationData().getPagesData();
    PageData homeAddressPageData = pagesData.getPage("homeAddress");
    if (homeAddressPageData == null) {
      return List.of();
    }

    if (String.join("", homeAddressPageData.get("streetAddress").getValue()).isBlank()) {
      return List.of(new ApplicationInput(
          "homeAddress",
          "streetAddressWithPermanentAddress",
          List.of(NO_PERMANENT_ADDRESS),
          ApplicationInputType.SINGLE_VALUE
      ));
    }

    String usesEnriched = pagesData
        .getPageInputFirstValue("homeAddressValidation", "useEnrichedAddress");
    String streetInputName =
        Boolean.parseBoolean(usesEnriched) ? "enrichedStreetAddress" : "streetAddress";

    String value = homeAddressPageData.get(streetInputName).getValue().get(0);

    return List.of(
        new ApplicationInput(
            "homeAddress",
            "streetAddressWithPermanentAddress",
            List.of(value),
            ApplicationInputType.SINGLE_VALUE
        )
    );
  }
}