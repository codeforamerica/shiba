package org.codeforamerica.shiba;

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
                                        @Value("${client.keystore-password}") String keystorePassword) throws Exception {
        return SSLContexts.custom()
                .loadKeyMaterial(Paths.get(keystore).toFile(), keystorePassword.toCharArray(), keystorePassword.toCharArray());
    }
}
