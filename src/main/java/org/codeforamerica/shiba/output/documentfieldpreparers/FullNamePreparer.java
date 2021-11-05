package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.SubworkflowIterationScopeTracker.IterationScopeInfo;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

@Component
public class FullNamePreparer implements DocumentFieldPreparer {

  private final ApplicationConfiguration applicationConfiguration;

  public FullNamePreparer(ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    String pageName = "householdSelectionForIncome";
    String whoseJob = "whoseJobIsIt";
    String groupName = "jobs";
    String inputName = "employeeFullName";
    Subworkflow subworkflow = application.getApplicationData().getSubworkflows().get(groupName);
    Optional<PageGroupConfiguration> pageGroupConfiguration = ofNullable(applicationConfiguration)
        .map(ApplicationConfiguration::getPageGroups)
        .map(pageGroups -> pageGroups.get(groupName));

    return ofNullable(subworkflow).orElse(new Subworkflow()).stream()
        .filter(iteration -> iteration.getPagesData().get(pageName) != null)
        .flatMap(iteration -> {
          PageData pageData = iteration.getPagesData().get(pageName);
          String fullName = FullNameFormatter.format(pageData.get(whoseJob).getValue(0));

          Stream<DocumentField> inputs = Stream.of(new DocumentField(pageName, inputName,
              List.of(fullName), DocumentFieldType.SINGLE_VALUE,
              subworkflow.indexOf(iteration)));

          if (scopeTracker != null && pageGroupConfiguration.isPresent()) {
            IterationScopeInfo scopeInfo = scopeTracker
                .getIterationScopeInfo(pageGroupConfiguration.get(), iteration);
            if (scopeInfo != null) {
              inputs = Stream.concat(inputs, Stream.of(new DocumentField(
                  scopeInfo.getScope() + "_" + pageName,
                  inputName,
                  List.of(fullName),
                  DocumentFieldType.SINGLE_VALUE,
                  scopeInfo.getIndex()
              )));
            }
          }

          return inputs;
        }).collect(Collectors.toList());
  }
}
