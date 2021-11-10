package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer.formInputTypeToApplicationInputType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class OneToOneDocumentFieldPreparer implements DocumentFieldPreparer {

  private final ApplicationConfiguration applicationConfiguration;
  private final Map<String, String> personalDataMappings;

  public OneToOneDocumentFieldPreparer(ApplicationConfiguration applicationConfiguration,
      Map<String, String> personalDataMappings) {
    this.applicationConfiguration = applicationConfiguration;
    this.personalDataMappings = personalDataMappings;
  }

  private record PageNameToInput(String pageName, FormInput input) {

  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    ApplicationData applicationData = application.getApplicationData();

    List<PageNameToInput> pageNameToInputList = getPageNameToInputList();

    return pageNameToInputList.stream()
        .filter(nameToInput -> doesPageHaveData(applicationData, nameToInput.pageName()))
        .map(nameToInput -> {
          FormInput formInput = nameToInput.input();
          List<String> values = ofNullable(
              applicationData.getPagesData().getPage(nameToInput.pageName())
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
              nameToInput.pageName(),
              formInput.getName(),
              valuesForInput,
              formInputTypeToApplicationInputType(formInput.getType()));
        })
        .collect(toList());
  }

  private boolean doesPageHaveData(ApplicationData applicationData, String pageName) {
    return applicationData.getPagesData().getPage(pageName) != null;
  }

  @NotNull
  private List<PageNameToInput> getPageNameToInputList() {
    List<PageNameToInput> pageNameToInputList = new ArrayList<>();
    applicationConfiguration.getPageDefinitions().forEach(pageConfiguration -> {
          for (FormInput input : pageConfiguration.getFlattenedInputs()) {
            PageNameToInput pageNameToInput = new PageNameToInput(
                pageConfiguration.getName(),
                input);
            pageNameToInputList.add(pageNameToInput);
          }
        }
    );
    return pageNameToInputList;
  }
}
