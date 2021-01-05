package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.Document.*;

@Component
public class DocumentListParser extends ApplicationDataParser<List<Document>> {
    public DocumentListParser(ParsingConfiguration parsingConfiguration) { super(parsingConfiguration); }

    @Override
    public List<Document> parse(ApplicationData applicationData) {
        if(isCCAPApplication(applicationData)) {
            return List.of(CCAP, CAF);
        } else {
            return List.of(CAF);
        }
    }

    private boolean isCCAPApplication(ApplicationData applicationData) {
        List<String> applicantPrograms = applicationData.getPagesData().safeGetPageInputValue("choosePrograms", "programs");
        boolean applicantHasCCAP = applicantPrograms.contains("CCAP");
        boolean hasHousehold = applicationData.getSubworkflows().containsKey("household");
        boolean householdHasCCAP = false;
        if (hasHousehold) {
            householdHasCCAP = applicationData.getSubworkflows().get("household").stream().anyMatch(iteration ->
                iteration.getPagesData().safeGetPageInputValue("householdMemberInfo", "programs").contains("CCAP"));
        }
        return applicantHasCCAP || householdHasCCAP;
    }
}
