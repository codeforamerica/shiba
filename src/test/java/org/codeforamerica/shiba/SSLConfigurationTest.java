package org.codeforamerica.shiba;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class SSLConfigurationTest {
    @Autowired
    private SSLContextBuilder sslContextBuilder;

    private WireMockServer wireMockServer;

    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        httpClient = HttpClients.custom()
                .setSSLContext(sslContextBuilder
                        .loadTrustMaterial(new TrustSelfSignedStrategy())
                        .build())
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        WireMockConfiguration options = WireMockConfiguration.wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .needClientAuth(true)
                .trustStorePath("src/test/resources/truststore.jks")
                .trustStorePassword("changeit");
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldSignRequestsWithConfiguredKeystore() throws IOException {
        stubFor(get(urlEqualTo("/somePath"))
                .willReturn(aResponse().withStatus(200)));

        HttpResponse httpResponse = httpClient.execute(HttpHost.create(wireMockServer.baseUrl()), new HttpGet("/somePath"));

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }
}
