package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.lang.Boolean.parseBoolean;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.GENERAL_DELIVERY_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.GENERAL_DELIVERY_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.NO_PERMANENT_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SAME_MAILING_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.USE_ENRICHED_HOME_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.USE_ENRICHED_MAILING_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressStreetPreparer implements DocumentFieldPreparer {

  private final static String GENERAL_DELIVERY = "General Delivery";

  private final CityInfoConfiguration cityInfoConfiguration;
  private final ServicingAgencyMap<CountyRoutingDestination> countyMap;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public MailingAddressStreetPreparer(
      CityInfoConfiguration cityInfoConfiguration,
      ServicingAgencyMap<CountyRoutingDestination> countyMap) {
    this.cityInfoConfiguration = cityInfoConfiguration;
    this.countyMap = countyMap;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    PagesData pagesData = application.getApplicationData().getPagesData();

    // Not applicable or enough information to continue
    PageData mailingAddressPageData = pagesData.get("mailingAddress");
    boolean hasInput = mailingAddressPageData != null && mailingAddressPageData
        .containsKey("sameMailingAddress");
    String generalDeliveryCityInput = getFirstValue(pagesData, GENERAL_DELIVERY_CITY);
    if (!hasInput && generalDeliveryCityInput == null) {
      return List.of();
    }

    // Use home address for mailing
    boolean sameAsHomeAddress = parseBoolean(getFirstValue(pagesData, SAME_MAILING_ADDRESS));
    if (sameAsHomeAddress) {
      return createAddressInputsFromHomeAddress(pagesData);
    }

    boolean noPermanentAddress = parseBoolean(getFirstValue(pagesData, NO_PERMANENT_ADDRESS));
    String generalDeliveryCity = getFirstValue(pagesData, GENERAL_DELIVERY_CITY);
    if (noPermanentAddress == true  && generalDeliveryCity != null) {
      // General delivery
      return createGeneralDeliveryAddressInputs(pagesData);
    }

    // Use mailing address application data
    return createAddressInputsFromMailingAddress(pagesData);
  }

  private List<DocumentField> createGeneralDeliveryAddressInputs(PagesData pagesData) {
    // Default values if no post office provided
    String streetAddress = GENERAL_DELIVERY;
    String zipcode = getFirstValue(pagesData, GENERAL_DELIVERY_ZIPCODE);
    String cityName = getFirstValue(pagesData, GENERAL_DELIVERY_CITY);
    County identifiedCounty = County.valueOf(getFirstValue(pagesData, IDENTIFY_COUNTY));

    // If post office information available, set mailing address application inputs to that
    Map<String, String> cityInfo =
        cityInfoConfiguration.getCityToZipAndCountyMapping().get(cityName);
    County countyFromCity = County.valueOf(cityInfo.get("county").replace(" ", ""));
    County county = identifiedCounty == County.Other ? countyFromCity : identifiedCounty;
    CountyRoutingDestination countyInfo = countyMap.get(county);

    if (countyInfo.getPostOfficeAddress() != null) {
      streetAddress = countyInfo.getPostOfficeAddress().getStreet();
      zipcode = countyInfo.getPostOfficeAddress().getZipcode();
      cityName = countyInfo.getPostOfficeAddress().getCity();
    }

    return List.of(
        createSingleMailingInput("selectedStreetAddress", streetAddress),
        createSingleMailingInput("selectedZipCode", zipcode),
        createSingleMailingInput("selectedCity", cityName),
        createSingleMailingInput("selectedState", "MN"));
  }

  /**
   * Create mailing address inputs from the home address application data.
   *
   * @param pagesData application data to check
   * @return mailing address inputs
   */
  private List<DocumentField> createAddressInputsFromHomeAddress(PagesData pagesData) {
    boolean usesEnriched = parseBoolean(getFirstValue(pagesData, USE_ENRICHED_HOME_ADDRESS));
    if (usesEnriched) {
      return createMailingInputs(pagesData,
          ENRICHED_HOME_STREET,
          ENRICHED_HOME_APARTMENT_NUMBER,
          ENRICHED_HOME_ZIPCODE,
          ENRICHED_HOME_CITY,
          ENRICHED_HOME_STATE,
          ENRICHED_HOME_COUNTY);
    } else {
      return createMailingInputs(pagesData,
          HOME_STREET,
          HOME_APARTMENT_NUMBER,
          HOME_ZIPCODE,
          HOME_CITY,
          HOME_STATE,
          HOME_COUNTY);
    }
  }

  /**
   * Create the mailing address inputs from the mailing address application data. It will be there
   * if the home address isn't also being used for mailing address.
   *
   * @param pagesData application data to check
   * @return mailing address inputs
   */
  private List<DocumentField> createAddressInputsFromMailingAddress(PagesData pagesData) {
    boolean usesEnriched = parseBoolean(getFirstValue(pagesData, USE_ENRICHED_MAILING_ADDRESS));
    if (usesEnriched) {
      return createMailingInputs(pagesData,
          ENRICHED_MAILING_STREET,
          ENRICHED_MAILING_APARTMENT_NUMBER,
          ENRICHED_MAILING_ZIPCODE,
          ENRICHED_MAILING_CITY,
          ENRICHED_MAILING_STATE,
          ENRICHED_MAILING_COUNTY);
    } else {
      return createMailingInputs(pagesData,
          MAILING_STREET,
          MAILING_APARTMENT_NUMBER,
          MAILING_ZIPCODE,
          MAILING_CITY,
          MAILING_STATE,
          MAILING_COUNTY);
    }
  }

  private List<DocumentField> createMailingInputs(PagesData pagesData, Field street,
      Field apartment, Field zipcode, Field city, Field state, Field county) {
    // county Fields default to "Other" but we don't want to write that to the PDF
    String countyValue = getFirstValue(pagesData, county);
    if (County.Other.toString().equals(countyValue)) {
      countyValue = "";
    }
    return List.of(
        createSingleMailingInput("selectedStreetAddress", getFirstValue(pagesData, street)),
        createSingleMailingInput("selectedApartmentNumber", getFirstValue(pagesData, apartment)),
        createSingleMailingInput("selectedZipCode", getFirstValue(pagesData, zipcode)),
        createSingleMailingInput("selectedCity", getFirstValue(pagesData, city)),
        createSingleMailingInput("selectedState", getFirstValue(pagesData, state)),
        createSingleMailingInput("selectedCounty", countyValue));
  }

  @NotNull
  private DocumentField createSingleMailingInput(String name, String value) {
    return new DocumentField("mailingAddress", name, value, SINGLE_VALUE);
  }
}
