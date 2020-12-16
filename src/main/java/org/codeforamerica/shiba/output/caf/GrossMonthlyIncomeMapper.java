package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker.IterationScopeInfo;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class GrossMonthlyIncomeMapper implements ApplicationInputsMapper {

    private final ApplicationConfiguration applicationConfiguration;
    private final ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser;

    public GrossMonthlyIncomeMapper(ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser, ApplicationConfiguration applicationConfiguration) {
        this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        PageGroupConfiguration pageGroupConfiguration = applicationConfiguration.getPageGroups().get("jobs");
        return grossMonthlyIncomeParser.parse(application.getApplicationData()).stream()
                .flatMap(jobIncomeInformation -> {

                    String pageName = "employee";
                    String inputName = "grossMonthlyIncome";
                    Stream<ApplicationInput> inputs = Stream.of(new ApplicationInput(
                            pageName,
                            inputName,
                            List.of(String.valueOf(jobIncomeInformation.grossMonthlyIncome())),
                            SINGLE_VALUE,
                            jobIncomeInformation.getIndexInJobsSubworkflow()));


                    IterationScopeInfo scopeInfo = scopeTracker.getIterationScopeInfo(pageGroupConfiguration, jobIncomeInformation.getIteration());
                    if (scopeInfo != null) {
                        inputs = Stream.concat(inputs, Stream.of(new ApplicationInput(
                                scopeInfo.getScope() + "_" + pageName,
                                inputName,
                                List.of(String.valueOf(jobIncomeInformation.grossMonthlyIncome())),
                                SINGLE_VALUE,
                                scopeInfo.getIndex()
                        )));
                    }

                    return inputs;
                })
                .collect(Collectors.toList());
    }

}

