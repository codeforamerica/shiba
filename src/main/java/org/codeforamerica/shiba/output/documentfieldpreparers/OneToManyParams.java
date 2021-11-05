package org.codeforamerica.shiba.output.documentfieldpreparers;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;

/**
 * @see OneToManyDocumentFieldPreparer
 */
@Data
@AllArgsConstructor
class OneToManyParams {

  private String pageName;
  private Field field;
  private List<String> yesNoOptions;
}