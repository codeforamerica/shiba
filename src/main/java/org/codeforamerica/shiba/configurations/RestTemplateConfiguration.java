package org.codeforamerica.shiba.configurations;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  @Value("${client.truststore-password}")
  private String truststorePassword;
  @Value("${client.truststore}")
  private String truststore;

  @Bean
  public RestTemplate restTemplate()
      throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {

    SSLContext sslContext = SSLContexts.custom()
        .loadTrustMaterial(Paths.get(truststore).toFile(), truststorePassword.toCharArray())
        .build();
    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
        NoopHostnameVerifier.INSTANCE);

    HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
        httpClient);

    return new RestTemplate(factory);
  }
}
