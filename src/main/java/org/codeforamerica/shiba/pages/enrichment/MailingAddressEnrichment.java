package org.codeforamerica.shiba.pages.enrichment;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressEnrichment extends AddressEnrichment {

  public MailingAddressEnrichment(
      LocationClient locationClient,
      Map<String, County> countyZipCodeMap) {
    this.locationClient = locationClient;
    this.countyZipCodeMap = countyZipCodeMap;
  }

  @Override
  protected Address parseAddress(PagesData pagesData) {
    return new Address(
        getFirstValue(pagesData, MAILING_STREET),
        getFirstValue(pagesData, MAILING_CITY),
        getFirstValue(pagesData, MAILING_STATE),
        getFirstValue(pagesData, MAILING_ZIPCODE),
        getFirstValue(pagesData, MAILING_APARTMENT_NUMBER),
        null);
  }
}
