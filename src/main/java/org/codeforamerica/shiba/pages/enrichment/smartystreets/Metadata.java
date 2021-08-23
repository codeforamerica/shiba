package org.codeforamerica.shiba.pages.enrichment.smartystreets;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class Metadata {

  @JsonProperty("county_name")
  String countyName;
}
