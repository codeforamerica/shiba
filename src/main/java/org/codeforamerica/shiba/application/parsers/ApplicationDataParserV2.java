package org.codeforamerica.shiba.application.parsers;

import lombok.Getter;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDataParserV2 {

    /**
     * Retrievable fields
     */
    public enum Field {
        PAID_BY_THE_HOUR,
        HOURLY_WAGE,
        HOURS_A_WEEK,
        PAY_PERIOD,
        INCOME_PER_PAY_PERIOD,
        LAST_THIRTY_DAYS_JOB_INCOME
    }

    public enum Group {
        JOBS,
        HOUSEHOLD
    }

    /**
     * Mapping configuration
     */
    private static final Map<Field, ParsingCoordinate> coordinatesMap = new HashMap<>();
    static {
        coordinatesMap.put(Field.PAID_BY_THE_HOUR, new ParsingCoordinate("paidByTheHour", "paidByTheHour"));
        coordinatesMap.put(Field.HOURLY_WAGE, new ParsingCoordinate("hourlyWage", "hourlyWage"));
        coordinatesMap.put(Field.HOURS_A_WEEK, new ParsingCoordinate("hoursAWeek", "hoursAWeek"));
        coordinatesMap.put(Field.PAY_PERIOD, new ParsingCoordinate("payPeriod", "payPeriod"));
        coordinatesMap.put(Field.INCOME_PER_PAY_PERIOD, new ParsingCoordinate("incomePerPayPeriod", "incomePerPayPeriod"));
        coordinatesMap.put(Field.LAST_THIRTY_DAYS_JOB_INCOME, new ParsingCoordinate("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome"));
    }

    private static final Map<Group, String> groupCoordinatesMap = new HashMap<>();
    static {
        groupCoordinatesMap.put(Group.JOBS, "jobs");
        groupCoordinatesMap.put(Group.HOUSEHOLD, "household");
    }

    @Getter
    private static class ParsingCoordinate {
        private final String pageName;
        private final String inputName;
        private String defaultValue = null;

        ParsingCoordinate(String pageName, String inputName) {
            this.pageName = pageName;
            this.inputName = inputName;
        }

        ParsingCoordinate(String pageName, String inputName, String defaultValue) {
            this.pageName = pageName;
            this.inputName = inputName;
            this.defaultValue = defaultValue;
        }
    }

    public static List<String> getValues(PagesData pagesData, Field field) {
        ParsingCoordinate coordinate = coordinatesMap.get(field);
        return pagesData.safeGetPageInputValue(coordinate.getPageName(), coordinate.getInputName());
    }

    public static String getFirstValue(PagesData pagesData, Field field) {
        ParsingCoordinate coordinate = coordinatesMap.get(field);
        String pageInputValue = pagesData.getPageInputFirstValue(coordinate.getPageName(), coordinate.getInputName());
        return pageInputValue == null ? coordinate.getDefaultValue() : pageInputValue;
    }

    public static Subworkflow getGroup(ApplicationData applicationData, Group group) {
        return applicationData.getSubworkflows().get(groupCoordinatesMap.get(group));
    }
}
