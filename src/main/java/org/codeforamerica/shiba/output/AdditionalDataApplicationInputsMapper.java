package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.ApplicationData;
import org.codeforamerica.shiba.pages.FormData;
import org.codeforamerica.shiba.pages.PagesConfiguration;
import org.codeforamerica.shiba.pages.PagesData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdditionalDataApplicationInputsMapper implements ApplicationInputsMapper {
    private final PagesConfiguration pagesConfiguration;

    public AdditionalDataApplicationInputsMapper(PagesConfiguration pagesConfiguration) {
        this.pagesConfiguration = pagesConfiguration;
    }

    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        PagesData pagesData = data.getPagesData();
        return pagesConfiguration.getPages().entrySet().stream()
                .filter(entry -> pagesData.getPage(entry.getKey()) != null)
                .flatMap(entry -> entry.getValue().getAdditionalData().stream()
                        .map(additionalDatum -> {
                            FormData page = pagesData.getPage(entry.getKey());
                            return new ApplicationInput(entry.getKey(),
                                    page.get(additionalDatum.getName()).getValue(),
                                    additionalDatum.getName(),
                                    ApplicationInputType.ENUMERATED_SINGLE_VALUE);
                        }))
                .collect(Collectors.toList());
    }
}
