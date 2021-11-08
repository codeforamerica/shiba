package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.*;
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
    Optional<PageGroupConfiguration> pageGroupConfiguration = ofNullable(applicationConfiguration)
        .map(ApplicationConfiguration::getPageGroups)
        .map(pageGroups -> pageGroups.get(groupName));

    List<DocumentField> fields = new ArrayList<>();

    Subworkflow subworkflow = application.getApplicationData().getSubworkflows().get(groupName);
    Subworkflow notNullSubworkflow = ofNullable(subworkflow).orElse(new Subworkflow());

    notNullSubworkflow.stream()
        .filter(iteration -> iteration.getPagesData().get(pageName) != null)
        .forEach(iteration -> {
          PageData pageData = iteration.getPagesData().get(pageName);
          String fullName = FullNameFormatter.format(pageData.get(whoseJob).getValue(0));

          fields.add(new DocumentField(pageName, inputName,
              List.of(fullName), DocumentFieldType.SINGLE_VALUE,
              subworkflow.indexOf(iteration)));

          if (scopeTracker != null && pageGroupConfiguration.isPresent()) {
            IterationScopeInfo scopeInfo = scopeTracker
                .getIterationScopeInfo(pageGroupConfiguration.get(), iteration);
            if (scopeInfo != null) {
              fields.add(new DocumentField(
                  scopeInfo.getScope() + "_" + pageName,
                  inputName,
                  List.of(fullName),
                  DocumentFieldType.SINGLE_VALUE,
                  scopeInfo.getIndex()
              ));
            }
          }
        });
    return fields;
  }
}
