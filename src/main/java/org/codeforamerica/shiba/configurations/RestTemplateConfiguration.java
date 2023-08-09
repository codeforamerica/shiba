package org.codeforamerica.shiba.configurations;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
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
    SSLConnectionSocketFactory clientConnect = new SSLConnectionSocketFactory(sslContext,
        NoopHostnameVerifier.INSTANCE);
    
    Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
    socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .register("https", clientConnect)
        .build();  
    
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    connectionManager.setMaxTotal(5);
    // This client is for internal connections so only one route is expected
    connectionManager.setDefaultMaxPerRoute(1);
    CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
    return new RestTemplate(factory);
  }
}
