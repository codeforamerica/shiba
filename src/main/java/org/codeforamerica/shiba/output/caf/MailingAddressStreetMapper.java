package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.GENERAL_DELIVERY_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.GENERAL_DELIVERY_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SAME_MAILING_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SAME_MAILING_ADDRESS2;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressStreetMapper implements ApplicationInputsMapper {

  private final static String GENERAL_DELIVERY = "General Delivery";
  private final FeatureFlagConfiguration featureFlagConfiguration;

  public MailingAddressStreetMapper(FeatureFlagConfiguration featureFlagConfiguration) {
    this.featureFlagConfiguration = featureFlagConfiguration;
  }

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    PagesData pagesData = application.getApplicationData().getPagesData();

    // Not applicable or enough information to continue
    if (featureFlagConfiguration.get("apply-without-address").isOff()) {
      PageData homeAddressPageData = pagesData.get("homeAddress");
      boolean hasInput =
          homeAddressPageData != null && homeAddressPageData.containsKey("sameMailingAddress");
      if (!hasInput) {
        return List.of();
      }
    } else {
      PageData mailingAddressPageData = pagesData.get("mailingAddress");
      boolean hasInput = mailingAddressPageData != null && mailingAddressPageData
          .containsKey("sameMailingAddress");
      String generalDeliveryCityInput = getFirstValue(pagesData, GENERAL_DELIVERY_CITY);
      if (!hasInput && generalDeliveryCityInput == null) {
        return List.of();
      }
    }

    // Use home address for mailing
    boolean sameAsHomeAddress = Boolean.parseBoolean(getFirstValue(pagesData,
        featureFlagConfiguration.get("apply-without-address").isOff() ? SAME_MAILING_ADDRESS
            : SAME_MAILING_ADDRESS2));
    if (sameAsHomeAddress) {
      return createAddressInputsFromHomeAddress(pagesData);
    }

    String usesEnriched = pagesData
        .getPageInputFirstValue("mailingAddressValidation", "useEnrichedAddress");
    if (usesEnriched == null) {
      // General delivery
      return createGeneralDeliveryAddressInputs(pagesData);
    }

    // Use mailing address application data
    return createAddressInputsFromMailingAddress(pagesData);
  }

  private List<ApplicationInput> createGeneralDeliveryAddressInputs(PagesData pagesData) {
    return List.of(new ApplicationInput(
        "mailingAddress",
        "selectedStreetAddress",
        List.of(GENERAL_DELIVERY),
        ApplicationInputType.SINGLE_VALUE
    ), new ApplicationInput(
        "mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, GENERAL_DELIVERY_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE
    ), new ApplicationInput(
        "mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, GENERAL_DELIVERY_CITY)),
        ApplicationInputType.SINGLE_VALUE
    ), new ApplicationInput(
        "mailingAddress",
        "selectedState",
        List.of("MN"),
        ApplicationInputType.SINGLE_VALUE
    ));
  }

  /**
   * Create mailing address inputs from the home address application data.
   *
   * @param pagesData application data to check
   * @return mailing address inputs
   */
  private List<ApplicationInput> createAddressInputsFromHomeAddress(PagesData pagesData) {
    boolean usesEnriched = Boolean.parseBoolean(
        pagesData.getPageInputFirstValue("homeAddressValidation", "useEnrichedAddress"));
    if (usesEnriched) {
      return createMailingInputs(pagesData,
          ENRICHED_HOME_STREET,
          ENRICHED_HOME_APARTMENT_NUMBER,
          ENRICHED_HOME_ZIPCODE,
          ENRICHED_HOME_CITY,
          ENRICHED_HOME_STATE);
    } else {
      return createMailingInputs(pagesData,
          HOME_STREET,
          HOME_APARTMENT_NUMBER,
          HOME_ZIPCODE,
          HOME_CITY,
          HOME_STATE);
    }
  }

  /**
   * Create the mailing address inputs from the mailing address application data. It will be there
   * if the home address isn't also being used for mailing address.
   *
   * @param pagesData application data to check
   * @return mailing address inputs
   */
  private List<ApplicationInput> createAddressInputsFromMailingAddress(PagesData pagesData) {
    boolean usesEnriched = Boolean.parseBoolean(
        pagesData.getPageInputFirstValue("mailingAddressValidation", "useEnrichedAddress"));
    if (usesEnriched) {
      return createMailingInputs(pagesData,
          ENRICHED_MAILING_STREET,
          ENRICHED_MAILING_APARTMENT_NUMBER,
          ENRICHED_MAILING_ZIPCODE,
          ENRICHED_MAILING_CITY,
          ENRICHED_MAILING_STATE);
    } else {
      return createMailingInputs(pagesData,
          MAILING_STREET,
          MAILING_APARTMENT_NUMBER,
          MAILING_ZIPCODE,
          MAILING_CITY,
          MAILING_STATE);
    }
  }

  private List<ApplicationInput> createMailingInputs(PagesData pagesData, Field street,
      Field apartment, Field zipcode, Field city, Field state) {
    return List.of(new ApplicationInput(
        "mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, street)),
        ApplicationInputType.SINGLE_VALUE
    ), new ApplicationInput(
        "mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, apartment)),
        ApplicationInputType.SINGLE_VALUE
    ), new ApplicationInput(
        "mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, zipcode)),
        ApplicationInputType.SINGLE_VALUE
    ), new ApplicationInput(
        "mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, city)),
        ApplicationInputType.SINGLE_VALUE
    ), new ApplicationInput(
        "mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, state)),
        ApplicationInputType.SINGLE_VALUE
    ));
  }
}
