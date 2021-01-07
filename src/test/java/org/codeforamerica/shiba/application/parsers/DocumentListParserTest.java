package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;

class DocumentListParserTest {
    DocumentListParser documentListParser = new DocumentListParser(new ParsingConfiguration());
    PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    @Test
    void parseShouldAlwaysIncludeCAFByDefault() {
        Application application = Application.builder().applicationData(new ApplicationData()).build();

        assertThat(documentListParser.parse(application.getApplicationData())).containsExactlyInAnyOrder(CAF);
    }

    @Test
    void parseShouldIncludeCCAPWhenProgramsContainCCAP() {
        Application application = Application.builder().applicationData(new ApplicationData()).build();
        ApplicationData applicationData = application.getApplicationData();
        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("choosePrograms", Map.of("programs", List.of("CCAP", "SNAP")))
        ));
        applicationData.setPagesData(pagesData);

        assertThat(documentListParser.parse(applicationData)).containsExactlyInAnyOrder(CAF, CCAP);
    }

    @Test
    void parseShouldIncludeCCAPWhenHouseholdMembersProgramsIncludeCCAP() {
        Application application = Application.builder().applicationData(new ApplicationData()).build();
        Subworkflows subworkflows = new Subworkflows();
        ApplicationData applicationData = application.getApplicationData();
        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CASH", "CCAP")
                ))
        ));
        subworkflows.addIteration("household", pagesData);
        applicationData.setSubworkflows(subworkflows);

        assertThat(documentListParser.parse(applicationData)).containsExactlyInAnyOrder(CAF, CCAP);
    }
}