package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;

@Component
public class SubworkflowInputMapper implements ApplicationInputsMapper {
    private final ApplicationConfiguration applicationConfiguration;

    public SubworkflowInputMapper(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        ApplicationData data = application.getApplicationData();
        return applicationConfiguration.getWorkflow().values().stream()
                .filter(workflowConfiguration -> workflowConfiguration.getGroupName() != null)
                .flatMap(pageWorkflowConfiguration -> {
                    Subworkflow subworkflow = data.getSubworkflows().get(pageWorkflowConfiguration.getGroupName());
                    if (subworkflow == null) {
                        return Stream.empty();
                    }

                    Stream<ApplicationInput> applicationInputStream = subworkflow.stream()
                            .flatMap(iteration -> {
                                        PageData pageData = iteration.get(pageWorkflowConfiguration.getPageConfiguration().getName());
                                        if (pageData == null) {
                                            return empty();
                                        }
                                        return pageWorkflowConfiguration.getPageConfiguration().getInputs().stream()
                                                .map(input -> new ApplicationInput(
                                                        pageWorkflowConfiguration.getPageConfiguration().getName(),
                                                        input.getName(),
                                                        pageData.get(input.getName()).getValue(),
                                                        ApplicationInputsMapper.formInputTypeToApplicationInputType(input.getType()),
                                                        subworkflow.indexOf(iteration)
                                                ));
                                    }
                            );
                    return Stream.concat(
                            applicationInputStream,
                            Stream.of(new ApplicationInput(
                                    pageWorkflowConfiguration.getGroupName(),
                                    "count",
                                    List.of(String.valueOf(subworkflow.size())),
                                    ApplicationInputType.SINGLE_VALUE
                            )));
                })
                .collect(Collectors.toList());
    }
}
