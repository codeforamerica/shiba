package org.codeforamerica.shiba.pages.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class ApplicationData implements Serializable {
    @Serial
    private static final long serialVersionUID = 5573310526258484730L;

    private String id;
    @Setter(AccessLevel.NONE)
    private Instant startTime;
    private String utmSource;
    private FlowType flow = FlowType.UNDETERMINED;
    private boolean isSubmitted = false;
    private PagesData pagesData = new PagesData();
    private Subworkflows subworkflows = new Subworkflows();
    private Map<String, PagesData> incompleteIterations = new HashMap<>();
    private List<UploadedDocument> uploadedDocs = new ArrayList<>();

    public void setStartTimeOnce(Instant instant) {
        if (startTime == null) {
            startTime = instant;
        }
    }

    public PageData getPageData(String pageName) {
        return this.pagesData.getPage(pageName);
    }

    public Subworkflows getSubworkflowsForPageDatasources(List<PageDatasource> pageDatasources) {
        return new Subworkflows(pageDatasources.stream()
                .filter(datasource -> datasource.getGroupName() != null)
                .filter(datasource -> !datasource.isOptional() || subworkflows.containsKey(datasource.getGroupName()))
                .map(datasource -> Map.entry(
                        datasource.getGroupName(),
                        subworkflows.get(datasource.getGroupName())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public boolean hasRequiredSubworkflows(List<PageDatasource> datasources) {
        return datasources.stream()
                .filter(datasource -> datasource.getGroupName() != null)
                .allMatch(datasource -> datasource.isOptional() || getSubworkflows().get(datasource.getGroupName()) != null);
    }

    public NextPage getNextPageName(FeatureFlagConfiguration featureFlags, @NotNull PageWorkflowConfiguration pageWorkflowConfiguration, Integer option) {
        if (pageWorkflowConfiguration.isDirectNavigation()) {
            return pageWorkflowConfiguration.getNextPages().get(option);
        }
        PageData pageData;
        if (pageWorkflowConfiguration.isInAGroup()) {
            pageData = incompleteIterations.get(pageWorkflowConfiguration.getGroupName()).get(pageWorkflowConfiguration.getPageConfiguration().getName());
        } else {
            pageData = pagesData.getPage(pageWorkflowConfiguration.getPageConfiguration().getName());
        }

        if (pageData == null) {
            throw new RuntimeException(String.format("Conditional navigation for %s requires page to have data/inputs.", pageWorkflowConfiguration.getPageConfiguration().getName()));
        }

        return pageWorkflowConfiguration.getNextPages().stream()
                .filter(nextPage -> {
                    boolean isNextPage = true;
                    Condition condition = nextPage.getCondition();
                    if (condition != null) {
                        isNextPage = condition.matches(pageData, pagesData);
                    }
                    if (nextPage.getFlag() != null) {
                        isNextPage &= featureFlags.get(nextPage.getFlag()) == FeatureFlag.ON;
                    }
                    return isNextPage;
                }).findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find suitable next page."));
    }

    public boolean isCCAPApplication() {
        return isApplicationWith(List.of("CCAP"));
    }

    public boolean isCAFApplication() {
        return isApplicationWith(List.of("SNAP", "CASH", "GRH", "EA"));
    }

    public boolean isApplicationWith(List<String> programs) {
        List<String> applicantPrograms = this.getPagesData().safeGetPageInputValue("choosePrograms", "programs");
        boolean applicantWith = programs.stream().anyMatch(applicantPrograms::contains);
        boolean hasHousehold = this.getSubworkflows().containsKey("household");
        boolean householdWith = false;
        if (hasHousehold) {
            householdWith = this.getSubworkflows().get("household").stream().anyMatch(iteration -> {
                List<String> iterationsPrograms = iteration.getPagesData().safeGetPageInputValue("householdMemberInfo", "programs");
                return programs.stream().anyMatch(iterationsPrograms::contains);
            });
        }

        return applicantWith || householdWith;
    }

    public boolean isMedicalExpensesApplication() {
        List<String> medicalExpenses = this.getPagesData().safeGetPageInputValue("medicalExpenses", "medicalExpenses");
        List<String> selectedExpenses = List.of("MEDICAL_INSURANCE_PREMIUMS", "DENTAL_INSURANCE_PREMIUMS", "VISION_INSURANCE_PREMIUMS");
        return selectedExpenses.stream().anyMatch(medicalExpenses::contains);
    }

    public void addUploadedDoc(MultipartFile file, String s3Filepath, String dataURL, String type) {
        UploadedDocument uploadedDocument = new UploadedDocument(file.getOriginalFilename(), s3Filepath, dataURL, type, file.getSize());
        uploadedDocs.add(uploadedDocument);
    }

    public void removeUploadedDoc(String fileToDelete) {
        UploadedDocument toRemove = uploadedDocs.stream()
                .filter(uploadedDocument -> uploadedDocument.getFilename().equals(fileToDelete))
                .findFirst().orElse(null);
        uploadedDocs.remove(toRemove);
    }

    @NotNull
    public Set<String> getApplicantAndHouseholdMemberPrograms() {
        List<String> applicantPrograms = getPagesData().safeGetPageInputValue("choosePrograms", "programs");
        Set<String> applicantAndHouseholdMemberPrograms = new HashSet<>(applicantPrograms);
        boolean hasHousehold = getSubworkflows().containsKey("household");
        if (hasHousehold) {
            Subworkflow householdSubworkflow = getSubworkflows().get("household");
            householdSubworkflow.forEach(iteration ->
                    applicantAndHouseholdMemberPrograms.addAll(iteration.getPagesData().safeGetPageInputValue("householdMemberInfo", "programs")));
        }
        return applicantAndHouseholdMemberPrograms;
    }
}
