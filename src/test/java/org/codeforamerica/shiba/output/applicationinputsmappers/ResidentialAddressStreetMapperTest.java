package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResidentialAddressStreetMapperTest {
    @Test
    void shouldIncludeAddressNotPermanentWhen_clientDoNotHaveAPermanentAddress_AndProvidedHomeAddress() {
        ResidentialAddressStreetMapper mapper = new ResidentialAddressStreetMapper();
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData homeAddressData = new PageData();
        homeAddressData.put("zipCode", InputData.builder().value(List.of("something")).build());
        homeAddressData.put("city", InputData.builder().value(List.of("something")).build());
        String streetAddress = "street address";
        homeAddressData.put("streetAddress", InputData.builder().value(List.of(streetAddress)).build());
        homeAddressData.put("isHomeless", InputData.builder().value(List.of("true")).build());

        pagesData.put("homeAddress", homeAddressData);
        applicationData.setPagesData(pagesData);
        Application application = Application.builder()
                .id("")
                .completedAt(null)
                .applicationData(applicationData)
                .county(null)
                .timeToComplete(null)
                .build();
        List<ApplicationInput> map = mapper.map(application, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of(streetAddress + " (not permanent)"),
                ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldIncludeNoPermanentAddressWhen_clientDoNotProvideAddress() {
        ResidentialAddressStreetMapper mapper = new ResidentialAddressStreetMapper();
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData homeAddressData = new PageData();
        homeAddressData.put("streetAddress", InputData.builder().value(List.of("")).build());
        homeAddressData.put("isHomeless", InputData.builder().value(List.of("true")).build());

        pagesData.put("homeAddress", homeAddressData);
        applicationData.setPagesData(pagesData);
        Application application = Application.builder()
                .id("")
                .completedAt(null)
                .applicationData(applicationData)
                .county(null)
                .timeToComplete(null)
                .build();
        List<ApplicationInput> map = mapper.map(application, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of("No permanent address"),
                ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldIncludePermanentAddressWhen_clientProvidedAddress_andStateTheAddressIsPermanent() {
        ResidentialAddressStreetMapper mapper = new ResidentialAddressStreetMapper();
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData homeAddressData = new PageData();
        homeAddressData.put("isHomeless", InputData.builder().value(List.of()).build());
        homeAddressData.put("zipCode", InputData.builder().value(List.of("something")).build());
        homeAddressData.put("city", InputData.builder().value(List.of("something")).build());
        String streetAddress = "street address";
        homeAddressData.put("streetAddress", InputData.builder().value(List.of(streetAddress)).build());

        pagesData.put("homeAddress", homeAddressData);
        applicationData.setPagesData(pagesData);
        Application application = Application.builder()
                .id("")
                .completedAt(null)
                .applicationData(applicationData)
                .county(null)
                .timeToComplete(null)
                .build();
        List<ApplicationInput> map = mapper.map(application, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of(streetAddress),
                ApplicationInputType.SINGLE_VALUE));
    }
}