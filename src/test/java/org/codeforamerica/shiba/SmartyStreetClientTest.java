package org.codeforamerica.shiba;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
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
    void retrieveCountyByAddress() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("something", new PageData())));

        String street = "1725 Slough Avenue";
        String city = "Scranton";
        String state = "PA";
        String zipcode = "91402";
        Address address = new Address(street, city, state, zipcode);
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(okJson("[{\"metadata\": {\"county_name\": \"Cook\"}}]"))
        );

        String county = smartyStreetClient.getCounty(address).get();

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/"))
                .withQueryParam("auth-id", equalTo(authId))
                .withQueryParam("auth-token", equalTo(authToken))
                .withQueryParam("street", equalTo(street))
                .withQueryParam("city", equalTo(city))
                .withQueryParam("state", equalTo(state))
                .withQueryParam("zipcode", equalTo(zipcode))
                .withQueryParam("candidates", equalTo("1"))
        );

        assertThat(county).isEqualTo("Cook");
    }

    @Test
    void shouldReturnEmptyOptional_whenNoAddressIsReturned() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("something", new PageData())));

        String street = "1725 Slough Avenue";
        String city = "Scranton";
        String state = "PA";
        String zipcode = "91402";
        Address address = new Address(street, city, state, zipcode);
        wireMockServer.stubFor(get(anyUrl()).willReturn(okJson("[]")));

        Optional<String> county = smartyStreetClient.getCounty(address);

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
        Address address = new Address(street, city, state, zipcode);
        wireMockServer.stubFor(get(anyUrl()).willReturn(status(200)));

        Optional<String> county = smartyStreetClient.getCounty(address);

        assertThat(county).isEmpty();
    }
}