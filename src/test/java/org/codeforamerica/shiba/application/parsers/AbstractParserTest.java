package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE, classes = {ParserTestConfiguration.class})
@ActiveProfiles("test")
public class AbstractParserTest {
    @Autowired
    protected ParsingConfiguration parsingConfiguration;

    @Autowired
    protected FeatureFlagConfiguration featureFlagConfiguration;
}
