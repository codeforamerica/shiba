package org.codeforamerica.shiba;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class Metadata {
    @JsonProperty("county_name")
    String countyName;
}