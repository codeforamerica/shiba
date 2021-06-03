package org.codeforamerica.shiba.application.parsers;

import lombok.Getter;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.List;

public class ApplicationDataParserV2 {
    private final ApplicationData applicationData;

    enum Field {
        PAID_BY_THE_HOUR("paidByTheHour", "paidByTheHour"),
        HOURLY_WAGE("hourlyWage", "hourlyWage"),
        HOURS_A_WEEK("hoursAWeek", "hoursAWeek"),
        PAY_PERIOD("payPeriod", "payPeriod"),
        INCOME_PER_PAY_PERIOD("incomePerPayPeriod", "incomePerPayPeriod"),
        LAST_THIRTY_DAYS_JOB_INCOME("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome");

        @Getter
        private final String pageName;
        @Getter
        private final String inputName;
        private String defaultValue = null;

        Field(String pageName, String inputName) {
            this.pageName = pageName;
            this.inputName = inputName;
        }

        Field(String pageName, String inputName, String defaultValue) {
            this.pageName = pageName;
            this.inputName = inputName;
            this.defaultValue = defaultValue;
        }
    }

    public ApplicationDataParserV2(ApplicationData applicationData) {
        this.applicationData = applicationData;
    }

    public List<String> getValues(Field field) {
        return applicationData.getPagesData().safeGetPageInputValue(field.getPageName(), field.getInputName());
    }

    public String getFirstValue(Field field) {
        String pageInputValue = applicationData.getPagesData().getPageInputFirstValue(field.getPageName(), field.getInputName());
        return pageInputValue == null ? field.defaultValue : pageInputValue;
    }

    public static List<String> getValues(PagesData pagesData, Field field) {
        return pagesData.safeGetPageInputValue(field.getPageName(), field.getInputName());
    }

    public static String getFirstValue(PagesData pagesData, Field field) {
        String pageInputValue = pagesData.getPageInputFirstValue(field.getPageName(), field.getInputName());
        return pageInputValue == null ? field.defaultValue : pageInputValue;
    }
}
