package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNamesExceptFor;
import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedNameStrings;

@Component
public class AdultRequestingChildcareInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        Stream<ApplicationInput> lookingForAJob = getAdultsForSection(application, "whoIsLookingForAJob", "whoIsLookingForAJob", "adultRequestingChildcareLookingForJob");
        Stream<ApplicationInput> goingToSchool = getAdultsForSection(application, "whoIsGoingToSchool", "whoIsGoingToSchool", "adultRequestingChildcareGoingToSchool");
        Stream<ApplicationInput> working = getAdultsForWorkingSection(application);

        return Stream.of(lookingForAJob, goingToSchool, working)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    private static Stream<ApplicationInput> getAdultsForWorkingSection(Application application) {
        AtomicInteger i = new AtomicInteger(0);
        List<String> exceptNameStrings = getListOfSelectedNameStrings(application, "childrenInNeedOfCare", "whoNeedsChildCare");
        return Optional.ofNullable(application.getApplicationData().getSubworkflows().get("jobs"))
                .orElse(new Subworkflow())
                .stream().filter(iteration -> {
                    String nameString = iteration.getPagesData().getPage("householdSelectionForIncome").get("whoseJobIsIt").getValue(0);
                    return !exceptNameStrings.contains(nameString);
                })
                .flatMap(iteration -> {
                    String nameString = iteration.getPagesData().getPage("householdSelectionForIncome").get("whoseJobIsIt").getValue(0);
                    String fullName = FullNameFormatter.format(nameString);
                    String employersName = iteration.getPagesData().getPage("employersName").get("employersName").getValue(0);

                    Stream<ApplicationInput> inputs = Stream.of(
                            new ApplicationInput(
                                    "adultRequestingChildcareWorking",
                                    "fullName",
                                    List.of(fullName),
                                    ApplicationInputType.SINGLE_VALUE,
                                    i.get()),
                            new ApplicationInput(
                                    "adultRequestingChildcareWorking",
                                    "employersName",
                                    List.of(employersName),
                                    ApplicationInputType.SINGLE_VALUE,
                                    i.get()));
                    i.getAndIncrement();
                    return inputs;
                });
    }

    @NotNull
    private static Stream<ApplicationInput> getAdultsForSection(Application application, String pageName, String inputName, String outputName) {
        List<String> adults = getListOfSelectedFullNamesExceptFor(application, pageName, inputName, "childrenInNeedOfCare", "whoNeedsChildCare");
        AtomicInteger i = new AtomicInteger(0);
        return adults.stream()
                .map(fullName ->
                        new ApplicationInput(outputName,
                                "fullName",
                                List.of(fullName),
                                ApplicationInputType.SINGLE_VALUE,
                                i.getAndIncrement()));
    }
}
