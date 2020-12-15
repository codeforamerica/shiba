package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.PotentialDerivedValuesConfiguration;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DerivedValueApplicationInputsMapper implements ApplicationInputsMapper {
    private final PotentialDerivedValuesConfiguration derivedValuesConfiguration;

    public DerivedValueApplicationInputsMapper(PotentialDerivedValuesConfiguration derivedValuesConfiguration) {
        this.derivedValuesConfiguration = derivedValuesConfiguration;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        ApplicationData data = application.getApplicationData();
        return this.derivedValuesConfiguration.stream()
                .map(potentialDerivedValues -> potentialDerivedValues
                        .getValues()
                        .stream()
                        .filter(derivedValue -> derivedValue.shouldDeriveValue(data))
                        .findFirst()
                        .flatMap(derivedValue -> derivedValue.getValue().resolveOptional(data)
                                .map(value -> new ApplicationInput(
                                        potentialDerivedValues.getGroupName(),
                                        potentialDerivedValues.getFieldName(),
                                        value,
                                        derivedValue.getType(),
                                        potentialDerivedValues.getIteration()
                                ))
                        )
                )
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
