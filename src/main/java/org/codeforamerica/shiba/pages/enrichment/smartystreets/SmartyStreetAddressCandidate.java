package org.codeforamerica.shiba.pages.enrichment.smartystreets;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SmartyStreetAddressCandidate {
    Metadata metadata;
    Components components;
    @JsonProperty("delivery_line_1")
    String deliveryLine1;
}
