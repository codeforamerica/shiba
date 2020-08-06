package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class SubworkflowInputMapper implements ApplicationInputsMapper {
    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        return data.getSubworkflows()
                .entrySet()
                .stream()
                .flatMap(subworkflowEntry -> IntStream.range(0, subworkflowEntry.getValue().size())
                        .mapToObj(index -> subworkflowEntry.getValue().get(index)
                                .entrySet()
                                .stream()
                                .flatMap(iteration -> iteration.getValue()
                                        .entrySet()
                                        .stream()
                                        .map(inputData -> new ApplicationInput(
                                                iteration.getKey(),
                                                inputData.getKey(),
                                                inputData.getValue().getValue(),
                                                ApplicationInputType.SINGLE_VALUE,
                                                index)))))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }
}
