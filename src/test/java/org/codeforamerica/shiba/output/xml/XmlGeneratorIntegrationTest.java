package org.codeforamerica.shiba.output.xml;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.Option;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SpringBootTest
@ActiveProfiles("test")
public class XmlGeneratorIntegrationTest {

  @Autowired
  private FileGenerator xmlGenerator;

  @Value("classpath:OnlineApplication.xsd")
  private Resource onlineApplicationSchema;

  @Autowired
  private ApplicationConfiguration applicationConfiguration;

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private Clock clock;

  @Test
  void shouldProduceAValidDocument() throws Exception {
    String applicationId = applicationRepository.getNextId();
    Map<String, PageData> data = applicationConfiguration.getPageDefinitions().stream()
        .map(pageConfiguration -> {
          Map<String, InputData> inputDataMap = pageConfiguration.getFlattenedInputs().stream()
              .collect(toMap(
                  FormInput::getName,
                  input -> {
                    if (input.getReadOnly() && input.getDefaultValue() != null) {
                      return new InputData(List.of(input.getDefaultValue()));
                    }
                    @NotNull List<String> value = switch (input.getType()) {
                      case RADIO, SELECT -> List.of(input.getOptions().getSelectableOptions().get(
                              new Random().nextInt(input.getOptions().getSelectableOptions().size()))
                          .getValue());
                      case CHECKBOX -> input.getOptions().getSelectableOptions().subList(0,
                              new Random().nextInt(input.getOptions().getSelectableOptions().size())
                                  + 1).stream()
                          .map(Option::getValue)
                          .collect(Collectors.toList());
                      case DATE -> List.of(LocalDate.ofEpochDay(0).plusDays(new Random().nextInt())
                          .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")).split("/"));
                      case YES_NO -> List.of(String.valueOf(new Random().nextBoolean()));
                      case MONEY -> List.of(Integer.valueOf(0).toString());
                      default -> Optional.ofNullable(input.getValidators())
                          .filter(validators -> validators.size() == 1)
                          .map(validators -> switch (validators.get(0).getValidation()) {
                            case SSN -> new ArrayList<>(List.of("1234-56-789"));
                            case ZIPCODE -> List.of("12345");
                            case STATE -> List.of("MN");
                            default -> List.of("some-value");
                          })
                          .orElse(List.of("some-value"));
                    };
                    return new InputData(value);
                  }
              ));
          return Map.entry(pageConfiguration.getName(), new PageData(inputDataMap));
        })
        .distinct()
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    PagesData pagesData = new PagesData();
    pagesData.putAll(data);
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(pagesData);
    Application application = Application.builder()
        .id(applicationId)
        .completedAt(ZonedDateTime.now(clock))
        .applicationData(applicationData)
        .county(County.Other)
        .timeToComplete(Duration.ofSeconds(534))
        .build();
    applicationRepository.save(application);
    ApplicationFile applicationFile = xmlGenerator
        .generate(applicationId, Document.CAF, CASEWORKER);

    Node document = byteArrayToDocument(applicationFile.getFileBytes());

    Validator schemaValidator = SchemaFactory.newDefaultInstance()
        .newSchema(onlineApplicationSchema.getFile())
        .newValidator();
    assertThatCode(() -> schemaValidator.validate(new DOMSource(document)))
        .doesNotThrowAnyException();   
  }

  @Test
  void shouldMapPersonalInfoForRegularFlow() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPersonalInfo()
        .withPageData("additionalInfo","caseNumber","123456").build();
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now(clock))
        .applicationData(applicationData)
        .county(County.Hennepin)
        .timeToComplete(Duration.ofSeconds(534))
        .build();
    applicationRepository.save(application);

    ApplicationFile applicationFile = xmlGenerator
        .generate("someId", Document.XML, CASEWORKER);

    String xmlFile = new String(applicationFile.getFileBytes());
    assertThat(xmlFile).containsIgnoringWhitespaces("""
        <ns4:Person>
                    <ns4:FirstName>Jane</ns4:FirstName>
                    <ns4:LastName>Doe</ns4:LastName>
                </ns4:Person>""");
    assertThat(xmlFile).containsIgnoringWhitespaces("""
        <ns4:PersonalInfo>
                        <ns4:OtherName>
                            <ns4:FirstName></ns4:FirstName>
                        </ns4:OtherName>
                        <ns4:FirstName>Jane</ns4:FirstName>
                        <ns4:LastName>Doe</ns4:LastName>
                        <ns4:Gender>Female</ns4:Gender>
                        <ns4:MaritalStatus>Never Married</ns4:MaritalStatus>
                        <ns4:DOB>10/04/2020</ns4:DOB>
                        <ns4:Relationship>Self</ns4:Relationship>
        """);
    assertThat(xmlFile).containsIgnoringWhitespaces("""
        <ns4:CaseNumber>123456</ns4:CaseNumber>
        """);
  }

  @Test
  void shouldMapPersonalInfoForLaterDocs() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("matchInfo", "firstName", "Judy")
        .withPageData("matchInfo", "lastName", "Garland")
        .withPageData("matchInfo", "dateOfBirth", List.of("06", "10", "1922"))
        .withPageData("matchInfo", "caseNumber", "123456")
        .build();
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now(clock))
        .applicationData(applicationData)
        .county(County.Hennepin)
        .timeToComplete(Duration.ofSeconds(534))
        .build();
    applicationRepository.save(application);

    ApplicationFile applicationFile = xmlGenerator
        .generate("someId", Document.XML, CASEWORKER);

    String xmlFile = new String(applicationFile.getFileBytes());
    assertThat(xmlFile).containsIgnoringWhitespaces("""
        <ns4:Person>
                    <ns4:FirstName>Judy</ns4:FirstName>
                    <ns4:LastName>Garland</ns4:LastName>
                </ns4:Person>""");
    assertThat(xmlFile).containsIgnoringWhitespaces("""
        <ns4:PersonalInfo>
                        <ns4:OtherName>
                        </ns4:OtherName>
                        <ns4:FirstName>Judy</ns4:FirstName>
                        <ns4:LastName>Garland</ns4:LastName>
                        <ns4:DOB>06/10/1922</ns4:DOB>
                        <ns4:Relationship>Self</ns4:Relationship>
        """);
    assertThat(xmlFile).containsIgnoringWhitespaces("""
        <ns4:CaseNumber>123456</ns4:CaseNumber>
        """);
  }

  @Test
  void shouldMapPersonalInfoForLaterDocsWithEmptyDOB() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("matchInfo", "firstName", "Judy")
        .withPageData("matchInfo", "lastName", "Garland")
        .withPageData("matchInfo", "dateOfBirth", List.of("", "", ""))
        .build();
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now(clock))
        .applicationData(applicationData)
        .county(County.Hennepin)
        .timeToComplete(Duration.ofSeconds(534))
        .build();
    applicationRepository.save(application);

    ApplicationFile applicationFile = xmlGenerator
        .generate("someId", Document.XML, CASEWORKER);

    String xmlFile = new String(applicationFile.getFileBytes());
    assertThat(xmlFile).containsIgnoringWhitespaces("""
                        <ns4:DOB>//</ns4:DOB>
                        <ns4:Relationship>Self</ns4:Relationship>
        """);
  }

  private Node byteArrayToDocument(byte[] bytes)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    org.w3c.dom.Document document = documentBuilder.parse(new ByteArrayInputStream(bytes));
    return document.getFirstChild().getFirstChild();
  }
}
