package org.codeforamerica.shiba.output.applicationinputsmappers;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper.formInputTypeToApplicationInputType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

@Component
public class OneToOneApplicationInputsMapper implements ApplicationInputsMapper {

  private final ApplicationConfiguration applicationConfiguration;
  private final Map<String, String> personalDataMappings;

  public OneToOneApplicationInputsMapper(ApplicationConfiguration applicationConfiguration,
      Map<String, String> personalDataMappings) {
    this.applicationConfiguration = applicationConfiguration;
    this.personalDataMappings = personalDataMappings;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    ApplicationData data = application.getApplicationData();
    return applicationConfiguration.getPageDefinitions().stream()
        .flatMap(pageConfiguration -> pageConfiguration.getFlattenedInputs().stream()
            .map(input -> Map.entry(pageConfiguration.getName(), input)))
        .filter(entry -> data.getPagesData().getPage(entry.getKey()) != null)
        .map(entry -> {
          FormInput formInput = entry.getValue();
          List<String> values = ofNullable(data.getPagesData().getPage(entry.getKey())
              .get(formInput.getName()))
              .map(InputData::getValue)
              .orElse(List.of());
          List<String> valuesForInput = values.stream()
              .map(value -> {
                if (Recipient.CLIENT.equals(recipient) &&
                    personalDataMappings.get(formInput.getName()) != null &&
                    !value.isEmpty()) {
                  return personalDataMappings.get(formInput.getName());
                } else {
                  return value;
                }
              })
              .collect(Collectors.toList());
          return new DocumentField(
              entry.getKey(),
              formInput.getName(),
              valuesForInput,
              formInputTypeToApplicationInputType(formInput.getType()));
        })
        .collect(toList());
  }
}
