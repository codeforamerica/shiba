package org.codeforamerica.shiba;

import java.util.List;

public interface ApplicationInputsMapper {
    List<ApplicationInput> map(PagesData data);

    static ApplicationInputType formInputTypeToApplicationInputType(FormInputType type) {
        return switch (type) {
            case CHECKBOX -> ApplicationInputType.ENUMERATED_MULTI_VALUE;
            case RADIO, SELECT -> ApplicationInputType.ENUMERATED_SINGLE_VALUE;
            case DATE -> ApplicationInputType.DATE_VALUE;
            case TEXT, NUMBER, LEGAL_TERMS -> ApplicationInputType.SINGLE_VALUE;
            case YES_NO -> null;//TODO
        };
    }
}
