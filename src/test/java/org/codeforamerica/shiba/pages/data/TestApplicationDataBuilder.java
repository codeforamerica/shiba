package org.codeforamerica.shiba.pages.data;


import java.time.Instant;
import java.util.List;

/**
 * Helper class for building test application data
 */
public class TestApplicationDataBuilder {
    private final ApplicationData applicationData = new ApplicationData();

    public ApplicationData build() {
        return applicationData;
    }
    
    public TestApplicationDataBuilder base() {
        applicationData.setId("12345");
        applicationData.setStartTimeOnce(Instant.now());
        return this;
    }

    public TestApplicationDataBuilder withApplicantPrograms(List<String> programs) {
        PageData programPage = new PageData();
        programPage.put("programs", InputData.builder().value(programs).build());
        applicationData.getPagesData().put("choosePrograms", programPage);
        return this;
    }

    public TestApplicationDataBuilder withPersonalInfo() {
        PageData personalInfo = new PageData();
        personalInfo.put("firstName", InputData.builder().value(List.of("Jane")).build());
        personalInfo.put("lastName", InputData.builder().value(List.of("Doe")).build());
        personalInfo.put("otherName", InputData.builder().value(List.of("")).build());
        personalInfo.put("dateOfBirth", InputData.builder().value(List.of("10", "04", "2020")).build());
        personalInfo.put("ssn", InputData.builder().value(List.of("123-45-6789")).build());
        personalInfo.put("sex", InputData.builder().value(List.of("FEMALE")).build());
        personalInfo.put("maritalStatus", InputData.builder().value(List.of("NEVER_MARRIED")).build());
        personalInfo.put("livedInMnWholeLife", InputData.builder().value(List.of("true")).build());
        applicationData.getPagesData().put("personalInfo", personalInfo);
        return this;
    }

    public TestApplicationDataBuilder withPageData(String pageName, String input, List<String> values) {
        PageData pageData = new PageData();
        pageData.put(input, InputData.builder().value(values).build());
        applicationData.getPagesData().put(pageName, pageData);
        return this;
    }
}
