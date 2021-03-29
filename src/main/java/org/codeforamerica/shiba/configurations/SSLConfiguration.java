package org.codeforamerica.shiba.configurations;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class SSLConfiguration {
    @Bean
    SSLContextBuilder sslContextBuilder(@Value("${client.keystore}") String keystore,
                                        @Value("${client.keystore-password}") String keystorePassword,
                                        @Value("${client.truststore}") String truststore,
                                        @Value("${client.truststore-password}") String truststorePassword
                                        ) throws Exception {
        return SSLContexts.custom()
                .loadKeyMaterial(Paths.get(keystore).toFile(), keystorePassword.toCharArray(), keystorePassword.toCharArray())
                .loadTrustMaterial(Paths.get(truststore).toFile(), truststorePassword.toCharArray());
    }
}
