package org.codeforamerica.shiba.application.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

public class ApplicationDataParser {

  /**
   * Mapping configurations
   */
  private static final Map<Field, ParsingCoordinate> coordinatesMap = new HashMap<>();
  private static final Map<Group, String> groupCoordinatesMap = new HashMap<>();

  static {
    coordinatesMap.put(Field.WRITTEN_LANGUAGE_PREFERENCES,
        new ParsingCoordinate("languagePreferences", "writtenLanguage"));
    coordinatesMap.put(Field.BASIC_CRITERIA_CERTAIN_POPS,
        new ParsingCoordinate("basicCriteria", "basicCriteria"));
    coordinatesMap.put(Field.MEDICAL_EXPENSES,
        new ParsingCoordinate("medicalExpenses", "medicalExpenses"));
    coordinatesMap
        .put(Field.PAID_BY_THE_HOUR, new ParsingCoordinate("paidByTheHour", "paidByTheHour"));
    coordinatesMap.put(Field.HOURLY_WAGE, new ParsingCoordinate("hourlyWage", "hourlyWage"));
    coordinatesMap.put(Field.HOURS_A_WEEK, new ParsingCoordinate("hoursAWeek", "hoursAWeek"));
    coordinatesMap.put(Field.PAY_PERIOD, new ParsingCoordinate("payPeriod", "payPeriod"));
    coordinatesMap.put(Field.INCOME_PER_PAY_PERIOD,
        new ParsingCoordinate("incomePerPayPeriod", "incomePerPayPeriod"));
    coordinatesMap.put(Field.LAST_THIRTY_DAYS_JOB_INCOME,
        new ParsingCoordinate("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome"));
    coordinatesMap.put(Field.IS_SELF_EMPLOYMENT,
        new ParsingCoordinate("selfEmployment", "selfEmployment"));
    coordinatesMap.put(Field.WHOSE_JOB_IS_IT,
        new ParsingCoordinate("householdSelectionForIncome", "whoseJobIsIt"));
    coordinatesMap.put(Field.EMPLOYERS_NAME,
        new ParsingCoordinate("employersName", "employersName"));
    coordinatesMap.put(Field.ARE_YOU_WORKING,
        new ParsingCoordinate("employmentStatus", "areYouWorking"));

    coordinatesMap.put(Field.LIVING_SITUATION,
        new ParsingCoordinate("livingSituation", "livingSituation"));

    coordinatesMap.put(Field.HAS_DISABILITY, new ParsingCoordinate("disability", "hasDisability"));

    coordinatesMap
        .put(Field.MAILING_STREET, new ParsingCoordinate("mailingAddress", "streetAddress"));
    coordinatesMap.put(Field.MAILING_CITY, new ParsingCoordinate("mailingAddress", "city"));
    coordinatesMap.put(Field.MAILING_STATE, new ParsingCoordinate("mailingAddress", "state"));
    coordinatesMap.put(Field.MAILING_ZIPCODE, new ParsingCoordinate("mailingAddress", "zipCode"));
    coordinatesMap.put(Field.MAILING_APARTMENT_NUMBER,
        new ParsingCoordinate("mailingAddress", "apartmentNumber"));
    coordinatesMap
        .put(Field.MAILING_COUNTY, new ParsingCoordinate("mailingAddress", "enrichedCounty"));
    coordinatesMap.put(Field.ENRICHED_MAILING_STREET,
        new ParsingCoordinate("mailingAddress", "enrichedStreetAddress"));
    coordinatesMap
        .put(Field.ENRICHED_MAILING_CITY, new ParsingCoordinate("mailingAddress", "enrichedCity"));
    coordinatesMap.put(Field.ENRICHED_MAILING_STATE,
        new ParsingCoordinate("mailingAddress", "enrichedState"));
    coordinatesMap.put(Field.ENRICHED_MAILING_ZIPCODE,
        new ParsingCoordinate("mailingAddress", "enrichedZipCode"));
    coordinatesMap.put(Field.ENRICHED_MAILING_APARTMENT_NUMBER,
        new ParsingCoordinate("mailingAddress", "enrichedApartmentNumber"));
    coordinatesMap.put(Field.ENRICHED_MAILING_COUNTY,
        new ParsingCoordinate("mailingAddress", "enrichedCounty"));
    coordinatesMap.put(Field.USE_ENRICHED_MAILING_ADDRESS,
        new ParsingCoordinate("mailingAddressValidation", "useEnrichedAddress"));

    coordinatesMap.put(Field.HOME_STREET, new ParsingCoordinate("homeAddress", "streetAddress"));
    coordinatesMap.put(Field.HOME_CITY, new ParsingCoordinate("homeAddress", "city"));
    coordinatesMap.put(Field.HOME_STATE, new ParsingCoordinate("homeAddress", "state"));
    coordinatesMap.put(Field.HOME_ZIPCODE, new ParsingCoordinate("homeAddress", "zipCode"));
    coordinatesMap
        .put(Field.HOME_APARTMENT_NUMBER, new ParsingCoordinate("homeAddress", "apartmentNumber"));
    coordinatesMap.put(Field.HOME_COUNTY, new ParsingCoordinate("homeAddress", "enrichedCounty"));
    coordinatesMap.put(Field.ENRICHED_HOME_STREET,
        new ParsingCoordinate("homeAddress", "enrichedStreetAddress"));
    coordinatesMap
        .put(Field.ENRICHED_HOME_CITY, new ParsingCoordinate("homeAddress", "enrichedCity"));
    coordinatesMap
        .put(Field.ENRICHED_HOME_STATE, new ParsingCoordinate("homeAddress", "enrichedState"));
    coordinatesMap
        .put(Field.ENRICHED_HOME_ZIPCODE, new ParsingCoordinate("homeAddress", "enrichedZipCode"));
    coordinatesMap.put(Field.ENRICHED_HOME_APARTMENT_NUMBER,
        new ParsingCoordinate("homeAddress", "enrichedApartmentNumber"));
    coordinatesMap.put(Field.ENRICHED_HOME_COUNTY,
        new ParsingCoordinate("homeAddress", "enrichedCounty"));
    coordinatesMap.put(Field.USE_ENRICHED_HOME_ADDRESS,
        new ParsingCoordinate("homeAddressValidation", "useEnrichedAddress"));

    coordinatesMap.put(Field.GENERAL_DELIVERY_CITY,
        new ParsingCoordinate("cityForGeneralDelivery", "whatIsTheCity"));
    coordinatesMap.put(Field.GENERAL_DELIVERY_ZIPCODE,
        new ParsingCoordinate("cityForGeneralDelivery", "enrichedZipcode"));

    coordinatesMap.put(Field.SELECTED_TRIBAL_NATION,
        new ParsingCoordinate("selectTheTribe", "selectedTribe"));

    coordinatesMap.put(Field.LIVING_IN_TRIBAL_NATION_BOUNDARY,
        new ParsingCoordinate("nationsBoundary", "livingInNationBoundary"));

    coordinatesMap.put(Field.APPLYING_FOR_TRIBAL_TANF,
        new ParsingCoordinate("applyForTribalTANF", "applyForTribalTANF"));

    coordinatesMap.put(Field.NO_PERMANENT_ADDRESS,
        new ParsingCoordinate("homeAddress", "isHomeless"));
    coordinatesMap.put(Field.SAME_MAILING_ADDRESS,
        new ParsingCoordinate("mailingAddress", "sameMailingAddress"));

    coordinatesMap.put(Field.IDENTIFY_ZIPCODE, new ParsingCoordinate("identifyZipcode", "zipCode"));
    coordinatesMap.put(Field.IDENTIFY_COUNTY, new ParsingCoordinate("identifyCounty", "county"));

    coordinatesMap.put(Field.ASSETS, new ParsingCoordinate("liquidAssets", "liquidAssets"));
    coordinatesMap
        .put(Field.INCOME, new ParsingCoordinate("thirtyDayIncome", "moneyMadeLast30Days"));
    coordinatesMap.put(Field.MIGRANT_WORKER,
        new ParsingCoordinate("migrantFarmWorker", "migrantOrSeasonalFarmWorker"));
    coordinatesMap.put(Field.HOUSING_COSTS,
        new ParsingCoordinate("homeExpensesAmount", "homeExpensesAmount"));
    coordinatesMap.put(Field.UTILITY_EXPENSES_SELECTIONS,
        new ParsingCoordinate("utilityPayments", "payForUtilities"));
    coordinatesMap
        .put(Field.APPLICANT_PROGRAMS, new ParsingCoordinate("choosePrograms", "programs"));
    coordinatesMap
        .put(Field.HOUSEHOLD_PROGRAMS, new ParsingCoordinate("householdMemberInfo", "programs"));
    coordinatesMap.put(Field.PREPARING_MEALS_TOGETHER,
        new ParsingCoordinate("preparingMealsTogether", "isPreparingMealsTogether"));
    coordinatesMap.put(Field.IS_GOING_TO_SCHOOL,
        new ParsingCoordinate("goingToSchool", "goingToSchool"));
    coordinatesMap.put(Field.WHO_IS_GOING_TO_SCHOOL,
        new ParsingCoordinate("whoIsGoingToSchool", "whoIsGoingToSchool"));
    coordinatesMap.put(Field.IS_LOOKING_FOR_JOB,
        new ParsingCoordinate("jobSearch", "currentlyLookingForJob"));
    coordinatesMap.put(Field.WHO_IS_LOOKING_FOR_A_JOB,
        new ParsingCoordinate("whoIsLookingForAJob", "whoIsLookingForAJob"));

    coordinatesMap.put(Field.UNEARNED_INCOME,
        new ParsingCoordinate("unearnedIncome", "unearnedIncome"));
    coordinatesMap.put(Field.SOCIAL_SECURITY_AMOUNT,
        new ParsingCoordinate("unearnedIncomeSources", "socialSecurityAmount"));
    coordinatesMap.put(Field.SSI_AMOUNT, new ParsingCoordinate("unearnedIncomeSources",
        "supplementalSecurityIncomeAmount"));
    coordinatesMap.put(Field.VETERANS_BENEFITS_AMOUNT,
        new ParsingCoordinate("unearnedIncomeSources", "veteransBenefitsAmount"));
    coordinatesMap.put(Field.UNEMPLOYMENT_AMOUNT,
        new ParsingCoordinate("unearnedIncomeSources", "unemploymentAmount"));
    coordinatesMap.put(Field.
            WORKERS_COMPENSATION_AMOUNT,
        new ParsingCoordinate("unearnedIncomeSources",
            "workersCompensationAmount"));
    coordinatesMap.put(Field.RETIREMENT_AMOUNT,
        new ParsingCoordinate("unearnedIncomeSources", "retirementAmount"));
    coordinatesMap.put(Field.CHILD_OR_SPOUSAL_SUPPORT_AMOUNT,
        new ParsingCoordinate("unearnedIncomeSources", "childOrSpousalSupportAmount"));
    coordinatesMap.put(Field.TRIBAL_PAYMENTS_AMOUNT,
        new ParsingCoordinate("unearnedIncomeSources", "tribalPaymentsAmount"));
    
    coordinatesMap.put(Field.UNEARNED_SOCIAL_SECURITY_AMOUNT,
        new ParsingCoordinate("socialSecurityIncomeSource", "socialSecurityAmount"));
    coordinatesMap.put(Field.UNEARNED_SSI_AMOUNT,
        new ParsingCoordinate("supplementalSecurityIncomeSource", "supplementalSecurityIncomeAmount"));
    coordinatesMap.put(Field.UNEARNED_VETERANS_BENEFITS_AMOUNT,
        new ParsingCoordinate("veteransBenefitsIncomeSource", "veteransBenefitsAmount"));
    coordinatesMap.put(Field.UNEARNED_UNEMPLOYMENT_AMOUNT,
        new ParsingCoordinate("unemploymentIncomeSource", "unemploymentAmount"));
    coordinatesMap.put(Field.UNEARNED_WORKERS_COMPENSATION_AMOUNT,
        new ParsingCoordinate("workersCompIncomeSource", "workersCompensationAmount"));
    coordinatesMap.put(Field.UNEARNED_RETIREMENT_AMOUNT,
        new ParsingCoordinate("retirementIncomeSource", "retirementAmount"));
    coordinatesMap.put(Field.UNEARNED_CHILD_OR_SPOUSAL_SUPPORT_AMOUNT,
        new ParsingCoordinate("childOrSpousalSupportIncomeSource", "childOrSpousalSupportAmount"));
    coordinatesMap.put(Field.UNEARNED_TRIBAL_PAYMENTS_AMOUNT,
        new ParsingCoordinate("tribalPaymentIncomeSource", "tribalPaymentsAmount"));
    
    coordinatesMap.put(Field.UNEARNED_INCOME_OTHER,
        new ParsingCoordinate("otherUnearnedIncome", "otherUnearnedIncome"));
    coordinatesMap.put(Field.UNEARNED_BENEFITS_PROGRAMS_AMOUNT,
        new ParsingCoordinate("benefitsProgramsIncomeSource", "benefitsAmount"));
    coordinatesMap.put(Field.UNEARNED_INSURANCE_PAYMENTS_AMOUNT,
        new ParsingCoordinate("insurancePaymentsIncomeSource", "insurancePaymentsAmount"));
    coordinatesMap.put(Field.UNEARNED_CONTRACT_FOR_DEED_AMOUNT,
        new ParsingCoordinate("contractForDeedIncomeSource", "contractForDeedAmount"));
    coordinatesMap.put(Field.UNEARNED_TRUST_MONEY_AMOUNT,
        new ParsingCoordinate("trustMoneyIncomeSource", "trustMoneyAmount"));
    coordinatesMap.put(Field.UNEARNED_HEALTHCARE_REIMBURSEMENT_AMOUNT,
        new ParsingCoordinate("healthcareReimbursementIncomeSource",
            "healthCareReimbursementAmount"));
    coordinatesMap.put(Field.UNEARNED_INTEREST_DIVIDENDS_AMOUNT,
        new ParsingCoordinate("interestDividendsIncomeSource", "interestDividendsAmount"));
    coordinatesMap.put(Field.UNEARNED_RENTAL_AMOUNT,
        new ParsingCoordinate("rentalIncomeSource", "rentalIncomeAmount"));
    coordinatesMap.put(Field.UNEARNED_OTHER_PAYMENTS_AMOUNT,
        new ParsingCoordinate("otherPaymentsIncomeSource", "otherPaymentsAmount"));
    coordinatesMap.put(Field.BENEFITS_PROGRAMS_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "benefitsAmount"));
    coordinatesMap.put(Field.INSURANCE_PAYMENTS_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "insurancePaymentsAmount"));
    coordinatesMap.put(Field.CONTRACT_FOR_DEED_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "contractForDeedAmount"));
    coordinatesMap.put(Field.TRUST_MONEY_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "trustMoneyAmount"));
    coordinatesMap.put(Field.HEALTHCARE_REIMBURSEMENT_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "healthCareReimbursementAmount"));
    coordinatesMap.put(Field.INTEREST_DIVIDENDS_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "interestDividendsAmount"));
    coordinatesMap.put(Field.RENTAL_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "rentalIncomeAmount"));
    coordinatesMap.put(Field.OTHER_PAYMENTS_AMOUNT,
        new ParsingCoordinate("otherUnearnedIncomeSources", "otherPaymentsAmount"));
    coordinatesMap.put(Field.HOME_EXPENSES,
        new ParsingCoordinate("homeExpenses", "homeExpenses"));
    coordinatesMap.put(Field.UTILITY_PAYMENTS,
        new ParsingCoordinate("utilityPayments", "payForUtilities"));
    coordinatesMap.put(Field.ASSETS_TYPE,
        new ParsingCoordinate("assets", "assets"));
    coordinatesMap.put(Field.SAVINGS,
        new ParsingCoordinate("savings", "haveSavings"));
    coordinatesMap.put(Field.RECEIVES_ENERGY_ASSISTANCE,
        new ParsingCoordinate("energyAssistance", "energyAssistance"));
    coordinatesMap.put(Field.ENERGY_ASSISTANCE_OVER_20,
        new ParsingCoordinate("energyAssistanceMoreThan20", "energyAssistanceMoreThan20"));
    coordinatesMap.put(Field.REGISTER_TO_VOTE,
        new ParsingCoordinate("registerToVote", "registerToVote"));
    coordinatesMap.put(Field.HAVE_HEALTHCARE_COVERAGE,
        new ParsingCoordinate("healthcareCoverage", "healthcareCoverage"));
    coordinatesMap
        .put(Field.PERSONAL_INFO_DOB, new ParsingCoordinate("personalInfo", "dateOfBirth"));
    coordinatesMap
        .put(Field.PERSONAL_INFO_SSN, new ParsingCoordinate("personalInfo", "ssn"));
    coordinatesMap
        .put(Field.PERSONAL_INFO_NO_SSN, new ParsingCoordinate("personalInfo", "noSSNCheck"));
    coordinatesMap
        .put(Field.PERSONAL_INFO_FIRST_NAME, new ParsingCoordinate("personalInfo", "firstName"));
    coordinatesMap
        .put(Field.PERSONAL_INFO_LAST_NAME, new ParsingCoordinate("personalInfo", "lastName"));

    coordinatesMap
        .put(Field.HOUSEHOLD_INFO_DOB, new ParsingCoordinate("householdMemberInfo", "dateOfBirth"));
    coordinatesMap.put(Field.HOUSEHOLD_INFO_FIRST_NAME,
        new ParsingCoordinate("householdMemberInfo", "firstName"));
    coordinatesMap.put(Field.HOUSEHOLD_INFO_LAST_NAME,
        new ParsingCoordinate("householdMemberInfo", "lastName"));
    coordinatesMap.put(Field.HOUSEHOLD_INFO_RELATIONSHIP,
        new ParsingCoordinate("householdMemberInfo", "relationship"));
    coordinatesMap.put(Field.HOUSEHOLD_INFO_SEX,
        new ParsingCoordinate("householdMemberInfo", "sex"));
    coordinatesMap.put(Field.HOUSEHOLD_INFO_SSN,
        new ParsingCoordinate("householdMemberInfo", "ssn"));
    coordinatesMap.put(Field.HOUSEHOLD_INFO_MARITAL_STATUS,
        new ParsingCoordinate("householdMemberInfo", "maritalStatus"));

    coordinatesMap.put(Field.MATCH_INFO_DOB, new ParsingCoordinate("matchInfo", "dateOfBirth"));
    coordinatesMap
        .put(Field.MATCH_INFO_FIRST_NAME, new ParsingCoordinate("matchInfo", "firstName"));
    coordinatesMap.put(Field.MATCH_INFO_LAST_NAME, new ParsingCoordinate("matchInfo", "lastName"));

    coordinatesMap.put(Field.EVERYONE_US_CITIZENS,
        new ParsingCoordinate("usCitizen", "isUsCitizen"));
    coordinatesMap.put(Field.WHO_ARE_NON_US_CITIZENS,
        new ParsingCoordinate("whoIsNonCitizen", "whoIsNonCitizen"));

    coordinatesMap.put(Field.RACE_AND_ETHNICITY,
        new ParsingCoordinate("raceAndEthnicity", "raceAndEthnicity"));

    coordinatesMap.put(Field.HAS_HOUSE_HOLD,
        new ParsingCoordinate("addHouseholdMembers", "addHouseholdMembers"));
    groupCoordinatesMap.put(Group.JOBS, "jobs");
    groupCoordinatesMap.put(Group.HOUSEHOLD, "household");
  }

  public static List<String> getValues(PagesData pagesData, Field field) {
    ParsingCoordinate coordinate = coordinatesMap.get(field);
    return pagesData.safeGetPageInputValue(coordinate.pageName(), coordinate.inputName());
  }

  public static String getFirstValue(PagesData pagesData, Field field) {
    ParsingCoordinate coordinate = coordinatesMap.get(field);
    String pageInputValue = pagesData
        .getPageInputFirstValue(coordinate.pageName(), coordinate.inputName());
    return pageInputValue == null ? field.getDefaultValue() : pageInputValue;
  }

  public static boolean getBooleanValue(PagesData pagesData, Field field) {
    return Boolean.parseBoolean(getFirstValue(pagesData, field));
  }

  public static List<String> getValues(ApplicationData applicationData, Group group, Field field) {
    Subworkflow iterations = getGroup(applicationData, group);
    if (iterations == null) {
      return null;
    }

    List<String> result = new ArrayList<>();
    for (Iteration iteration : iterations) {
      result.addAll(getValues(iteration.getPagesData(), field));
    }
    return result;
  }

  public static Subworkflow getGroup(ApplicationData applicationData, Group group) {
    return applicationData.getSubworkflows().get(groupCoordinatesMap.get(group));
  }

  /**
   * Retrievable fields
   */
  public enum Field {
    WRITTEN_LANGUAGE_PREFERENCES,

    BASIC_CRITERIA_CERTAIN_POPS,

    HAS_DISABILITY,

    MEDICAL_EXPENSES,

    PAID_BY_THE_HOUR,
    HOURLY_WAGE,
    HOURS_A_WEEK,
    PAY_PERIOD,
    INCOME_PER_PAY_PERIOD,
    LAST_THIRTY_DAYS_JOB_INCOME,
    IS_SELF_EMPLOYMENT,
    WHOSE_JOB_IS_IT,
    EMPLOYERS_NAME,
    ARE_YOU_WORKING,

    LIVING_SITUATION,

    MAILING_STREET,
    MAILING_CITY,
    MAILING_STATE,
    MAILING_ZIPCODE,
    MAILING_APARTMENT_NUMBER,
    MAILING_COUNTY("Other"),
    ENRICHED_MAILING_STREET,
    ENRICHED_MAILING_CITY,
    ENRICHED_MAILING_STATE,
    ENRICHED_MAILING_ZIPCODE,
    ENRICHED_MAILING_APARTMENT_NUMBER,
    ENRICHED_MAILING_COUNTY,
    USE_ENRICHED_MAILING_ADDRESS,

    HOME_STREET,
    HOME_CITY,
    HOME_STATE,
    HOME_ZIPCODE,
    HOME_APARTMENT_NUMBER,
    HOME_COUNTY("Other"),
    ENRICHED_HOME_STREET,
    ENRICHED_HOME_CITY,
    ENRICHED_HOME_STATE,
    ENRICHED_HOME_ZIPCODE,
    ENRICHED_HOME_APARTMENT_NUMBER,
    ENRICHED_HOME_COUNTY,
    USE_ENRICHED_HOME_ADDRESS,

    NO_PERMANENT_ADDRESS,
    SAME_MAILING_ADDRESS,

    GENERAL_DELIVERY_CITY,
    GENERAL_DELIVERY_ZIPCODE,

    SELECTED_TRIBAL_NATION,
    LIVING_IN_TRIBAL_NATION_BOUNDARY,
    APPLYING_FOR_TRIBAL_TANF,

    IDENTIFY_ZIPCODE,
    IDENTIFY_COUNTY("Other"),

    ASSETS("0"),
    INCOME("0"),
    MIGRANT_WORKER,
    HOUSING_COSTS("0"),
    UTILITY_EXPENSES_SELECTIONS,
    APPLICANT_PROGRAMS,
    HOUSEHOLD_PROGRAMS,
    PREPARING_MEALS_TOGETHER,
    IS_GOING_TO_SCHOOL,
    WHO_IS_GOING_TO_SCHOOL,
    IS_LOOKING_FOR_JOB,
    WHO_IS_LOOKING_FOR_A_JOB,

    UNEARNED_INCOME,
    SOCIAL_SECURITY_AMOUNT,
    SSI_AMOUNT,
    VETERANS_BENEFITS_AMOUNT,
    UNEMPLOYMENT_AMOUNT,
    WORKERS_COMPENSATION_AMOUNT,
    RETIREMENT_AMOUNT,
    CHILD_OR_SPOUSAL_SUPPORT_AMOUNT,
    TRIBAL_PAYMENTS_AMOUNT,

    UNEARNED_INCOME_OTHER,
    BENEFITS_PROGRAMS_AMOUNT,
    INSURANCE_PAYMENTS_AMOUNT,
    CONTRACT_FOR_DEED_AMOUNT,
    TRUST_MONEY_AMOUNT,
    HEALTHCARE_REIMBURSEMENT_AMOUNT,
    INTEREST_DIVIDENDS_AMOUNT,
    RENTAL_AMOUNT,
    OTHER_PAYMENTS_AMOUNT,
    
    UNEARNED_BENEFITS_PROGRAMS_AMOUNT,
    UNEARNED_INSURANCE_PAYMENTS_AMOUNT,
    UNEARNED_CONTRACT_FOR_DEED_AMOUNT,
    UNEARNED_TRUST_MONEY_AMOUNT,
    UNEARNED_HEALTHCARE_REIMBURSEMENT_AMOUNT,
    UNEARNED_INTEREST_DIVIDENDS_AMOUNT,
    UNEARNED_RENTAL_AMOUNT,
    UNEARNED_OTHER_PAYMENTS_AMOUNT,

    HOME_EXPENSES,
    UTILITY_PAYMENTS,
    ASSETS_TYPE,
    SAVINGS,

    RECEIVES_ENERGY_ASSISTANCE,
    ENERGY_ASSISTANCE_OVER_20,

    REGISTER_TO_VOTE,

    HAVE_HEALTHCARE_COVERAGE,

    PERSONAL_INFO_DOB,
    PERSONAL_INFO_SSN,
    PERSONAL_INFO_NO_SSN,
    PERSONAL_INFO_FIRST_NAME(""),
    PERSONAL_INFO_LAST_NAME(""),

    HOUSEHOLD_INFO_DOB,
    HOUSEHOLD_INFO_FIRST_NAME(""),
    HOUSEHOLD_INFO_LAST_NAME(""),
    HOUSEHOLD_INFO_RELATIONSHIP(""),
    HOUSEHOLD_INFO_SEX(""),
    HOUSEHOLD_INFO_MARITAL_STATUS(""),
    HOUSEHOLD_INFO_SSN(""),

    MATCH_INFO_DOB,
    MATCH_INFO_FIRST_NAME(""),
    MATCH_INFO_LAST_NAME(""),

    EVERYONE_US_CITIZENS,
    WHO_ARE_NON_US_CITIZENS,
    RACE_AND_ETHNICITY,
    HAS_HOUSE_HOLD,
    UNEARNED_SOCIAL_SECURITY_AMOUNT,
    UNEARNED_SSI_AMOUNT,
    UNEARNED_VETERANS_BENEFITS_AMOUNT,
    UNEARNED_UNEMPLOYMENT_AMOUNT,
    UNEARNED_WORKERS_COMPENSATION_AMOUNT,
    UNEARNED_RETIREMENT_AMOUNT,
    UNEARNED_CHILD_OR_SPOUSAL_SUPPORT_AMOUNT,
    UNEARNED_TRIBAL_PAYMENTS_AMOUNT;
    @Getter
    private final String defaultValue;

    Field(String defaultValue) {
      this.defaultValue = defaultValue;
    }

    Field() {
      defaultValue = null;
    }
  }

  public enum Group {
    JOBS,
    HOUSEHOLD
  }

  private record ParsingCoordinate(String pageName, String inputName) {

  }
}
