package org.codeforamerica.shiba.research;

import lombok.Builder;
import lombok.Value;
import org.codeforamerica.shiba.application.FlowType;

import java.time.LocalDate;

@Value
@Builder
public class ResearchData {
    String spokenLanguage;
    String writtenLanguage;
    String sex;
    @Builder.Default
    Boolean snap = false;
    @Builder.Default
    Boolean cash = false;
    @Builder.Default
    Boolean housing = false;
    @Builder.Default
    Boolean emergency = false;
    @Builder.Default
    Boolean childcare = false;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    String phoneNumber;
    String email;
    Boolean phoneOptIn;
    Boolean emailOptIn;
    String zipCode;
    Boolean liveAlone;
    @Builder.Default
    Double moneyMadeLast30Days = 0.0;
    Boolean payRentOrMortgage;
    Double homeExpensesAmount;
    Boolean areYouWorking;
    Boolean selfEmployment;
    Boolean socialSecurity;
    Boolean SSI;
    Boolean veteransBenefits;
    Boolean unemployment;
    Boolean workersCompensation;
    Boolean retirement;
    Boolean childOrSpousalSupport;
    Boolean tribalPayments;
    Integer householdSize;
    Boolean enteredSsn;
    FlowType flow;
    String applicationId;
    String county;
}
