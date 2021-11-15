package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.FormInputType;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SubworkflowPreparer implements DocumentFieldPreparer {

  private final ApplicationConfiguration applicationConfiguration;
  private final Map<String, String> personalDataMappings;

  public SubworkflowPreparer(ApplicationConfiguration applicationConfiguration,
      Map<String, String> personalDataMappings) {
    this.applicationConfiguration = applicationConfiguration;
    this.personalDataMappings = personalDataMappings;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    ApplicationData data = application.getApplicationData();
    Map<String, PageGroupConfiguration> pageGroups = applicationConfiguration.getPageGroups();

    List<DocumentField> fields = new ArrayList<>(getCount(data, pageGroups));

    data.getSubworkflows().forEach((groupName, subworkflow) ->
        // for each subworkflow
        subworkflow.forEach(iteration -> {
          // for each iteration in that subworkflow
          PagesData pagesData = iteration.getPagesData();
          pagesData.forEach((pageName, pageData) -> {
            // for each page in that iteration
            if (pageData == null) {
              return;
            }

            List<FormInput> inputConfigurations = applicationConfiguration.getPageDefinitions()
                .stream()
                .filter(pageConfig -> pageConfig.getName().equals(pageName)).findAny()
                .map(PageConfiguration::getInputs)
                .orElse(emptyList());

            pageData.forEach((inputName, inputData) -> {
              // for each input on that page
              List<String> valuesForInput = getValuesForInput(recipient, inputName, inputData);
              FormInputType inputType = getInputType(inputConfigurations, inputName);
              DocumentFieldType documentFieldType = DocumentFieldPreparer.formInputTypeToApplicationInputType(
                  inputType);

              // Add DocumentField to our final result for the input
              fields.add(new DocumentField(
                  pageName,
                  inputName,
                  valuesForInput,
                  documentFieldType,
                  subworkflow.indexOf(iteration)));
            });
          });
        }));
    return fields;
  }

  @NotNull
  private FormInputType getInputType(List<FormInput> inputConfigurations, String inputName) {
    return inputConfigurations.stream()
        .filter(inputConfiguration -> inputConfiguration.getName().equals(inputName))
        .findAny()
        .map(FormInput::getType)
        .orElse(FormInputType.TEXT);
  }

  @NotNull
  private List<String> getValuesForInput(Recipient recipient, String inputName,
      InputData inputData) {
    return inputData.getValue().stream()
        .map(value -> {
          if (Recipient.CLIENT.equals(recipient)
              && personalDataMappings.get(inputName) != null
              && !value.isEmpty()) {
            return personalDataMappings.get(inputName);
          }
          return value;
        })
        .collect(Collectors.toList());
  }

  @NotNull
  private List<DocumentField> getCount(ApplicationData data,
      Map<String, PageGroupConfiguration> pageGroups) {
    return pageGroups.entrySet().stream().map(entry -> {
      String groupName = entry.getKey();
      PageGroupConfiguration pageGroupConfiguration = entry.getValue();

      Integer startingCount = ofNullable(pageGroupConfiguration.getStartingCount()).orElse(0);

      Subworkflow subworkflow = data.getSubworkflows().get(groupName);
      Integer subworkflowCount = ofNullable(subworkflow).map(ArrayList::size).orElse(0);

      return new DocumentField(
          groupName,
          "count",
          List.of(String.valueOf(subworkflowCount + startingCount)),
          DocumentFieldType.SINGLE_VALUE
      );
    }).collect(Collectors.toList());
  }
}
