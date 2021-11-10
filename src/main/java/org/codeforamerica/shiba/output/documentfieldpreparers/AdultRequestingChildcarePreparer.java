package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNamesExceptFor;
import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedNameStrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.output.*;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class AdultRequestingChildcarePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {

    List<DocumentField> fields;

    List<DocumentField> lookingForAJob = getAdultsForSection(application, "whoIsLookingForAJob",
        "whoIsLookingForAJob", "adultRequestingChildcareLookingForJob");
    fields = new ArrayList<>(lookingForAJob);

    List<DocumentField> goingToSchool = getAdultsForSection(application, "whoIsGoingToSchool",
        "whoIsGoingToSchool", "adultRequestingChildcareGoingToSchool");
    fields.addAll(goingToSchool);

    List<DocumentField> working = getAdultsForWorkingSection(application);
    fields.addAll(working);

    return fields;
  }

  private static List<DocumentField> getAdultsForWorkingSection(Application application) {
    boolean shouldReturnEmptyList =
        !DocumentListParser.parse(application.getApplicationData()).contains(Document.CCAP) ||
            !application.getApplicationData().getSubworkflows().containsKey("jobs");
    if (shouldReturnEmptyList) {
      return Collections.emptyList();
    }

    Subworkflow jobsSubworkflow = application.getApplicationData().getSubworkflows().get("jobs");
    List<String> childrenNames = getListOfSelectedNameStrings(application, "childrenInNeedOfCare",
        "whoNeedsChildCare");

    List<Iteration> jobsHeldByAdults = jobsSubworkflow.stream()
        .filter(iteration -> {
          String name = "";
          if (iteration.getPagesData().containsKey("householdSelectionForIncome")) {
            name = iteration.getPagesData().getPage("householdSelectionForIncome")
                .get("whoseJobIsIt").getValue(0);
          }
          return !childrenNames.contains(name);
        }).toList();

    List<DocumentField> fields = new ArrayList<>();
    for (int i = 0; i < jobsHeldByAdults.size(); i++) {
      Iteration iteration = jobsHeldByAdults.get(i);
      String nameString = iteration.getPagesData().getPage("householdSelectionForIncome")
          .get("whoseJobIsIt").getValue(0);
      String fullName = FullNameFormatter.format(nameString);
      String employersName = iteration.getPagesData().getPage("employersName")
          .get("employersName").getValue(0);

      fields.add(new DocumentField("adultRequestingChildcareWorking", "fullName", List.of(fullName),
          DocumentFieldType.SINGLE_VALUE, i));

      fields.add(new DocumentField("adultRequestingChildcareWorking", "employersName",
          List.of(employersName), DocumentFieldType.SINGLE_VALUE, i));
    }

    return fields;
  }

  @NotNull
  private static List<DocumentField> getAdultsForSection(Application application,
      String pageName, String inputName, String outputName) {
    List<String> adults = getListOfSelectedFullNamesExceptFor(application, pageName, inputName,
        "childrenInNeedOfCare", "whoNeedsChildCare");

    List<DocumentField> fields = new ArrayList<>();
    for (int i = 0; i < adults.size(); i++) {
      DocumentField name = new DocumentField(outputName, "fullName", List.of(adults.get(i)),
          DocumentFieldType.SINGLE_VALUE, i);
      fields.add(name);
    }
    return fields;
  }
}
