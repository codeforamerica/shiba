package org.codeforamerica.shiba.research;

import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.output.caf.TotalIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class ResearchDataParser {
    private final TotalIncomeCalculator totalIncomeCalculator;
    private final TotalIncomeParser totalIncomeParser;

    public ResearchDataParser(TotalIncomeCalculator totalIncomeCalculator, TotalIncomeParser totalIncomeParser) {
        this.totalIncomeCalculator = totalIncomeCalculator;
        this.totalIncomeParser = totalIncomeParser;
    }

    public ResearchData parse(ApplicationData applicationData) {
        PagesData pagesData = applicationData.getPagesData();
        Optional<PageData> contactInfoOptional = Optional.ofNullable(pagesData.getPage("contactInfo"));
        TotalIncome totalIncome = totalIncomeParser.parse(applicationData);
        Optional<PageData> programsOptional = Optional.ofNullable(pagesData.getPage("choosePrograms"));
        Optional<Subworkflow> jobsOptional = Optional.ofNullable(applicationData.getSubworkflows().get("jobs"));
        Optional<PageData> unearnedIncomeOptional = Optional.ofNullable(pagesData.getPage("unearnedIncome"));
        Optional<Subworkflow> householdSizeOptional = Optional.ofNullable(applicationData.getSubworkflows().get("household"));

        return ResearchData.builder()
                .spokenLanguage(pagesData.safeGetPageInputValue("languagePreferences", "spokenLanguage").stream().findFirst().orElse(null))
                .writtenLanguage(pagesData.safeGetPageInputValue("languagePreferences", "writtenLanguage").stream().findFirst().orElse(null))
                .sex(pagesData.safeGetPageInputValue("personalInfo", "sex").stream().findFirst().orElse(null))
                .snap(programsOptional.map(c -> c.get("programs").getValue().contains("SNAP")).orElse(null))
                .cash(programsOptional.map(c -> c.get("programs").getValue().contains("CASH")).orElse(null))
                .housing(programsOptional.map(c -> c.get("programs").getValue().contains("GRH")).orElse(null))
                .emergency(programsOptional.map(c -> c.get("programs").getValue().contains("EA")).orElse(null))
                .firstName(pagesData.safeGetPageInputValue("personalInfo", "firstName").stream().findFirst().orElse(null))
                .lastName(pagesData.safeGetPageInputValue("personalInfo", "lastName").stream().findFirst().orElse(null))
                .dateOfBirth(String.join("-", pagesData.safeGetPageInputValue("personalInfo", "dateOfBirth"))
                        .transform(dateString -> dateString.isBlank() ? null : LocalDate.parse(dateString, DateTimeFormatter.ofPattern("MM-dd-yyyy"))))
                .phoneNumber(pagesData.safeGetPageInputValue("contactInfo", "phoneNumber").stream().findFirst().orElse(null))
                .email(pagesData.safeGetPageInputValue("contactInfo", "email").stream().findFirst().orElse(null))
                .phoneOptIn(contactInfoOptional
                        .map(contactInformation -> Optional.ofNullable(contactInformation.get("phoneOrEmail"))
                                .map(inputData -> inputData.getValue().contains("TEXT"))
                                .orElse(false))
                        .orElse(null))
                .emailOptIn(contactInfoOptional
                        .map(contactInformation -> Optional.ofNullable(contactInformation.get("phoneOrEmail"))
                                .map(inputData -> inputData.getValue().contains("EMAIL"))
                                .orElse(false))
                        .orElse(null))
                .zipCode(pagesData.safeGetPageInputValue("homeAddress", "zipCode").stream().findFirst().orElse(null))
                .liveAlone(pagesData.safeGetPageInputValue("doYouLiveAlone", "liveAlone").stream().findFirst().map(Boolean::valueOf).orElse(null))
                .moneyMadeLast30Days(totalIncomeCalculator.calculate(totalIncome))
                .payRentOrMortgage(getPayRentOrMortgage(applicationData))
                .homeExpensesAmount(pagesData.safeGetPageInputValue("homeExpensesAmount", "homeExpensesAmount").stream().findFirst().map(Double::valueOf).orElse(null))
                .areYouWorking(pagesData.safeGetPageInputValue("employmentStatus", "areYouWorking").stream().findFirst().map(Boolean::valueOf).orElse(null))
                .selfEmployment(jobsOptional.map(this::getSelfEmployment).orElse(null))
                .socialSecurity(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("SOCIAL_SECURITY")).orElse(null))
                .SSI(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("SSI")).orElse(null))
                .veteransBenefits(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("VETERANS_BENEFITS")).orElse(null))
                .unemployment(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("UNEMPLOYMENT")).orElse(null))
                .workersCompensation(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("WORKERS_COMPENSATION")).orElse(null))
                .retirement(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("RETIREMENT")).orElse(null))
                .childOrSpousalSupport(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("CHILD_OR_SPOUSAL_SUPPORT")).orElse(null))
                .tribalPayments(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("TRIBAL_PAYMENTS")).orElse(null))
                .enteredSsn(Optional.ofNullable(pagesData.get("personalInfo"))
                        .map(personalInfo -> Optional.ofNullable(personalInfo.get("ssn"))
                                .map(inputData -> !String.join("", inputData.getValue()).isBlank())
                                .orElse(false))
                        .orElse(null))
                .flow(applicationData.getFlow())
                .applicationId(applicationData.getId())
                .county(pagesData.safeGetPageInputValue("homeAddress", "enrichedCounty").stream().findFirst().orElse(null))
                .householdSize(householdSizeOptional.map(householdIterations -> householdIterations.size() + 1).orElse(null))
                .build();
    }

    private Boolean getPayRentOrMortgage(ApplicationData applicationData) {
        FlowType flow = applicationData.getFlow();
        if (flow == null) {
            return null;
        }
        return switch (flow) {
            case EXPEDITED -> Optional.ofNullable(applicationData.getPagesData().getPage("expeditedExpenses"))
                    .map(expeditedExpenses -> expeditedExpenses.get("payRentOrMortgage").getValue().contains("true"))
                    .orElse(null);
            case FULL -> Optional.ofNullable(applicationData.getPagesData().getPage("homeExpenses"))
                    .map(homeExpenses -> {
                        List<String> housingExpenses = homeExpenses.get("homeExpenses").getValue();
                        return housingExpenses.contains("MORTGAGE") || housingExpenses.contains("RENT");
                    })
                    .orElse(null);
            case MINIMUM -> null;
        };
    }

    private Boolean getSelfEmployment(Subworkflow jobsSubworkflow) {
        return jobsSubworkflow.stream()
                .anyMatch(job -> job.getPage("selfEmployment").get("selfEmployment").getValue().contains("true"));
    }
}
