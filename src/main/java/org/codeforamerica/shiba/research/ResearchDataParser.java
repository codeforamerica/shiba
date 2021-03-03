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
import java.util.*;
import java.util.stream.Collectors;

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
        List<String> applicationPrograms = getApplicationPrograms(applicationData);
        Optional<PageData> unearnedIncomeOptional = Optional.ofNullable(pagesData.getPage("unearnedIncome"));
        Optional<Subworkflow> householdSizeOptional = Optional.ofNullable(applicationData.getSubworkflows().get("household"));

        return ResearchData.builder()
                .spokenLanguage(pagesData.safeGetPageInputValue("languagePreferences", "spokenLanguage").stream().findFirst().orElse(null))
                .writtenLanguage(pagesData.safeGetPageInputValue("languagePreferences", "writtenLanguage").stream().findFirst().orElse(null))
                .sex(pagesData.safeGetPageInputValue("personalInfo", "sex").stream().findFirst().orElse(null))
                .snap(applicationPrograms.contains("SNAP"))
                .cash(applicationPrograms.contains("CASH"))
                .housing(applicationPrograms.contains("GRH"))
                .emergency(applicationPrograms.contains("EA"))
                .childcare(applicationPrograms.contains("CCAP"))
                .firstName(pagesData.safeGetPageInputValue("personalInfo", "firstName").stream().findFirst().orElse(null))
                .lastName(pagesData.safeGetPageInputValue("personalInfo", "lastName").stream().findFirst().orElse(null))
                .dateOfBirth(String.join("-", pagesData.safeGetPageInputValue("personalInfo", "dateOfBirth"))
                        .transform(dateString -> dateString.isBlank() || dateString.equals("--") ? null : LocalDate.parse(dateString, DateTimeFormatter.ofPattern("M-d-yyyy"))))
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
                .hasHousehold(pagesData.safeGetPageInputValue("addHouseholdMembers", "addHouseholdMembers").stream().findFirst().map(Boolean::parseBoolean).orElse(null))
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
            case MINIMUM, UNDETERMINED -> null;
        };
    }

    private Boolean getSelfEmployment(Subworkflow jobsSubworkflow) {
        return jobsSubworkflow.stream()
                .anyMatch(job -> job.getPagesData().getPage("selfEmployment").get("selfEmployment").getValue().contains("true"));
    }

    private List<String> getApplicationPrograms (ApplicationData applicationData) {

        List<String> applicantPrograms = applicationData.getPagesData().safeGetPageInputValue("choosePrograms", "programs");
        List<String> applicationPrograms = new ArrayList<>(applicantPrograms);
        boolean hasHousehold = applicationData.getSubworkflows().containsKey("household");
        if (hasHousehold) {
            List<List<String>> householdPrograms = applicationData.getSubworkflows().get("household").stream().map(iteration ->
                    iteration.getPagesData().safeGetPageInputValue("householdMemberInfo", "programs")).collect(Collectors.toList());
            householdPrograms.forEach(programSelections -> programSelections.forEach(program -> {
                if (!applicationPrograms.contains(program)) {
                    applicationPrograms.add(program);
                }
            }));
        }

        return applicationPrograms;
    }
}
