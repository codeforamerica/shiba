package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressEnrichmentTest {
    private final LocationClient locationClient = mock(LocationClient.class);
    private final HashMap<String, County> countyZipCodeMap = new HashMap<>();
    HomeAddressEnrichment homeAddressValidationQuery =
            new HomeAddressEnrichment(
                    locationClient,
                    countyZipCodeMap);

    @Test
    void shouldCallAddressValidationOnAddressValidationPage() {
        ApplicationData applicationData = new TestApplicationDataBuilder().withHomeAddress().build();
        Address expectedAddress = new Address(
                "street",
                "city",
                "CA",
                "02103",
                "ste 123",
                "someCounty");
        Address address = new Address(
                expectedAddress.getStreet(),
                expectedAddress.getCity(),
                expectedAddress.getState(),
                expectedAddress.getZipcode(),
                expectedAddress.getApartmentNumber(),
                null);
        when(locationClient.validateAddress(address)).thenReturn(Optional.of(expectedAddress));

        EnrichmentResult enrichmentResult = homeAddressValidationQuery.process(applicationData);

        assertThat(enrichmentResult).containsEntry("enrichedStreetAddress", new InputData(List.of(expectedAddress.getStreet())));
        assertThat(enrichmentResult).containsEntry("enrichedCity", new InputData(List.of(expectedAddress.getCity())));
        assertThat(enrichmentResult).containsEntry("enrichedState", new InputData(List.of(expectedAddress.getState())));
        assertThat(enrichmentResult).containsEntry("enrichedZipCode", new InputData(List.of(expectedAddress.getZipcode())));
        assertThat(enrichmentResult).containsEntry("enrichedApartmentNumber", new InputData(List.of(expectedAddress.getApartmentNumber())));
        assertThat(enrichmentResult).containsEntry("enrichedCounty", new InputData(List.of(expectedAddress.getCounty())));
    }

    @Test
    void shouldIncludeCountyFromMapping_whenLocationClientDoesNotReturnAnAddress() {
        ApplicationData applicationData = new TestApplicationDataBuilder().withHomeAddress().build();
        String zipCode = "02103";
        countyZipCodeMap.put(zipCode, County.Olmsted);
        Address address = new Address("street", "city", "CA", zipCode, "", null);
        when(locationClient.validateAddress(address)).thenReturn(empty());

        EnrichmentResult enrichmentResult = homeAddressValidationQuery.process(applicationData);

        assertThat(enrichmentResult).containsOnly(Map.entry("enrichedCounty", new InputData(List.of("Olmsted"))));
    }

    @Test
    void shouldNotIncludeCountyFromMapping_whenLocationClientDoesNotReturnAnAddress_andMappingDoesNotExistForSuppliedZipCode() {
        ApplicationData applicationData = new TestApplicationDataBuilder().withHomeAddress().build();
        Address address = new Address("street", "city", "CA", "02103", "", null);
        when(locationClient.validateAddress(address)).thenReturn(empty());

        EnrichmentResult enrichmentResult = homeAddressValidationQuery.process(applicationData);

        assertThat(enrichmentResult).isEmpty();
    }
}