package org.codeforamerica.shiba.testutilities;


import org.codeforamerica.shiba.pages.data.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Helper class for building test application data
 */
public class TestApplicationDataBuilder {
    private final ApplicationData applicationData = new ApplicationData();
    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

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

    public TestApplicationDataBuilder withHomeAddress() {
        PageData pageData = new PageData();
        pageData.put("streetAddress", InputData.builder().value(List.of("street")).build());
        pageData.put("city", InputData.builder().value(List.of("city")).build());
        pageData.put("state", InputData.builder().value(List.of("CA")).build());
        pageData.put("zipCode", InputData.builder().value(List.of("02103")).build());
        pageData.put("apartmentNumber", InputData.builder().value(List.of("ste 123")).build());
        applicationData.getPagesData().put("homeAddress", pageData);
        return this;
    }

    public TestApplicationDataBuilder withPageData(String pageName, String input, List<String> values) {
        PagesData pagesData = applicationData.getPagesData();
        pagesData.putIfAbsent(pageName, new PageData());
        pagesData.get(pageName).put(input, InputData.builder().value(values).build());
        return this;
    }

    public TestApplicationDataBuilder withJobs() {
        applicationData.setSubworkflows(new Subworkflows(Map.of("jobs", new Subworkflow(List.of(pagesDataBuilder.build(List.of(
                new PageDataBuilder("payPeriod", Map.of("payPeriod", List.of("EVERY_WEEK"))),
                new PageDataBuilder("incomePerPayPeriod", Map.of("incomePerPayPeriod", List.of("1.1")))
        )))))));
        return this;
    }
    public TestApplicationDataBuilder withHouseholdMemberPrograms(List<String> programs) {
        applicationData.setSubworkflows(new Subworkflows(Map.of("household", new Subworkflow(List.of(pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of("programs", programs)))
        ))))));
        return this;
    }

    public TestApplicationDataBuilder withHouseholdMember() {
        applicationData.setSubworkflows(new Subworkflows(Map.of("household", new Subworkflow(List.of(pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo",
                        Map.of("firstName", List.of("Daria"),
                                "lastName", List.of("Agàta"),
                                "dateOfBirth", List.of("5", "6", "1978"),
                                "maritalStatus", List.of("Never married"),
                                "sex", List.of("Female"),
                                "livedInMnWholeLife", List.of("Yes"),
                                "relationship", List.of("housemate"),
                                "programs", List.of("SNAP"),
                                "ssn", List.of("123121234"))))
        ))))));
        return this;
    }
}
