package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.SubworkflowIterationScopeTracker.IterationScopeInfo;
import org.codeforamerica.shiba.pages.config.*;
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
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    ApplicationData data = application.getApplicationData();
    Map<String, PageGroupConfiguration> pageGroups = applicationConfiguration.getPageGroups();

    List<DocumentField> fields = new ArrayList<>(
        getCount(data, pageGroups));

    data.getSubworkflows().forEach((groupName, subworkflow) ->
        subworkflow.forEach(iteration -> {
          PagesData pagesData = iteration.getPagesData();
          pagesData.forEach((pageName, pageData) -> {

            if (pageData == null) {
              return;
            }

            List<FormInput> inputConfigurations = applicationConfiguration.getPageDefinitions()
                .stream()
                .filter(pageConfig -> pageConfig.getName().equals(pageName)).findAny()
                .map(PageConfiguration::getInputs)
                .orElse(emptyList());

            List<DocumentField> fieldsForthisPage = pageData.entrySet().stream()
                .flatMap(pageDataEntry -> {
                  String inputName = pageDataEntry.getKey();
                  InputData inputData = pageDataEntry.getValue();

                  List<String> valuesForInput = getValuesForInput(recipient, inputName,
                      inputData);

                  FormInputType inputType = inputConfigurations.stream()
                      .filter(inputConfiguration -> inputConfiguration.getName()
                          .equals(inputName))
                      .findAny()
                      .map(FormInput::getType)
                      .orElse(FormInputType.TEXT);

                  Stream<DocumentField> inputs = Stream.of(new DocumentField(
                      pageName,
                      inputName,
                      valuesForInput,
                      DocumentFieldPreparer
                          .formInputTypeToApplicationInputType(inputType),
                      subworkflow.indexOf(iteration)));
                  IterationScopeInfo scopeInfo = scopeTracker
                      .getIterationScopeInfo(pageGroups.get(groupName), iteration);
                  if (scopeInfo != null) {
                    inputs = Stream.concat(inputs, Stream.of(new DocumentField(
                        scopeInfo.getScope() + "_" + pageName,
                        inputName,
                        valuesForInput,
                        DocumentFieldPreparer
                            .formInputTypeToApplicationInputType(inputType),
                        scopeInfo.getIndex()
                    )));
                  }
                  return inputs;
                }).toList();
            fields.addAll(fieldsForthisPage);
          });
        }));
    return fields;
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
