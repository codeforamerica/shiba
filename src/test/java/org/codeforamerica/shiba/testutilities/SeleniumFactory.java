package org.codeforamerica.shiba.testutilities;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.FactoryBean;

public class SeleniumFactory implements FactoryBean<RemoteWebDriver> {

  private final Path tempdir;
  private RemoteWebDriver driver;

  public SeleniumFactory(Path tempdir) {
    this.tempdir = tempdir;
  }

  @Override
  public RemoteWebDriver getObject() {
    return driver;
  }

  @Override
  public Class<RemoteWebDriver> getObjectType() {
    return RemoteWebDriver.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void start() throws IOException {
    WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    HashMap<String, Object> chromePrefs = new HashMap<>();
    chromePrefs.put("download.default_directory", tempdir.toString());
    options.setExperimentalOption("prefs", chromePrefs);
    options.addArguments("--window-size=1280,1600");
    options.addArguments("--headless=new");
    driver = new ChromeDriver(options);
  }

  public void stop() {
    if (driver != null) {
      driver.close();
      driver.quit();
    }
  }
}
