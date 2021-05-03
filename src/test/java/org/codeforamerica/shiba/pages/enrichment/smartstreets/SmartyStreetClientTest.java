package org.codeforamerica.shiba.pages.enrichment.smartstreets;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
class SmartyStreetClientTest {
    SmartyStreetClient smartyStreetClient;

    WireMockServer wireMockServer;

    int port;

    private final String authId = "someAuthId";
    private final String authToken = "someAuthToken";

    @BeforeEach
    void setUp() {
        WireMockConfiguration options = wireMockConfig().dynamicPort();
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
        port = wireMockServer.port();
        WireMock.configureFor(port);

        smartyStreetClient = new SmartyStreetClient(
                authId,
                authToken,
                "http://localhost:" + port
        );
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldReturnEmptyOptional_whenNoAddressIsReturned() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("something", new PageData())));

        String street = "1725 Slough Avenue";
        String city = "Scranton";
        String state = "PA";
        String zipcode = "91402";
        Address address = new Address(street, city, state, zipcode, null, null);
        wireMockServer.stubFor(get(anyUrl()).willReturn(okJson("[]")));

        Optional<Address> county = smartyStreetClient.validateAddress(address);

        assertThat(county).isEmpty();
    }

    @Test
    void returnEmptyOptional_whenWeGetAnEmptyResponse() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("something", new PageData())));

        String street = "1725 Slough Avenue";
        String city = "Scranton";
        String state = "PA";
        String zipcode = "91402";
        Address address = new Address(street, city, state, zipcode, null, null);
        wireMockServer.stubFor(get(anyUrl()).willReturn(status(200)));

        Optional<Address> county = smartyStreetClient.validateAddress(address);

        assertThat(county).isEmpty();
    }

    @Test
    void returnEmptyOptional_whenResponseCodeIsNot2XX() {
        String street = "1725 Slough Avenue";
        String city = "Scranton";
        String state = "PA";
        String zipcode = "91402";
        Address address = new Address(street, city, state, zipcode, null, null);
        wireMockServer.stubFor(get(anyUrl()).willReturn(status(400)));

        Optional<Address> county = smartyStreetClient.validateAddress(address);

        assertThat(county).isEmpty();
    }

    @Test
    void returnAddress_whenVerifyAddressFindsCandidate() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("something", new PageData())));

        String street = "1725 Slough Avenue";
        String city = "Scranton";
        String state = "PA";
        String zipcode = "91402";
        String apartmentNumber = "apt 1104";
        Address address = new Address(street, city, state, zipcode, apartmentNumber, null);
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(okJson("""
                        [
                          {
                            "metadata": {"county_name": "Cook"},
                            "components": {
                              "primary_number": "222",
                              "street_name": "Merchandise Mart",
                              "street_suffix": "Plz",
                              "city_name": "Chicago",
                              "default_city_name": "Chicago",
                              "state_abbreviation": "IL",
                              "zipcode": "60654",
                              "plus4_code": "1103",
                              "secondary_number": "1104",
                              "secondary_designator": "apt"
                            },
                           "delivery_line_1": "222 Merchandise Mart Plz"
                          }
                        ]""")));

        Address resultAddress = smartyStreetClient.validateAddress(address).get();

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/"))
                .withQueryParam("auth-id", equalTo(authId))
                .withQueryParam("auth-token", equalTo(authToken))
                .withQueryParam("street", equalTo(street))
                .withQueryParam("city", equalTo(city))
                .withQueryParam("state", equalTo(state))
                .withQueryParam("zipcode", equalTo(zipcode))
                .withQueryParam("candidates", equalTo("1"))
                .withQueryParam("secondary", equalTo(apartmentNumber))
        );

        assertThat(resultAddress).isEqualTo(new Address(
                "222 Merchandise Mart Plz",
                "Chicago",
                "IL",
                "60654-1103",
                "apt 1104",
                "Cook"));
    }

    @Test
    void returnAddressWithoutSecondary_whenVerifyAddressFindsCandidate() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("something", new PageData())));

        String street = "1725 Slough Avenue";
        String city = "Scranton";
        String state = "PA";
        String zipcode = "91402";
        Address address = new Address(street, city, state, zipcode, null, null);
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(okJson("""
                        [
                          {
                            "metadata": {"county_name": "Cook"},
                            "components": {
                              "primary_number": "222",
                              "street_name": "Merchandise Mart",
                              "street_suffix": "Plz",
                              "city_name": "Chicago",
                              "default_city_name": "Chicago",
                              "state_abbreviation": "IL",
                              "zipcode": "60654",
                              "plus4_code": "1103"
                            },
                           "delivery_line_1": "222 Merchandise Mart Plz"
                          }
                        ]""")));

        Address resultAddress = smartyStreetClient.validateAddress(address).get();

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/"))
                .withQueryParam("auth-id", equalTo(authId))
                .withQueryParam("auth-token", equalTo(authToken))
                .withQueryParam("street", equalTo(street))
                .withQueryParam("city", equalTo(city))
                .withQueryParam("state", equalTo(state))
                .withQueryParam("zipcode", equalTo(zipcode))
                .withQueryParam("candidates", equalTo("1"))
                .withQueryParam("secondary", equalTo(""))
        );

        assertThat(resultAddress).isEqualTo(new Address("222 Merchandise Mart Plz", "Chicago", "IL", "60654-1103", "", "Cook"));
    }
}