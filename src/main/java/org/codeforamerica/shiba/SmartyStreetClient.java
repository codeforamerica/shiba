package org.codeforamerica.shiba;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
public class SmartyStreetClient implements LocationClient {
    private final String authId;
    private final String authToken;
    private final String smartyStreetUrl;

    public SmartyStreetClient(
            @Value("${smarty-street-authId}") String authId,
            @Value("${smarty-street-authToken}") String authToken,
            @Value("${smarty-street-url}") String smartyStreetUrl) {
        this.authId = authId;
        this.authToken = authToken;
        this.smartyStreetUrl = smartyStreetUrl;
    }

    @Override
    public Optional<String> getCounty(Address address) {
        WebClient webClient = WebClient.builder().baseUrl(smartyStreetUrl).build();
        Optional<SmartyStreetVerifyStreetResponse> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("auth-id", authId)
                        .queryParam("auth-token", authToken)
                        .queryParam("street", address.getStreet())
                        .queryParam("city", address.getCity())
                        .queryParam("state", address.getState())
                        .queryParam("zipcode", address.getZipcode())
                        .queryParam("candidates", 1).build())
                .retrieve()
                .bodyToMono(SmartyStreetVerifyStreetResponse.class)
                .blockOptional();

        return response
                .flatMap(verifyStreetResponse -> verifyStreetResponse.stream().findFirst())
                .map(addressCandidate -> addressCandidate.getMetadata().getCountyName());
    }
}
