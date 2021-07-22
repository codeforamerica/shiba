package org.codeforamerica.shiba.application.parsers;

import lombok.Getter;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDataParser {

    /**
     * Retrievable fields
     */
    public enum Field {
        PAID_BY_THE_HOUR,
        HOURLY_WAGE,
        HOURS_A_WEEK,
        PAY_PERIOD,
        INCOME_PER_PAY_PERIOD,
        LAST_THIRTY_DAYS_JOB_INCOME,

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

        IS_HOMELESS,
        IS_HOMELESS_2,
        SAME_MAILING_ADDRESS,
        SAME_MAILING_ADDRESS2,

        GENERAL_DELIVERY_CITY,
        GENERAL_DELIVERY_ZIPCODE,

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

        PERSONAL_INFO_DOB,
        HOUSEHOLD_INFO_DOB,
        MATCH_INFO_DOB;

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

    /**
     * Mapping configurations
     */
    private static final Map<Field, ParsingCoordinate> coordinatesMap = new HashMap<>();
    private static final Map<Group, String> groupCoordinatesMap = new HashMap<>();

    static {
        coordinatesMap.put(Field.PAID_BY_THE_HOUR, new ParsingCoordinate("paidByTheHour", "paidByTheHour"));
        coordinatesMap.put(Field.HOURLY_WAGE, new ParsingCoordinate("hourlyWage", "hourlyWage"));
        coordinatesMap.put(Field.HOURS_A_WEEK, new ParsingCoordinate("hoursAWeek", "hoursAWeek"));
        coordinatesMap.put(Field.PAY_PERIOD, new ParsingCoordinate("payPeriod", "payPeriod"));
        coordinatesMap.put(Field.INCOME_PER_PAY_PERIOD, new ParsingCoordinate("incomePerPayPeriod", "incomePerPayPeriod"));
        coordinatesMap.put(Field.LAST_THIRTY_DAYS_JOB_INCOME, new ParsingCoordinate("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome"));

        coordinatesMap.put(Field.MAILING_STREET, new ParsingCoordinate("mailingAddress", "streetAddress"));
        coordinatesMap.put(Field.MAILING_CITY, new ParsingCoordinate("mailingAddress", "city"));
        coordinatesMap.put(Field.MAILING_STATE, new ParsingCoordinate("mailingAddress", "state"));
        coordinatesMap.put(Field.MAILING_ZIPCODE, new ParsingCoordinate("mailingAddress", "zipCode"));
        coordinatesMap.put(Field.MAILING_APARTMENT_NUMBER, new ParsingCoordinate("mailingAddress", "apartmentNumber"));
        coordinatesMap.put(Field.MAILING_COUNTY, new ParsingCoordinate("mailingAddress", "enrichedCounty"));
        coordinatesMap.put(Field.ENRICHED_MAILING_STREET, new ParsingCoordinate("mailingAddress", "enrichedStreetAddress"));
        coordinatesMap.put(Field.ENRICHED_MAILING_CITY, new ParsingCoordinate("mailingAddress", "enrichedCity"));
        coordinatesMap.put(Field.ENRICHED_MAILING_STATE, new ParsingCoordinate("mailingAddress", "enrichedState"));
        coordinatesMap.put(Field.ENRICHED_MAILING_ZIPCODE, new ParsingCoordinate("mailingAddress", "enrichedZipCode"));
        coordinatesMap.put(Field.ENRICHED_MAILING_APARTMENT_NUMBER, new ParsingCoordinate("mailingAddress", "enrichedApartmentNumber"));

        coordinatesMap.put(Field.HOME_STREET, new ParsingCoordinate("homeAddress", "streetAddress"));
        coordinatesMap.put(Field.HOME_CITY, new ParsingCoordinate("homeAddress", "city"));
        coordinatesMap.put(Field.HOME_STATE, new ParsingCoordinate("homeAddress", "state"));
        coordinatesMap.put(Field.HOME_ZIPCODE, new ParsingCoordinate("homeAddress", "zipCode"));
        coordinatesMap.put(Field.HOME_APARTMENT_NUMBER, new ParsingCoordinate("homeAddress", "apartmentNumber"));
        coordinatesMap.put(Field.HOME_COUNTY, new ParsingCoordinate("homeAddress", "enrichedCounty"));
        coordinatesMap.put(Field.ENRICHED_HOME_STREET, new ParsingCoordinate("homeAddress", "enrichedStreetAddress"));
        coordinatesMap.put(Field.ENRICHED_HOME_CITY, new ParsingCoordinate("homeAddress", "enrichedCity"));
        coordinatesMap.put(Field.ENRICHED_HOME_STATE, new ParsingCoordinate("homeAddress", "enrichedState"));
        coordinatesMap.put(Field.ENRICHED_HOME_ZIPCODE, new ParsingCoordinate("homeAddress", "enrichedZipCode"));
        coordinatesMap.put(Field.ENRICHED_HOME_APARTMENT_NUMBER, new ParsingCoordinate("homeAddress", "enrichedApartmentNumber"));

        coordinatesMap.put(Field.GENERAL_DELIVERY_CITY, new ParsingCoordinate("cityForGeneralDelivery", "whatIsTheCity"));
        coordinatesMap.put(Field.GENERAL_DELIVERY_ZIPCODE, new ParsingCoordinate("cityForGeneralDelivery", "enrichedZipcode"));

        coordinatesMap.put(Field.IS_HOMELESS, new ParsingCoordinate("homeAddress", "isHomeless"));
        coordinatesMap.put(Field.IS_HOMELESS_2, new ParsingCoordinate("homeAddress2", "isHomeless"));
        coordinatesMap.put(Field.SAME_MAILING_ADDRESS, new ParsingCoordinate("homeAddress", "sameMailingAddress"));
        coordinatesMap.put(Field.SAME_MAILING_ADDRESS2, new ParsingCoordinate("mailingAddress", "sameMailingAddress"));

        coordinatesMap.put(Field.IDENTIFY_ZIPCODE, new ParsingCoordinate("identifyZipcode", "zipCode"));
        coordinatesMap.put(Field.IDENTIFY_COUNTY, new ParsingCoordinate("identifyCounty", "county"));

        coordinatesMap.put(Field.ASSETS, new ParsingCoordinate("liquidAssets", "liquidAssets"));
        coordinatesMap.put(Field.INCOME, new ParsingCoordinate("thirtyDayIncome", "moneyMadeLast30Days"));
        coordinatesMap.put(Field.MIGRANT_WORKER, new ParsingCoordinate("migrantFarmWorker", "migrantOrSeasonalFarmWorker"));
        coordinatesMap.put(Field.HOUSING_COSTS, new ParsingCoordinate("homeExpensesAmount", "homeExpensesAmount"));
        coordinatesMap.put(Field.UTILITY_EXPENSES_SELECTIONS, new ParsingCoordinate("utilityPayments", "payForUtilities"));
        coordinatesMap.put(Field.APPLICANT_PROGRAMS, new ParsingCoordinate("choosePrograms", "programs"));
        coordinatesMap.put(Field.HOUSEHOLD_PROGRAMS, new ParsingCoordinate("householdMemberInfo", "programs"));
        coordinatesMap.put(Field.PREPARING_MEALS_TOGETHER, new ParsingCoordinate("preparingMealsTogether", "isPreparingMealsTogether"));

        coordinatesMap.put(Field.PERSONAL_INFO_DOB, new ParsingCoordinate("personalInfo", "dateOfBirth"));
        coordinatesMap.put(Field.HOUSEHOLD_INFO_DOB, new ParsingCoordinate("householdMemberInfo", "dateOfBirth"));
        coordinatesMap.put(Field.MATCH_INFO_DOB, new ParsingCoordinate("matchInfo", "dateOfBirth"));

        groupCoordinatesMap.put(Group.JOBS, "jobs");
        groupCoordinatesMap.put(Group.HOUSEHOLD, "household");
    }

    @Getter
    private static class ParsingCoordinate {
        private final String pageName;
        private final String inputName;

        ParsingCoordinate(String pageName, String inputName) {
            this.pageName = pageName;
            this.inputName = inputName;
        }
    }

    public static List<String> getValues(PagesData pagesData, Field field) {
        ParsingCoordinate coordinate = coordinatesMap.get(field);
        return pagesData.safeGetPageInputValue(coordinate.getPageName(), coordinate.getInputName());
    }

    public static String getFirstValue(PagesData pagesData, Field field) {
        ParsingCoordinate coordinate = coordinatesMap.get(field);
        String pageInputValue = pagesData.getPageInputFirstValue(coordinate.getPageName(), coordinate.getInputName());
        return pageInputValue == null ? field.getDefaultValue() : pageInputValue;
    }

    public static List<String> getValues(Group group, Field field, ApplicationData applicationData) {
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
}
