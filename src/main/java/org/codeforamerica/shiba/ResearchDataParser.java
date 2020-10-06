package org.codeforamerica.shiba;

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
import java.util.ArrayList;
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
        Optional<PageData> languagePreferencesOptional = Optional.ofNullable(pagesData.getPage("languagePreferences"));
        Optional<PageData> personalInfoOptional = Optional.ofNullable(pagesData.getPage("personalInfo"));
        Optional<PageData> contactInfoOptional = Optional.ofNullable(pagesData.getPage("contactInfo"));
        Optional<PageData> homeAddressOptional = Optional.ofNullable(pagesData.getPage("homeAddress"));
        Optional<PageData> homeExpensesAmountOptional = Optional.ofNullable(pagesData.getPage("homeExpensesAmount"));
        Optional<PageData> currentlyWorkingOptional = Optional.ofNullable(pagesData.getPage("employmentStatus"));
        Optional<PageData> liveAloneOptional = Optional.ofNullable(pagesData.getPage("doYouLiveAlone"));
        TotalIncome totalIncome = totalIncomeParser.parse(applicationData);
        Optional<PageData> programsOptional = Optional.ofNullable(pagesData.getPage("choosePrograms"));
        Optional<Subworkflow> jobsOptional = Optional.ofNullable(applicationData.getSubworkflows().get("jobs"));
        Optional<PageData> unearnedIncomeOptional = Optional.ofNullable(pagesData.getPage("unearnedIncome"));
        Optional<Subworkflow> householdSizeOptional = Optional.ofNullable(applicationData.getSubworkflows().get("household"));


        return ResearchData.builder()
                .spokenLanguage(languagePreferencesOptional.map(languagePrefs -> languagePrefs.get("spokenLanguage").getValue(0)).orElse(null))
                .writtenLanguage(languagePreferencesOptional.map(languagePreferences -> languagePreferences.get("writtenLanguage").getValue(0)).orElse(null))
                .sex(personalInfoOptional.map(pInfo -> pInfo.get("sex").getValue(0)).orElse(null))
                .snap(programsOptional.map(c -> c.get("programs").getValue().contains("SNAP")).orElse(null))
                .cash(programsOptional.map(c -> c.get("programs").getValue().contains("CASH")).orElse(null))
                .housing(programsOptional.map(c -> c.get("programs").getValue().contains("GRH")).orElse(null))
                .emergency(programsOptional.map(c -> c.get("programs").getValue().contains("EA")).orElse(null))
                .firstName(personalInfoOptional.map(pInfo -> pInfo.get("firstName").getValue(0)).orElse(null))
                .lastName(personalInfoOptional.map(pInfo -> pInfo.get("lastName").getValue(0)).orElse(null))
                .dateOfBirth(personalInfoOptional.map(pInfo -> LocalDate.parse(String.join("-", pInfo.get("dateOfBirth").getValue()), DateTimeFormatter.ofPattern("MM-dd-yyyy"))).orElse(null))
                .phoneNumber(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneNumber").getValue(0)).orElse(null))
                .email(contactInfoOptional.map(contactInformation -> contactInformation.get("email").getValue(0)).orElse(null))
                .phoneOptIn(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneOrEmail").getValue().contains("TEXT")).orElse(null))
                .emailOptIn(contactInfoOptional.map(contactInformation -> contactInformation.get("phoneOrEmail").getValue().contains("EMAIL")).orElse(null))
                .zipCode(homeAddressOptional.map(homeAddr -> homeAddr.get("zipCode").getValue(0)).orElse(null))
                .liveAlone(liveAloneOptional.map(liveAlone -> liveAlone.get("liveAlone").getValue(0).contains("true")).orElse(null))
                .moneyMadeLast30Days(totalIncomeCalculator.calculate(totalIncome))
                .payRentOrMortgage(getPayRentOrMortgage(applicationData))
                .homeExpensesAmount(homeExpensesAmountOptional.map(homeExpsAmount -> Double.valueOf(homeExpsAmount.get("homeExpensesAmount").getValue(0))).orElse(null))
                .areYouWorking(currentlyWorkingOptional.map(currentlyWorking -> Boolean.valueOf(currentlyWorking.get("areYouWorking").getValue(0))).orElse(null))
                .selfEmployment(jobsOptional.map(this::getSelfEmployment).orElse(null))
                .socialSecurity(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("SOCIAL_SECURITY")).orElse(null))
                .SSI(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("SSI")).orElse(null))
                .veteransBenefits(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("VETERANS_BENEFITS")).orElse(null))
                .unemployment(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("UNEMPLOYMENT")).orElse(null))
                .workersCompensation(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("WORKERS_COMPENSATION")).orElse(null))
                .retirement(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("RETIREMENT")).orElse(null))
                .childOrSpousalSupport(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("CHILD_OR_SPOUSAL_SUPPORT")).orElse(null))
                .tribalPayments(unearnedIncomeOptional.map(i -> i.get("unearnedIncome").getValue().contains("TRIBAL_PAYMENTS")).orElse(null))
                .enteredSsn(personalInfoOptional.map(pInfo -> !(String.join("", pInfo.get("ssn").getValue())).isBlank()).orElse(null))
                .flow(applicationData.getFlow())
                .applicationId(applicationData.getId())
                .county(homeAddressOptional
                        .flatMap(homeAddress -> Optional.ofNullable(homeAddress.get("enrichedCounty")))
                        .map(inputData -> inputData.getValue().get(0))
                        .orElse(null))
                .householdSize(householdSizeOptional.map(ArrayList::size).orElse(null))
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
