package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HomeAddressStreetMapper implements ApplicationInputsMapper {
    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        PageData homeAddressPageData = application.getApplicationData().getPagesData().getPage("homeAddress");
        if (homeAddressPageData == null) {
            return List.of();
        }
        if (String.join("", homeAddressPageData.get("streetAddress").getValue()).isBlank()) {
            return List.of(new ApplicationInput(
                    "homeAddress",
                    "streetAddressWithPermanentAddress",
                    List.of("No permanent address"),
                    ApplicationInputType.SINGLE_VALUE
            ));
        }

        String notPermanentAddressIndicator = homeAddressPageData.get("isHomeless").getValue().isEmpty() ?
                "" : " (not permanent)";
        String usesEnriched = application.getApplicationData().getPagesData()
                .getPage("homeAddressValidation")
                .get("useEnrichedAddress")
                .getValue().get(0);
        String streetInputName = Boolean.parseBoolean(usesEnriched) ? "enrichedStreetAddress" : "streetAddress";
        String value = homeAddressPageData.get(streetInputName).getValue().get(0) + notPermanentAddressIndicator;
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
