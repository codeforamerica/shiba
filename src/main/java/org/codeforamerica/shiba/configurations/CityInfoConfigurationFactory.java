package org.codeforamerica.shiba.configurations;

import java.io.IOException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

public class CityInfoConfigurationFactory implements FactoryBean<CityInfoConfiguration> {

  @Value("city-to-zipcode-and-county-mapping.yaml")
  String configPath;

  @Override
  public CityInfoConfiguration getObject() throws IOException {
    ClassPathResource classPathResource = new ClassPathResource(configPath);

    LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setAllowDuplicateKeys(false);
    loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
    loaderOptions.setAllowRecursiveKeys(true);
    DumperOptions options = new DumperOptions();
    Yaml yaml = new Yaml(new Constructor(CityInfoConfiguration.class), new Representer(options),
        new DumperOptions(), loaderOptions);
    return yaml.load(classPathResource.getInputStream());
  }

  @Override
  public Class<?> getObjectType() {
    return CityInfoConfiguration.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
