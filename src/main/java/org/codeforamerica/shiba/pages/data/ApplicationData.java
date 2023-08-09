package org.codeforamerica.shiba.pages.data;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLICANT_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLYING_FOR_TRIBAL_TANF;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.BASIC_CRITERIA_CERTAIN_POPS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_RELATIONSHIP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MEDICAL_EXPENSES;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PERSONAL_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WRITTEN_LANGUAGE_PREFERENCES;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.HOUSEHOLD;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.config.NextPage;
import org.codeforamerica.shiba.pages.config.PageDatasource;
import org.codeforamerica.shiba.pages.config.PageWorkflowConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class ApplicationData implements Serializable {

  @Serial
  private static final long serialVersionUID = 5573310526258484730L;

  private String id;
  private String clientIP;
  @Setter(AccessLevel.NONE)
  private Instant startTime;
  private String utmSource;
  private String lastPageViewed;
  private String deviceType;
  private String devicePlatform;
  private List<ExpeditedEligibility> expeditedEligibility = new ArrayList<>();
  private FlowType flow = FlowType.UNDETERMINED;
  private boolean isSubmitted = false;
  private PagesData pagesData = new PagesData();
  private Subworkflows subworkflows = new Subworkflows();
  private Map<String, PagesData> incompleteIterations = new HashMap<>();
  private List<UploadedDocument> uploadedDocs = new ArrayList<>();
  private String originalCounty;

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
        .filter(datasource -> !datasource.isOptional() || subworkflows
            .containsKey(datasource.getGroupName()))
        .map(datasource -> Map.entry(
            datasource.getGroupName(),
            subworkflows.get(datasource.getGroupName())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  public boolean hasRequiredSubworkflows(List<PageDatasource> datasources) {
    return datasources.stream()
        .filter(datasource -> datasource.getGroupName() != null)
        .allMatch(datasource -> datasource.isOptional()
            || getSubworkflows().get(datasource.getGroupName()) != null);
  }

  public NextPage getNextPageName(
      FeatureFlagConfiguration featureFlags,
      @NotNull PageWorkflowConfiguration currentPage,
      Integer option) {
    if (currentPage.isDirectNavigation()) {
      return currentPage.getNextPages().get(option);
    }

    return currentPage.getNextPages().stream()
        .filter(
            potentialNextPage ->
                nextPageConditionsAreSatisfied(featureFlags, currentPage, potentialNextPage)
        )
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Cannot find suitable next page."));
  }

  private boolean nextPageConditionsAreSatisfied(FeatureFlagConfiguration featureFlags,
      @NotNull PageWorkflowConfiguration currentPage, NextPage nextPage) {
    boolean satisfied = true;
    Condition condition = nextPage.getCondition();
    if (condition != null) {
      if (currentPage.isInAGroup()) {
        satisfied = condition.matches(
            incompleteIterations.get(currentPage.getGroupName())
                .get(currentPage.getPageConfiguration().getName()),
            pagesData);
      } else {
        var datasourcePages = getDatasourceDataForPageIncludingSubworkflows(currentPage);
        satisfied = datasourcePages.satisfies(condition);
      }
    }
    if (nextPage.getFlag() != null) {
      satisfied &= featureFlags.get(nextPage.getFlag()) == FeatureFlag.ON;
    }
    return satisfied;
  }

  public boolean isCCAPApplication() {
    return isApplicationWith(List.of("CCAP"));
  }

  public boolean isCAFApplication() {
    return isApplicationWith(List.of("SNAP", "CASH", "GRH", "EA")) ||
        getBooleanValue(pagesData, APPLYING_FOR_TRIBAL_TANF);
  }

  public boolean isCertainPopsApplication() {
    return isApplicationWith(List.of("CERTAIN_POPS")) &&
        !getValues(pagesData, BASIC_CRITERIA_CERTAIN_POPS).contains("NONE");
  }

  public boolean isApplicationWith(List<String> programs) {
    return getApplicantAndHouseholdMemberPrograms().stream().anyMatch(programs::contains);
  }

  public boolean isMedicalExpensesApplication() {
    List<String> medicalExpenses = getValues(pagesData, MEDICAL_EXPENSES);
    List<String> selectedExpenses = List
        .of("MEDICAL_INSURANCE_PREMIUMS", "DENTAL_INSURANCE_PREMIUMS",
            "VISION_INSURANCE_PREMIUMS");
    return selectedExpenses.stream().anyMatch(medicalExpenses::contains);
  }

  public void addUploadedDoc(MultipartFile file, String s3Filepath, String thumbnailFilepath,
      String type) {
    UploadedDocument uploadedDocument = new UploadedDocument(file.getOriginalFilename(),
        s3Filepath,
        thumbnailFilepath, type, file.getSize());
    uploadedDocs.add(uploadedDocument);
  }
  
  public void addUploadedDoc(MultipartFile file, String s3Filepath, String thumbnailFilepath,
      String type, String sysFileName) {
    UploadedDocument uploadedDocument = new UploadedDocument(file.getOriginalFilename(),
        s3Filepath,
        thumbnailFilepath, type, file.getSize(), sysFileName);
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
    List<String> applicantPrograms = getValues(pagesData, APPLICANT_PROGRAMS);
    Set<String> applicantAndHouseholdMemberPrograms = new HashSet<>(applicantPrograms);
    List<String> householdPrograms = getValues(this, HOUSEHOLD, HOUSEHOLD_PROGRAMS);
    if (householdPrograms != null) {
      applicantAndHouseholdMemberPrograms.addAll(householdPrograms);
    }
    return applicantAndHouseholdMemberPrograms;
  }
  
  @NotNull
  public int getApplicantAndHouseholdMemberSize() {
	int householdSize = 0;
	// compute applicant size, some tests don't generate the applicant, just the household members
    List<String> applicantFirstName = getValues(pagesData, PERSONAL_INFO_FIRST_NAME);
    if (applicantFirstName != null) {
    	householdSize = householdSize + applicantFirstName.size(); // the applicant
    }
    List<String> householdNames = getValues(this, HOUSEHOLD, HOUSEHOLD_INFO_FIRST_NAME);
    if (householdNames != null) {
      householdSize = householdSize + householdNames.size(); // the rest of the household
    }
    return householdSize;
  }

  @NotNull
  public long getHouseholdMemberWithoutSpouse() {
   
    List<String> householdRelation = getValues(this, HOUSEHOLD, HOUSEHOLD_INFO_RELATIONSHIP);
    if (householdRelation != null) {
      return householdRelation.stream().filter(relation->!relation.contains("spouse")).count();
    }
    return 0;
  }

  // method that takes the set given in the method above it, and uses that to build the string we want to show on the success page
  public String combinedApplicationProgramsList() {
    Set<String> programList = getApplicantAndHouseholdMemberPrograms();
    Set<String> programName = new HashSet<>();
    programList.forEach(program -> {
      if (program.equalsIgnoreCase("EA")) {
        programName.add("Emergency");
      }
      if (program.equalsIgnoreCase("CASH")) {
        programName.add("Cash");
      }
      if (program.equalsIgnoreCase("GRH")) {
        programName.add("Housing");
      }
      if (program.equalsIgnoreCase("SNAP")) {
        programName.add("SNAP");
      }
    });

    return String.join(", ", programName);
  }

  @NotNull
  public PagesData getDatasourceDataForPageIncludingSubworkflows(PageWorkflowConfiguration page) {
    PagesData pagesData = getPagesData();
    Subworkflows subworkflows = getSubworkflows();
    Map<String, PageData> pages = new HashMap<>();
    var thisPageName = page.getPageConfiguration().getName();
    var thisPageData = pagesData.get(thisPageName);
    pages.put(thisPageName, thisPageData);
    page.getDatasources().stream()
        .filter(datasource -> datasource.getPageName() != null)
        .forEach(datasource -> {
          var pageData = new PageData();
          if (datasource.getGroupName() == null) {
            // if datasource is not a subworkflow
            pageData.mergeInputDataValues(pagesData.get(datasource.getPageName()));
          } else if (subworkflows.containsKey(datasource.getGroupName())) {
            // if datasource is a subworkflow
            subworkflows.get(datasource.getGroupName()).stream()
                .map(iteration -> iteration.getPagesData().getPage(datasource.getPageName()))
                .forEach(pageData::mergeInputDataValues);
          }

          pages.put(datasource.getPageName(), pageData);
        });
    return new PagesData(pages);
  }

  public Locale getLocale() {
    if (getValues(pagesData, WRITTEN_LANGUAGE_PREFERENCES).contains("SPANISH")) {
      return new Locale("es");
    }
    return LocaleContextHolder.getLocale();
  }
}
