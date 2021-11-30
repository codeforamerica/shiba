package org.codeforamerica.shiba.testutilities;

import java.util.Comparator;
import org.codeforamerica.shiba.journeys.JourneyTest;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;
import org.springframework.boot.test.context.SpringBootTest;

@SuppressWarnings("unused")
public class TestClassOrderer implements ClassOrderer {

  @Override
  public void orderClasses(ClassOrdererContext classOrdererContext) {
    classOrdererContext.getClassDescriptors()
        .sort(Comparator.comparingInt(TestClassOrderer::getOrder));
  }

  private static int getOrder(ClassDescriptor classDescriptor) {
    if (classDescriptor.getDisplayName().equals("FullFlowJourneyTest")) {
      // First, the full flow journey test
      return 1;
    } else if (classDescriptor.getTestClass().getSuperclass().equals(JourneyTest.class)) {
      // Second: journey tests
      return 2;
    } else if (classDescriptor.getTestClass().getSuperclass().equals(AbstractBasePageTest.class)) {
      // Third: other tests that start up chromedriver
      return 3;
    } else if (classDescriptor.findAnnotation(SpringBootTest.class).isPresent()) {
      // Fourth: other tests that have the @SpringBootTest annotation
      return 4;
    } else {
      // Fifth: everything else
      return 5;
    }
  }
}
