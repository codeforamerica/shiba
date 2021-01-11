package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;

@Component
public class DocumentListParser extends ApplicationDataParser<List<Document>> {
    public DocumentListParser(ParsingConfiguration parsingConfiguration) { super(parsingConfiguration); }

    @Override
    public List<Document> parse(ApplicationData applicationData) {
        ArrayList<Document> documents = new ArrayList<>();
        if (isCCAPApplication(applicationData)) {
            documents.add(CCAP);
        }
        if (isCAFApplication(applicationData)) {
            documents.add(CAF);
        }

        return documents;
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

    private boolean isCAFApplication(ApplicationData applicationData) {
        List<String> applicantPrograms = applicationData.getPagesData().safeGetPageInputValue("choosePrograms", "programs");
        List<String> availablePrograms = List.of("SNAP", "CASH", "GRH", "EA");
        boolean applicantIsCAF = availablePrograms.stream().anyMatch(applicantPrograms::contains);
        boolean hasHousehold = applicationData.getSubworkflows().containsKey("household");
        boolean householdIsCAF = false;
        if (hasHousehold) {
            householdIsCAF = applicationData.getSubworkflows().get("household").stream().anyMatch(iteration -> {
                List<String> iterationsPrograms = iteration.getPagesData().safeGetPageInputValue("householdMemberInfo", "programs");
                return availablePrograms.stream().anyMatch(iterationsPrograms::contains);
            });
        }

        return applicantIsCAF || householdIsCAF;
    }
}
