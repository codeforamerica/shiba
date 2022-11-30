package org.codeforamerica.shiba.pages.config;

import static org.codeforamerica.shiba.pages.config.FormInputType.CHECKBOX;
import static org.codeforamerica.shiba.pages.config.FormInputType.RADIO;

import java.util.List;
import lombok.Value;

@Value
public class FormInputTemplate {

  FormInputType type;
  String name;
  String customInputFragment;
  PromptMessage promptMessage;
  String helpMessageKey;
  String placeholder;
  List<String> validationErrorMessageKeys;
  OptionsWithDataSourceTemplate options;
  List<FormInputTemplate> followUps;
  List<String> followUpValues;
  Boolean readOnly;
  String defaultValue;
  List<PageDatasource> datasources;
  String customFollowUps;
  String inputPostfix;
  String helpMessageKeyBelow;
  String noticeMessage;
  Boolean validationIcon;
  
  public String fragment() {
    return switch (type) {
      case TEXT, LONG_TEXT, NUMBER, SELECT, MONEY, TEXTAREA, HOURLY_WAGE, PHONE, SSN, NOTICE -> "single-input";
      case DATE -> "date-input";
      case RADIO -> "radio-input";
      case CHECKBOX -> "checkbox-input";
      case PEOPLE_CHECKBOX -> "people-checkbox-input";
      case YES_NO -> "yes-no-input";
      case HIDDEN -> "hidden-input";
      case CUSTOM -> customInputFragment;
    };
  }

  @SuppressWarnings("unused")
  public boolean hasFollowUps() {
    return !followUps.isEmpty() && !followUpValues.isEmpty();
  }
  
  @SuppressWarnings("unused")
  public boolean hasCustomFollowUps() {
    return Boolean.parseBoolean(customFollowUps);
  }

  public boolean needsAriaLabel() {
    return promptMessage == null &&
        type != CHECKBOX &&
        type != RADIO &&
        type != FormInputType.PEOPLE_CHECKBOX;
  }

  public boolean hasPromptMessageKey() {
	  return promptMessage != null && promptMessage.getPromptMessageKey() != null;
  }
  
  public boolean isRadioOrCheckbox() {
    return type == CHECKBOX || type == RADIO;
  }
}
