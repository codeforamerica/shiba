package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResidentialAddressStreetMapper implements ApplicationInputsMapper {
    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        InputData streetAddressInput = application.getApplicationData().getPagesData().getPage("homeAddress").get("streetAddress");
        if (String.join("", streetAddressInput.getValue()).isBlank()) {
            return List.of(new ApplicationInput(
                    "homeAddress",
                    "streetAddressWithPermanentAddress",
                    List.of("No permanent address"),
                    ApplicationInputType.SINGLE_VALUE
            ));
        }

        String notPermanentAddressIndicator = application.getApplicationData().getPagesData().getPage("homeAddress").get("isHomeless").getValue().isEmpty() ?
                "" : " (not permanent)";
        return List.of(new ApplicationInput(
                "homeAddress",
                "streetAddressWithPermanentAddress",
                List.of(streetAddressInput.getValue().get(0) + notPermanentAddressIndicator),
                ApplicationInputType.SINGLE_VALUE
        ));
    }
}
