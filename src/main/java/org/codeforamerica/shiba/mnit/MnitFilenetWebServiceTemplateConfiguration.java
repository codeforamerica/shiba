package org.codeforamerica.shiba.mnit;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
public class MnitFilenetWebServiceTemplateConfiguration {

  @Bean
  WebServiceTemplate filenetWebServiceTemplate(WebServiceTemplateBuilder webServiceTemplateBuilder,
      SSLContextBuilder sslContextBuilder,
      @Value("${mnit-filenet.username}") String username,
      @Value("${mnit-filenet.password}") String password,
      @Value("${mnit-filenet.jaxb-context-path}") String jaxbContextPath,
      @Value("${mnit-filenet.url}") String url,
      @Value("${mnit-filenet.timeout-seconds}") long timeoutSeconds )
      throws KeyManagementException, NoSuchAlgorithmException {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setContextPath(jaxbContextPath);
    String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
    int timeoutMillis = (int) TimeUnit.MILLISECONDS.convert(timeoutSeconds, TimeUnit.SECONDS);
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(timeoutMillis)
        .setConnectTimeout(timeoutMillis)
        .setSocketTimeout(timeoutMillis)
        .build();
    HttpClient httpClient = HttpClients.custom()
        .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
        .setSSLContext(sslContextBuilder.build())
        .setDefaultHeaders(
            List.of(new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth))))
        .setDefaultRequestConfig(requestConfig)
        .build();
    return webServiceTemplateBuilder
        .setDefaultUri(url)
        .setMarshaller(jaxb2Marshaller)
        .setUnmarshaller(jaxb2Marshaller)
        .messageSenders(new HttpComponentsMessageSender(httpClient))
        .build();
  }
}