package org.codeforamerica.shiba;

import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

  @SuppressWarnings({"NullableProblems", "ConstantConditions"})
  @Override
  public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
    YamlPropertiesFactoryBean factory = new UnlimitedYamlPropertiesFactoryBean();
    factory.setResources(encodedResource.getResource());
    Properties properties = factory.getObject();

    return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
  }
}
