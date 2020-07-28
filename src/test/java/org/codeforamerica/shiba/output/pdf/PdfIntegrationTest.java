package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PdfIntegrationTest {
    @Autowired
    private ApplicationInputsMappers mappers;

    ApplicationData data = new ApplicationData();
    PagesData pagesData = new PagesData();

    @BeforeEach
    void setUp() {
        data.setSubmissionTime("something");
    }

    @Test
    void shouldMapNoForUnearnedIncomeOptionsThatAreNotChecked() {
        PageData pageData = new PageData();
        pageData.put(
                "unearnedIncome",
                InputData.builder()
                        .value(List.of("SOCIAL_SECURITY", "CHILD_OR_SPOUSAL_SUPPORT"))
                        .build()
        );
        pagesData.putPage("unearnedIncome", pageData);
        data.setPagesData(pagesData);

        List<ApplicationInput> applicationInputs = mappers.map(data);

        assertThat(applicationInputs).contains(
                new ApplicationInput("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY", "CHILD_OR_SPOUSAL_SUPPORT"), ApplicationInputType.ENUMERATED_MULTI_VALUE),
                new ApplicationInput("unearnedIncome", "noSSI", List.of("No"), ApplicationInputType.ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noVeteransBenefits", List.of("No"), ApplicationInputType.ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noUnemployment", List.of("No"), ApplicationInputType.ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noWorkersCompensation", List.of("No"), ApplicationInputType.ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noRetirement", List.of("No"), ApplicationInputType.ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noTribalPayments", List.of("No"), ApplicationInputType.ENUMERATED_SINGLE_VALUE)

        );
    }
}
