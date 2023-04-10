package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.*;
import static org.codeforamerica.shiba.TribalNation.RedLakeNation;
import static org.codeforamerica.shiba.TribalNation.UpperSioux;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.mnit.TribalNationConfiguration;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class FilenameGeneratorTest {

  private ServicingAgencyMap<CountyRoutingDestination> countyMap;
  private FilenameGenerator filenameGenerator;
  private Application.ApplicationBuilder defaultApplicationBuilder;
  private ServicingAgencyMap<TribalNationRoutingDestination> tribalNations;
  private CountyRoutingDestination defaultCountyRoutingDestination;

  SnapExpeditedEligibilityDecider decider = mock(SnapExpeditedEligibilityDecider.class);

  @BeforeEach
  void setUp() {
    countyMap = new ServicingAgencyMap<>();
    ApplicationData applicationData = new TestApplicationDataBuilder()
       .withApplicantPrograms(emptyList()) 
        .build();
    defaultCountyRoutingDestination = new CountyRoutingDestination(Hennepin,
        "defaultCountyDhsProviderId", "defaultCountyEmail@example.com", "phoneNumber");
    countyMap.setDefaultValue(defaultCountyRoutingDestination);
    tribalNations = new TribalNationConfiguration().localTribalNations();
    defaultApplicationBuilder = Application.builder()
        .id("defaultId")
        .applicationData(applicationData)
        .completedAt(ZonedDateTime.now(ZoneOffset.UTC));
    filenameGenerator = new FilenameGenerator(countyMap, decider);
  }

  @Test
  void shouldGenerateFilenamesForTheCorrectRoutingDestination() {
    String applicationId = "coolIdBro";
    Application application = defaultApplicationBuilder.id(applicationId).build();
    String countyFilename = filenameGenerator.generatePdfFilename(application,
        Document.CAF, defaultCountyRoutingDestination);
    assertThat(countyFilename).contains(defaultCountyRoutingDestination.getDhsProviderId());

    TribalNationRoutingDestination redLakeRoutingDestination = tribalNations.get(RedLakeNation);
    String fileName = filenameGenerator.generatePdfFilename(application,
        Document.CAF,
        redLakeRoutingDestination);
    assertThat(fileName).contains(redLakeRoutingDestination.getDhsProviderId());
  }
  
	@Test
	void shouldGenerateFilenameForHealthCareRenewal() {
		ApplicationData applicationData = new TestApplicationDataBuilder().withUploadedDocs();
		String applicationId = "randomNumber";
		Application application = defaultApplicationBuilder.id(applicationId).flow(FlowType.HEALTHCARE_RENEWAL)
				.applicationData(applicationData).build();
		String countyFilename = filenameGenerator.generateUploadedDocumentName(application, 1, "pdf",
				defaultCountyRoutingDestination);
		assertThat(countyFilename).contains(defaultCountyRoutingDestination.getDhsProviderId());
		assertThat(countyFilename).contains("HCRenewal");
		assertThat(countyFilename).contains("doc2of2");
	}

  @Test
  void shouldIncludeIdInFileNameForApplication() {
    String applicationId = "someId";
    Application application = defaultApplicationBuilder.id(applicationId).build();
    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);
    assertThat(fileName).contains(applicationId);
    assertThat(fileName).contains(".pdf");
  }

  @Test
  void shouldIncludeSubmitDateInCentralTimeZone() {
    Application application = defaultApplicationBuilder.completedAt(
        ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC)).build();
    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);
    assertThat(fileName).contains("20070909");
  }

  @Test
  void shouldIncludeSubmitTimeInCentralTimeZone() {
    Application application = defaultApplicationBuilder.completedAt(
        ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:05:59.00Z"), ZoneOffset.UTC)).build();
    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);
    assertThat(fileName).contains("230559");
  }

  @Test
  void shouldIncludeCorrectCountyNPI() {
    String countyNPI = "someNPI";
    County county = Hennepin;
    countyMap.getAgencies()
        .put(county, new CountyRoutingDestination(county, countyNPI, "email", "phoneNumber"));
    Application application = defaultApplicationBuilder.county(county).build();

    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);

    assertThat(fileName).contains(countyNPI);
  }

  @Test
  void shouldIncludeProgramCodes() {
    List<String> programs = new ArrayList<>(List.of(
        "SNAP", "CASH", "GRH", "EA", "CCAP"
    ));
    Collections.shuffle(programs);
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(programs).build();
    Application application = defaultApplicationBuilder.applicationData(applicationData).build();

    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);

    assertThat(fileName).contains("EKFC");
  }

  @Test
  void shouldIncludeProgramCodesForHouseholdMembers() {
    List<String> programs = List.of("SNAP", "CASH", "GRH", "EA", "CCAP");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHouseholdMemberPrograms(programs).build();

    Application application = defaultApplicationBuilder.applicationData(applicationData).build();

    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);

    assertThat(fileName).contains("EKFC");
  }

  @Test
  void omitsProgramCodes_ifNoProgramsAreChosen() {
    ApplicationData applicationData = new ApplicationData();
    Application application = defaultApplicationBuilder.applicationData(applicationData).build();

    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);

    assertThat(fileName).endsWith("defaultId__CAF.pdf");
  }

  @Test
  void shouldArrangeNameCorrectlyForPdf() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("SNAP")).build();

    String countyNPI = "someNPI";
    County county = Hennepin;
    countyMap.getAgencies()
        .put(county, new CountyRoutingDestination(county, countyNPI, "email", "phoneNumber"));

    String applicationId = "someId";

    Application application = defaultApplicationBuilder
        .id(applicationId)
        .county(county)
        .completedAt(
            ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();

    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);

    assertThat(fileName).isEqualTo(String.format("%s_MNB_%s_%s_%s_%s_%s.pdf",
        countyNPI, "20070909", "235959", applicationId, "F", "CAF"));
  }

  @Test
  void shouldArrangeNameCorrectlyForXML() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("SNAP")).build();

    String countyNPI = "someNPI";
    County county = Hennepin;
    countyMap.getAgencies()
        .put(county, new CountyRoutingDestination(county, countyNPI, "email", "phoneNumber"));

    String applicationId = "someId";

    Application application = defaultApplicationBuilder
        .id(applicationId)
        .county(county)
        .completedAt(
            ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();

    String fileName = filenameGenerator.generateXmlFilename(application);

    assertThat(fileName).isEqualTo(String.format("%s_MNB_%s_%s_%s_%s.xml",
        countyNPI, "20070909", "235959", applicationId, "F"));
  }

  @Nested
  class WithUploadedDocs {

    private Application application;
    private final String countyNPI = "someNPI";
    private final String applicationId = "someId";

    @BeforeEach
    void setUp() {
      MockMultipartFile image = new MockMultipartFile("image", "someImage.jpg",
          MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
      MockMultipartFile pdf = new MockMultipartFile("pdf", "somePdf.pdf",
          MediaType.APPLICATION_PDF_VALUE, "thisIsAPdf".getBytes());
      ApplicationData applicationData = new ApplicationData();
      applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");
      applicationData.addUploadedDoc(pdf, "coolS3FilePath", "documentDataUrl", "application/pdf");

      County county = Olmsted;
      countyMap.getAgencies()
          .put(county, new CountyRoutingDestination(county, countyNPI, "email", "phoneNumber"));

      application = defaultApplicationBuilder
          .id(applicationId)
          .county(county)
          .completedAt(
              ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
          .applicationData(applicationData)
          .build();
    }

    @Test
    void shouldFormatNameCorrectlyForFileUploads() {
      String imageName = filenameGenerator.generateUploadedDocumentName(application, 0, "jpg");
      String pdfName = filenameGenerator.generateUploadedDocumentName(application, 1, "pdf");

      assertThat(imageName).isEqualTo(String
          .format("%s_MNB_%s_%s_%s_doc1of2.jpg", countyNPI, "20070909", "235959", applicationId));
      assertThat(pdfName).isEqualTo(String
          .format("%s_MNB_%s_%s_%s_doc2of2.pdf", countyNPI, "20070909", "235959", applicationId));
    }

    @Test
    void shouldIncludeCorrectDhsProviderIdWhenARoutingDestinationIsProvided() {
      String providerId = "someOtherProviderId";
      RoutingDestination routingDestination = new TribalNationRoutingDestination(UpperSioux,
          providerId, "", "");
      String imageName = filenameGenerator.generateUploadedDocumentName(application, 0, "jpg",
          routingDestination);
      String pdfName = filenameGenerator.generateUploadedDocumentName(application, 1, "pdf",
          routingDestination);

      assertThat(imageName).isEqualTo(String
          .format("%s_MNB_%s_%s_%s_doc1of2.jpg", providerId, "20070909", "235959", applicationId));
      assertThat(pdfName).isEqualTo(String
          .format("%s_MNB_%s_%s_%s_doc2of2.pdf", providerId, "20070909", "235959", applicationId));
    }
  }

  @Test
  void shouldBeDocInsteadOfMnbIfCountyIsHennepin() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("SNAP")).build();

    String hennepinCountyNPI = "hennepinNPI";
    County hennepinCounty = Hennepin;
    String olmstedCountyNPI = "olmstedNPI";
    County olmstedCounty = County.Olmsted;
    countyMap.getAgencies()
        .put(hennepinCounty, new CountyRoutingDestination(hennepinCounty, hennepinCountyNPI, "email", "phoneNumber"));
    countyMap.getAgencies()
        .put(olmstedCounty, new CountyRoutingDestination(olmstedCounty, olmstedCountyNPI, "email", "phoneNumber"));
    String applicationId = "someId";

    Application hennepinApplication = defaultApplicationBuilder
        .id(applicationId)
        .county(hennepinCounty)
        .completedAt(
            ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();

    Application olmstedApplication = defaultApplicationBuilder
        .id(applicationId)
        .county(olmstedCounty)
        .completedAt(
            ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();

    String fileName = filenameGenerator.generateUploadedDocumentName(hennepinApplication, 0, "pdf");
    String notHennepinFileName = filenameGenerator
        .generateUploadedDocumentName(olmstedApplication, 1, "jpg");

    assertThat(fileName).contains("hennepinNPI_DOC");
    assertThat(fileName).doesNotContain("hennepinNPI_MNB");
    assertThat(notHennepinFileName).doesNotContain("olmstedNPI_DOC");
    assertThat(notHennepinFileName).contains("olmstedNPI_MNB");
  }

  @Test
  void shouldBeDocInsteadOfMnbIfCountyIsOther() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("SNAP")).build();

    String otherCountyNpi = "hennepinNPI";
    County hennepinCounty = Hennepin;

    countyMap.getAgencies()
        .put(hennepinCounty, new CountyRoutingDestination(hennepinCounty, otherCountyNpi, "email", "phoneNumber"));
    String applicationId = "someId";

    Application hennepinApplication = defaultApplicationBuilder
        .id(applicationId)
        .county(hennepinCounty)
        .completedAt(
            ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();

    String fileName = filenameGenerator.generateUploadedDocumentName(hennepinApplication, 0, "pdf");

    assertThat(fileName).contains("hennepinNPI_DOC");
    assertThat(fileName).doesNotContain("hennepinNPI_MNB");
  }

  @Test
  void shouldAppendExpeditedFileNameCorrectlyForCAFPdf() {
    TestApplicationDataBuilder applicationDataBuilder = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("SNAP"));

    ApplicationData applicationData = applicationDataBuilder
        .build();
    String countyNPI = "someNPI";
    County county = Hennepin;
    countyMap.getAgencies()
        .put(county, new CountyRoutingDestination(county, countyNPI, "email", "phoneNumber"));

    String applicationId = "someId";
    when(decider.decide(applicationData)).thenReturn(ELIGIBLE);

    Application application = defaultApplicationBuilder
        .id(applicationId)
        .county(county)
        .completedAt(
            ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();

    String fileName = filenameGenerator.generatePdfFilename(application, Document.CAF);
    assertThat(fileName).isEqualTo(String.format("%s_MNB_%s_%s_%s_%s_%s%s.pdf",
        countyNPI, "20070909", "235959", applicationId, "F", "CAF", "_EXPEDITED"));
  }
}
