package org.codeforamerica.shiba.pages.config;


import org.apache.commons.lang3.Range;
import org.apache.commons.validator.GenericValidator;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public enum Validation {
    NONE(strings -> true),
    NOT_BLANK(strings -> !String.join("", strings).isBlank()),
    SELECT_AT_LEAST_ONE(strings -> strings.size() > 0),
    SSN(strings -> String.join("", strings).matches("\\d{9}")),
    DATE(strings -> GenericValidator.isDate(String.join("/", strings), "MM/dd/yyyy", true)),
    ZIPCODE(strings -> String.join("", strings).matches("\\d{5}")),
    STATE(strings -> Set.of("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY", "AS", "DC", "FM", "GU", "MH", "MP", "PR", "VI", "AB", "BC", "MB", "NB", "NF", "NS", "ON", "PE", "PQ", "SK")
            .contains(strings.get(0))),
    PHONE(strings -> String.join("", strings).matches("[2-9]\\d{9}")),
    MONEY(strings -> String.join("", strings).matches("[-]?\\d+")),
    NON_NEGATIVE_INTEGER(string -> String.join("", string).matches("\\d+")),
    NUMBER_OF_JOBS(strings -> NOT_BLANK.apply(strings) && NON_NEGATIVE_INTEGER.apply(strings) && Range.between(0, 20).contains(Integer.valueOf(strings.get(0))));

    private final Predicate<List<String>> rule;

    Validation(Predicate<List<String>> rule) {
        this.rule = rule;
    }

    public Boolean apply(List<String> value) {
        return this.rule.test(value);
    }

}
