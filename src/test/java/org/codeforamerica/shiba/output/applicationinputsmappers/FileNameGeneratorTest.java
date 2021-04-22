package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Hennepin;

class FileNameGeneratorTest {

    CountyMap<MnitCountyInformation> countyMap = new CountyMap<>();

    FileNameGenerator fileNameGenerator = new FileNameGenerator(countyMap);

    Application.ApplicationBuilder defaultApplicationBuilder;

    @BeforeEach
    void setUp() {
        PagesData pagesData = new PagesData();
        ApplicationData applicationData = new ApplicationData();
        PageData chooseProgramsData = new PageData();
        chooseProgramsData.put("programs", InputData.builder().value(emptyList()).build());
        pagesData.put("choosePrograms", chooseProgramsData);
        applicationData.setPagesData(pagesData);
        countyMap.setDefaultValue(MnitCountyInformation.builder()
                .folderId("defaultFolderId")
                .dhsProviderId("defaultDhsProviderId")
                .email("defaultEmail")
                .build());
        defaultApplicationBuilder = Application.builder()
                .id("defaultId")
                .applicationData(applicationData)
                .completedAt(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Test
    void shouldIncludeIdInFileNameForApplication() {
        String applicationId = "someId";
        Application application = defaultApplicationBuilder.id(applicationId).build();
        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);
        assertThat(fileName).contains(applicationId);
    }

    @Test
    void shouldIncludeSubmitDateInCentralTimeZone() {
        Application application = defaultApplicationBuilder.completedAt(ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC)).build();
        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);
        assertThat(fileName).contains("20070909");
    }

    @Test
    void shouldIncludeSubmitTimeInCentralTimeZone() {
        Application application = defaultApplicationBuilder.completedAt(ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:05:59.00Z"), ZoneOffset.UTC)).build();
        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);
        assertThat(fileName).contains("230559");
    }

    @Test
    void shouldIncludeCorrectCountyNPI() {
        String countyNPI = "someNPI";
        County county = Hennepin;
        countyMap.getCounties().put(county, MnitCountyInformation.builder().dhsProviderId(countyNPI).build());
        Application application = defaultApplicationBuilder.county(county).build();

        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);

        assertThat(fileName).contains(countyNPI);
    }

    @Test
    void shouldIncludeProgramCodes() {
        PageData chooseProgramsData = new PageData();
        List<String> programs = new ArrayList<>(List.of(
                "SNAP", "CASH", "GRH", "EA", "CCAP"
        ));
        Collections.shuffle(programs);
        chooseProgramsData.put("programs", InputData.builder().value(programs).build());
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("choosePrograms", chooseProgramsData)));
        Application application = defaultApplicationBuilder.applicationData(applicationData).build();

        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);

        assertThat(fileName).contains("EKFC");
    }

    @Test
    void shouldIncludeProgramCodesForHouseholdMembers() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData householdMemberProgramsPage = new PageData();
        householdMemberProgramsPage.put("programs", InputData.builder().value(List.of("SNAP", "CASH", "GRH", "EA", "CCAP")).build());
        pagesData.put("householdMemberInfo", householdMemberProgramsPage);
        applicationData.getSubworkflows().addIteration("household", pagesData);

        Application application = defaultApplicationBuilder.applicationData(applicationData).build();

        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);

        assertThat(fileName).contains("EKFC");
    }

    @Test
    void omitsProgramCodes_ifNoProgramsAreChosen() {
        ApplicationData applicationData = new ApplicationData();
        Application application = defaultApplicationBuilder.applicationData(applicationData).build();

        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);

        assertThat(fileName).endsWith("defaultId__CAF");
    }

    @Test
    void shouldArrangeNameCorrectlyForPdf() {
        PageData chooseProgramsData = new PageData(Map.of("programs", InputData.builder().value(List.of("SNAP")).build()));
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("choosePrograms", chooseProgramsData)));

        String countyNPI = "someNPI";
        County county = Hennepin;
        countyMap.getCounties().put(county, MnitCountyInformation.builder().dhsProviderId(countyNPI).build());

        String applicationId = "someId";

        Application application = defaultApplicationBuilder
                .id(applicationId)
                .county(county)
                .completedAt(ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
                .applicationData(applicationData)
                .build();

        String fileName = fileNameGenerator.generatePdfFileName(application, Document.CAF);

        assertThat(fileName).isEqualTo(String.format("%s_MNB_%s_%s_%s_%s_%s",
                countyNPI, "20070909", "235959", applicationId, "F", "CAF"));
    }

    @Test
    void shouldArrangeNameCorrectlyForXML() {
        PageData chooseProgramsData = new PageData(Map.of("programs", InputData.builder().value(List.of("SNAP")).build()));
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("choosePrograms", chooseProgramsData)));

        String countyNPI = "someNPI";
        County county = Hennepin;
        countyMap.getCounties().put(county, MnitCountyInformation.builder().dhsProviderId(countyNPI).build());

        String applicationId = "someId";

        Application application = defaultApplicationBuilder
                .id(applicationId)
                .county(county)
                .completedAt(ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
                .applicationData(applicationData)
                .build();

        String fileName = fileNameGenerator.generateXmlFileName(application);

        assertThat(fileName).isEqualTo(String.format("%s_MNB_%s_%s_%s_%s",
                countyNPI, "20070909", "235959", applicationId, "F"));
    }

    @Test
    void shouldFormatNameCorrectlyForFileUploads() {
        MockMultipartFile image = new MockMultipartFile("image", "someImage.jpg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
        MockMultipartFile pdf = new MockMultipartFile("pdf", "somePdf.pdf", MediaType.APPLICATION_PDF_VALUE, "thisIsAPdf".getBytes());
        ApplicationData applicationData = new ApplicationData();
        applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");
        applicationData.addUploadedDoc(pdf, "coolS3FilePath", "documentDataUrl", "application/pdf");

        String countyNPI = "someNPI";
        County county = Hennepin;
        countyMap.getCounties().put(county, MnitCountyInformation.builder().dhsProviderId(countyNPI).build());
        String applicationId = "someId";

        Application application = defaultApplicationBuilder
                .id("someId")
                .county(county)
                .completedAt(ZonedDateTime.ofInstant(Instant.parse("2007-09-10T04:59:59.00Z"), ZoneOffset.UTC))
                .applicationData(applicationData)
                .build();

        String imageName = fileNameGenerator.generateUploadedDocumentName(application, 0, "jpg");
        String pdfName = fileNameGenerator.generateUploadedDocumentName(application, 1, "pdf");

        assertThat(imageName).isEqualTo(String.format("%s_MNB_%s_%s_%s_doc1of2.jpg", countyNPI, "20070909", "235959", applicationId));
        assertThat(pdfName).isEqualTo(String.format("%s_MNB_%s_%s_%s_doc2of2.pdf", countyNPI, "20070909", "235959", applicationId));
    }
}