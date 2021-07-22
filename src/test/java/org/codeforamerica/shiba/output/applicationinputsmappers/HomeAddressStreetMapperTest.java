package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.caf.HomeAddressStreetMapper;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeAddressStreetMapperTest {

    private final FeatureFlagConfiguration featureFlagConfiguration = mock(FeatureFlagConfiguration.class);
    private final HomeAddressStreetMapper mapper = new HomeAddressStreetMapper(featureFlagConfiguration);
    private final PageData homeAddressData = new PageData();
    private final PageData verifyHomeAddressData = new PageData();
    private final ApplicationData applicationData = new ApplicationData();
    private final Application application = Application.builder().applicationData(applicationData).build();

    @BeforeEach
    void setUp() {
        PagesData pagesData = new PagesData();
        pagesData.put("homeAddress", homeAddressData);
        pagesData.put("homeAddressValidation", verifyHomeAddressData);
        applicationData.setPagesData(pagesData);

        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.ON);
    }

    @Test
    void shouldSayNotPermanentWhenClientDoesNotHaveAPermanentAddress() {
        homeAddressData.put("streetAddress", InputData.builder().value(List.of("")).build());
        homeAddressData.put("isHomeless", InputData.builder().value(List.of("true")).build());

        List<ApplicationInput> map = mapper.map(application, null, null, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of("No permanent address"),
                ApplicationInputType.SINGLE_VALUE));
    }


    @Test
    void shouldIncludeAddressNotPermanentWhen_clientDoNotHaveAPermanentAddress_andProvidedHomeAddress() {
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);

        String streetAddress = "street address";
        homeAddressData.put("streetAddress", InputData.builder().value(List.of(streetAddress)).build());
        homeAddressData.put("isHomeless", InputData.builder().value(List.of("true")).build());
        verifyHomeAddressData.put("useEnrichedAddress", InputData.builder().value(List.of("false")).build());

        List<ApplicationInput> map = mapper.map(application, null, null, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of(streetAddress + " (not permanent)"),
                ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldIncludeAddressNotPermanentWhen_clientDoNotHaveAPermanentAddress_andProvidedHomeAddress_usingEnrichedStreetAddress() {
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);

        String streetAddress = "enrichedStreetAddress";
        homeAddressData.put("streetAddress", InputData.builder().value(List.of("originalStreetAddress")).build());
        homeAddressData.put("enrichedStreetAddress", InputData.builder().value(List.of(streetAddress)).build());
        homeAddressData.put("isHomeless", InputData.builder().value(List.of("true")).build());
        verifyHomeAddressData.put("useEnrichedAddress", InputData.builder().value(List.of("true")).build());

        List<ApplicationInput> map = mapper.map(application, null, null, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of(streetAddress + " (not permanent)"),
                ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldIncludePermanentAddressWhen_clientProvidedAddress_andStateTheAddressIsPermanent() {
        homeAddressData.put("isHomeless", InputData.builder().value(List.of()).build());
        String streetAddress = "street address";
        homeAddressData.put("streetAddress", InputData.builder().value(List.of(streetAddress)).build());
        verifyHomeAddressData.put("useEnrichedAddress", InputData.builder().value(List.of("false")).build());

        List<ApplicationInput> map = mapper.map(application, null, null, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of(streetAddress),
                ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldIncludePermanentAddressWhen_clientProvidedAddress_andStateTheAddressIsPermanent_usingEnrichedStreetAddress() {
        homeAddressData.put("isHomeless", InputData.builder().value(List.of()).build());
        String streetAddress = "enrichedStreetAddress";
        homeAddressData.put("streetAddress", InputData.builder().value(List.of("originalStreetAddress")).build());
        homeAddressData.put("enrichedStreetAddress", InputData.builder().value(List.of(streetAddress)).build());
        verifyHomeAddressData.put("useEnrichedAddress", InputData.builder().value(List.of("true")).build());

        List<ApplicationInput> map = mapper.map(application, null, null, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of(streetAddress),
                ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldIncludeNoPermanentAddressWhen_clientDoNotProvideAddress() {
        homeAddressData.put("streetAddress", InputData.builder().value(List.of("")).build());
        homeAddressData.put("isHomeless", InputData.builder().value(List.of("true")).build());

        List<ApplicationInput> map = mapper.map(application, null, null, null);

        assertThat(map).contains(new ApplicationInput("homeAddress",
                "streetAddressWithPermanentAddress",
                List.of("No permanent address"),
                ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldNotIncludeApplicationInputs_whenThereIsNoHomeAddress() {
        applicationData.setPagesData(new PagesData());

        List<ApplicationInput> map = mapper.map(application, null, null, null);

        assertThat(map).isEmpty();
    }
}