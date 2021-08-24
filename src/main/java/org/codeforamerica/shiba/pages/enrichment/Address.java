package org.codeforamerica.shiba.pages.enrichment;

import lombok.Value;

@Value
public class Address {

  String street;
  String city;
  String state;
  String zipcode;
  String apartmentNumber;
  String county;

  public Address(String street, String city, String state, String zipcode, String apartmentNumber,
      String county) {
    this.street = street;
    this.city = city;
    this.state = state;
    this.zipcode = zipcode;
    this.apartmentNumber = apartmentNumber;
    this.county = county;
  }
}
