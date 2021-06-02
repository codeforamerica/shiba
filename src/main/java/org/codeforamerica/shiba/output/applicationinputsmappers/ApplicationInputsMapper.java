package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.FormInputType;

import java.util.List;

public interface ApplicationInputsMapper {
    List<ApplicationInput> map(Application application, Document document, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker);

    static ApplicationInputType formInputTypeToApplicationInputType(FormInputType type) {
        return switch (type) {
            case CHECKBOX, PEOPLE_CHECKBOX -> ApplicationInputType.ENUMERATED_MULTI_VALUE;
            case RADIO, SELECT -> ApplicationInputType.ENUMERATED_SINGLE_VALUE;
            case DATE -> ApplicationInputType.DATE_VALUE;
            case TEXT, LONG_TEXT, HOURLY_WAGE, NUMBER, YES_NO, MONEY, TEXTAREA, PHONE, SSN, CUSTOM -> ApplicationInputType.SINGLE_VALUE;
            case HIDDEN -> ApplicationInputType.UNUSED;
        };
    }
}
