package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.ApplicationData;
import org.codeforamerica.shiba.pages.FormInputType;

import java.util.List;

public interface ApplicationInputsMapper {
    List<ApplicationInput> map(ApplicationData data);

    static ApplicationInputType formInputTypeToApplicationInputType(FormInputType type) {
        return switch (type) {
            case CHECKBOX -> ApplicationInputType.ENUMERATED_MULTI_VALUE;
            case RADIO, SELECT -> ApplicationInputType.ENUMERATED_SINGLE_VALUE;
            case DATE -> ApplicationInputType.DATE_VALUE;
            case TEXT, NUMBER, YES_NO, MONEY -> ApplicationInputType.SINGLE_VALUE;
        };
    }
}
