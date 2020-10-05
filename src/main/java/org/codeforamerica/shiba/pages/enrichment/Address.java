package org.codeforamerica.shiba.pages.enrichment;

import lombok.Value;

@Value
public class Address {
    String street;
    String city;
    String state;
    String zipcode;
    String apartmentNumber;
}
