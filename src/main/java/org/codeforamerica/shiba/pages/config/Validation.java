package org.codeforamerica.shiba.pages.config;


import org.apache.commons.validator.GenericValidator;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/* Validation on an input field */
public enum Validation {
    NONE(strings -> true),
    NOT_BLANK(strings -> !String.join("", strings).isBlank()),
    NONE_BLANK(strings -> strings.stream().noneMatch(String::isBlank)),
    SELECT_AT_LEAST_ONE(strings -> strings.size() > 0),
    SSN(strings -> String.join("", strings).replace("-", "").matches("\\d{9}")),
    DATE(strings -> {
        return GenericValidator.isDate(String.join("/", strings), "MM/dd/yyyy", true)
                || GenericValidator.isDate(String.join("/", strings), "M/dd/yyyy", true)
                || GenericValidator.isDate(String.join("/", strings), "M/d/yyyy", true)
                || GenericValidator.isDate(String.join("/", strings), "MM/d/yyyy", true);
    }),
    DOB_VALID(strings -> {
    	String dobString = String.join("/", strings);
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    	try {
    		Date dobDate = sdf.parse(dobString);
	    	boolean notFutureDate = dobDate.getTime() < new Date().getTime();
	    	boolean notBefore1900 = Integer.parseInt(strings.get(2)) >= 1900;
	    	return notFutureDate && notBefore1900;
    	} catch(ParseException e) {
    		return false;
    	}
    }),
    ZIPCODE(strings -> String.join("", strings).matches("\\d{5}")),
    CASE_NUMBER(strings -> String.join("", strings).matches("\\d{4,7}")),
    STATE(strings -> Set.of("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY", "AS", "DC", "FM", "GU", "MH", "MP", "PR", "VI", "AB", "BC", "MB", "NB", "NF", "NS", "ON", "PE", "PQ", "SK")
            .contains(strings.get(0).toUpperCase())),
    PHONE(strings -> String.join("", strings).replaceAll("[^\\d]", "").matches("[2-9]\\d{9}")),
    PHONE_STARTS_WITH_ONE(strings -> !String.join("", strings).replaceAll("[^\\d]", "").startsWith("1")),
    MONEY(strings -> String.join("", strings).matches("^(\\d{1,3},(\\d{3},)*\\d{3}|\\d+)(\\.\\d{1,2})?")),
    NUMBER(strings -> strings.get(0).trim().matches("\\d*")),
    EMAIL(strings -> String.join("", strings).trim().matches("[a-zA-Z0-9!#$%&'*+=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?")),
    COUNTY(strings -> !String.join("", strings).equals("SelectYourCounty")),
    CITY(strings -> {
        return !String.join("", strings).equals("SelectYourCity");
    });

    private final Predicate<List<String>> rule;

    Validation(Predicate<List<String>> rule) {
        this.rule = rule;
    }

    public Boolean apply(List<String> value) {
        return this.rule.test(value);
    }

}
