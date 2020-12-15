package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class GrossMonthlyIncomeMapper implements ApplicationInputsMapper {

    private final ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser;

    public GrossMonthlyIncomeMapper(ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser) {
        this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        return grossMonthlyIncomeParser.parse(application.getApplicationData()).stream()
                .map(jobIncomeInformation ->
                        new ApplicationInput(
                                "employee",
                                "grossMonthlyIncome",
                                List.of(String.valueOf(jobIncomeInformation.grossMonthlyIncome())),
                                SINGLE_VALUE,
                                jobIncomeInformation.getIteration()))
                .collect(Collectors.toList());
    }

}

