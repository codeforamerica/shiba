package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.HomeAddressParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressEnrichmentTest {
    private final HomeAddressParser homeAddressParser = mock(HomeAddressParser.class);
    private final LocationClient locationClient = mock(LocationClient.class);
    HomeAddressEnrichment homeAddressValidationQuery = new HomeAddressEnrichment(
            homeAddressParser, locationClient
    );

    @Test
    void shouldCallAddressValidationOnAddressValidationPage() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData());
        Address address = new Address("street", "city", "CA", "02103", "ste 123");
        when(homeAddressParser.parse(applicationData)).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(Optional.of(address));

        EnrichmentResult enrichmentResult = homeAddressValidationQuery.process(applicationData);

        assertThat(enrichmentResult).containsEntry("enrichedStreetAddress", new InputData(List.of(address.getStreet())));
        assertThat(enrichmentResult).containsEntry("enrichedCity", new InputData(List.of(address.getCity())));
        assertThat(enrichmentResult).containsEntry("enrichedState", new InputData(List.of(address.getState())));
        assertThat(enrichmentResult).containsEntry("enrichedZipCode", new InputData(List.of(address.getZipcode())));
        assertThat(enrichmentResult).containsEntry("enrichedApartmentNumber", new InputData(List.of(address.getApartmentNumber())));
    }

    @Test
    void shouldNotIncludeValidatedAddress_whenLocationClientDoesNotReturnAnAddress() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData());
        Address address = new Address("street", "city", "CA", "02103", "");
        when(homeAddressParser.parse(applicationData)).thenReturn(address);
        when(locationClient.validateAddress(address)).thenReturn(empty());

        EnrichmentResult enrichmentResult = homeAddressValidationQuery.process(applicationData);

        assertThat(enrichmentResult).isEmpty();
    }

}