package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNamesExceptFor;
import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedNameStrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.output.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class AdultRequestingChildcarePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {

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
    AtomicInteger i = new AtomicInteger(0);
    List<String> exceptNameStrings = getListOfSelectedNameStrings(application,
        "childrenInNeedOfCare", "whoNeedsChildCare");
    if (!DocumentListParser.parse(application.getApplicationData()).contains(Document.CCAP) ||
        !application.getApplicationData().getSubworkflows().containsKey("jobs")) {
      return Collections.emptyList();
    } else {
      return application.getApplicationData().getSubworkflows().get("jobs")
          .stream().filter(iteration -> {
            String nameString;
            if (iteration.getPagesData().containsKey("householdSelectionForIncome")) {
              nameString = iteration.getPagesData().getPage("householdSelectionForIncome")
                  .get("whoseJobIsIt").getValue(0);
            } else {
              nameString = "";
            }
            return !exceptNameStrings.contains(nameString);
          })
          .flatMap(iteration -> {
            String nameString = iteration.getPagesData().getPage("householdSelectionForIncome")
                .get("whoseJobIsIt").getValue(0);
            String fullName = FullNameFormatter.format(nameString);
            String employersName = iteration.getPagesData().getPage("employersName")
                .get("employersName").getValue(0);

            Stream<DocumentField> inputs = Stream.of(
                new DocumentField(
                    "adultRequestingChildcareWorking",
                    "fullName",
                    List.of(fullName),
                    DocumentFieldType.SINGLE_VALUE,
                    i.get()),
                new DocumentField(
                    "adultRequestingChildcareWorking",
                    "employersName",
                    List.of(employersName),
                    DocumentFieldType.SINGLE_VALUE,
                    i.get()));
            i.getAndIncrement();
            return inputs;
          })
          .toList();
    }
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
