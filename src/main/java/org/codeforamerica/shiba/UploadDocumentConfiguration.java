package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class UploadDocumentConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.servlet.multipart")
    Map<String, String> uploadDocumentProperties() {
        return new HashMap<>();
    }

    /**
     * Return the int value of max file size (expected format "50MB").
     *
     * @return int value of max-file-size property
     */
    public Integer getMaxFilesize() {
        String maxFileSize = uploadDocumentProperties().get("max-file-size");
        return Integer.parseInt(maxFileSize.substring(0, maxFileSize.length()-2));
    }

    /**
     * Return the value of max file size in bytes.
     *
     * @return value of max-file-size in bytes
     */
    public long getMaxFilesizeInBytes() {
        return (long) (getMaxFilesize() * Math.pow(1024, 2));
    }
}
