package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RACE_AND_ETHNICITY;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RaceAndEthnicityMapper extends OneToManyApplicationInputsMapper {

  private static final List<String> RACE_AND_ETHNICITY_OPTIONS = List.of(
      "ASIAN",
      "AMERICAN_INDIAN_OR_ALASKA_NATIVE",
      "BLACK_OR_AFRICAN_AMERICAN",
      "NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER",
      "WHITE");

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams("raceAndEthnicity", RACE_AND_ETHNICITY, RACE_AND_ETHNICITY_OPTIONS);
  }
}
