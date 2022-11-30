package org.codeforamerica.shiba.output.documentfieldpreparers;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.FormInputType;

public interface DocumentFieldPreparer {

  static DocumentFieldType formInputTypeToApplicationInputType(FormInputType type) {
    return switch (type) {
      case CHECKBOX, PEOPLE_CHECKBOX -> DocumentFieldType.ENUMERATED_MULTI_VALUE;
      case RADIO, SELECT -> DocumentFieldType.ENUMERATED_SINGLE_VALUE;
      case DATE -> DocumentFieldType.DATE_VALUE;
      case TEXT, LONG_TEXT, HOURLY_WAGE, NUMBER, YES_NO, MONEY, TEXTAREA, PHONE, SSN, CUSTOM, NOTICE -> DocumentFieldType.SINGLE_VALUE;
      case HIDDEN -> DocumentFieldType.UNUSED;
    };
  }

  List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient);
}
