package org.codeforamerica.shiba.configurations;

import java.nio.file.Paths;

import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSLConfiguration {

  @Bean
  SSLContextBuilder sslContextBuilder(@Value("${client.keystore}") String keystore,
      @Value("${client.keystore-password}") String keystorePassword,
      @Value("${client.truststore}") String truststore,
      @Value("${client.truststore-password}") String truststorePassword
  ) throws Exception {
    return SSLContexts.custom()
        .loadKeyMaterial(Paths.get(keystore).toFile(), keystorePassword.toCharArray(),
            keystorePassword.toCharArray())
        .loadTrustMaterial(Paths.get(truststore).toFile(), truststorePassword.toCharArray());
  }
}
