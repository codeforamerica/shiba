package org.codeforamerica.shiba.output.applicationinputsmappers;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;

/**
 * @see org.codeforamerica.shiba.output.applicationinputsmappers.OneToManyApplicationInputsMapper
 */
@Data
@AllArgsConstructor
class OneToManyParams {

  private String pageName;
  private Field field;
  private List<String> yesNoOptions;
}