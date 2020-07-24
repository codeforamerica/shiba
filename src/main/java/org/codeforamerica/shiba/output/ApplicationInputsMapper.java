package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.config.FormInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.List;

public interface ApplicationInputsMapper {
    List<ApplicationInput> map(ApplicationData data);

    static ApplicationInputType formInputTypeToApplicationInputType(FormInputType type) {
        return switch (type) {
            case CHECKBOX -> ApplicationInputType.ENUMERATED_MULTI_VALUE;
            case RADIO, SELECT -> ApplicationInputType.ENUMERATED_SINGLE_VALUE;
            case DATE -> ApplicationInputType.DATE_VALUE;
            case TEXT, NUMBER, YES_NO, MONEY, INCREMENTER, TEXTAREA -> ApplicationInputType.SINGLE_VALUE;
        };
    }
}
