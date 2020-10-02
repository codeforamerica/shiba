package org.codeforamerica.shiba;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SmartyStreetClient implements LocationClient {
    private final String authId;
    private final String authToken;
    private final String smartyStreetUrl;

    public SmartyStreetClient(
            @Value("${smarty-street-auth-id}") String authId,
            @Value("${smarty-street-auth-token}") String authToken,
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
                .onErrorResume(error -> Mono.empty())
                .blockOptional();

        return response
                .flatMap(verifyStreetResponse -> verifyStreetResponse.stream().findFirst())
                .map(addressCandidate -> addressCandidate.getMetadata().getCountyName());
    }

    @Override
    public Optional<Address> validateAddress(Address address) {
        WebClient webClient = WebClient.builder().baseUrl(smartyStreetUrl).build();
        Optional<SmartyStreetVerifyStreetResponse> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("auth-id", authId)
                        .queryParam("auth-token", authToken)
                        .queryParam("street", address.getStreet())
                        .queryParam("city", address.getCity())
                        .queryParam("state", address.getState())
                        .queryParam("zipcode", address.getZipcode())
                        .queryParam("secondary", address.getApartmentNumber())
                        .queryParam("candidates", 1).build())
                .retrieve()
                .bodyToMono(SmartyStreetVerifyStreetResponse.class)
                .onErrorResume(error -> Mono.empty())
                .blockOptional();
        return response
                .flatMap(verifyStreetResponse -> verifyStreetResponse.stream().findFirst())
                .map(addressCandidate -> {
                    Components components = addressCandidate.getComponents();
                    return new Address(
                            String.format(
                                    "%s %s %s",
                                    components.getPrimaryNumber(), components.getStreetName(), components.getStreetSuffix()
                            ),
                            components.getCityName(),
                            components.getStateAbbreviation(),
                            components.getZipcode() + "-" + components.getPlus4Code(),
                            Stream.of(components.getSecondaryDesignator(), components.getSecondaryNumber())
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.joining(" "))
                    );
                });
    }
}
