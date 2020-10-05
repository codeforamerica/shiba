package org.codeforamerica.shiba.pages.enrichment.smartystreets;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Components {
    @JsonProperty("primary_number")
    String primaryNumber;
    @JsonProperty("street_name")
    String streetName;
    @JsonProperty("street_suffix")
    String streetSuffix;
    @JsonProperty("city_name")
    String cityName;
    @JsonProperty("default_city_name")
    String defaultCityName;
    @JsonProperty("state_abbreviation")
    String stateAbbreviation;
    @JsonProperty("zipcode")
    String zipcode;
    @JsonProperty("plus4_code")
    String plus4Code;
    @JsonProperty("secondary_designator")
    String secondaryDesignator;
    @JsonProperty("secondary_number")
    String secondaryNumber;
}
