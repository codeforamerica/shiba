package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.FormInputType;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class OneToOneDocumentFieldPreparerTest {

  ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
  OneToOneDocumentFieldPreparer preparer = new OneToOneDocumentFieldPreparer(
      applicationConfiguration, Map.of());

  @Test
  void shouldProduceAnApplicationInputForAFormInput() {
    FormInput input1 = new FormInput();
    String input1Name = "input 1";
    input1.setName(input1Name);
    input1.setType(FormInputType.TEXT);

    PageConfiguration page = new PageConfiguration();
    page.setInputs(List.of(input1));
    String pageName = "screen1";
    page.setName(pageName);
    applicationConfiguration.setPageDefinitions(List.of(page));

    List<String> input1Value = List.of("input1Value");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData(pageName, input1Name, input1Value).build();

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(County.Other)
        .timeToComplete(null)
        .build();
    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, null, Recipient.CLIENT, null);

    assertThat(documentFields).contains(
        new DocumentField(pageName, input1Name, input1Value, DocumentFieldType.SINGLE_VALUE)
    );
  }

  @Test
  void shouldProduceAnApplicationInput_withMaskedValueForAFormInputWithPersonalData_whenRecipientIsClient() {
    String input1Name = "input 1";
    String maskedValue = "not-for-your-eyes";

    OneToOneDocumentFieldPreparer preparer =
        new OneToOneDocumentFieldPreparer(applicationConfiguration,
            Map.of(input1Name, maskedValue));

    FormInput input1 = new FormInput();
    input1.setName(input1Name);
    input1.setType(FormInputType.TEXT);

    PageConfiguration page = new PageConfiguration();
    page.setInputs(List.of(input1));
    String pageName = "screen1";
    page.setName(pageName);
    applicationConfiguration.setPageDefinitions(List.of(page));

    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData(pageName, input1Name, "input1Value").build();

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(County.Other)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, null, Recipient.CLIENT, null);

    assertThat(documentFields)
        .contains(new DocumentField(pageName, input1Name, List.of(maskedValue),
            DocumentFieldType.SINGLE_VALUE));
  }

  @Test
  void shouldNotProduceAnApplicationInput_withMaskedValueForAFormInputWithPersonalData_whenRecipientIsClient_butValueIsBlank() {
    String input1Name = "input 1";
    String maskedValue = "not-for-your-eyes";

    OneToOneDocumentFieldPreparer preparer =
        new OneToOneDocumentFieldPreparer(applicationConfiguration,
            Map.of(input1Name, maskedValue));

    FormInput input1 = new FormInput();
    input1.setName(input1Name);
    input1.setType(FormInputType.TEXT);

    PageConfiguration page = new PageConfiguration();
    page.setInputs(List.of(input1));
    String pageName = "screen1";
    page.setName(pageName);
    applicationConfiguration.setPageDefinitions(List.of(page));

    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData(pageName, input1Name, "").build();

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(County.Other)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, null, Recipient.CLIENT, null);

    assertThat(documentFields)
        .contains(new DocumentField(pageName, input1Name, List.of(""),
            DocumentFieldType.SINGLE_VALUE));
  }

  @Test
  void shouldProduceAnApplicationInput_withUnmaskedValueForAFormInputWithPersonalData_whenRecipientIsCaseworker() {
    String input1Name = "input 1";

    OneToOneDocumentFieldPreparer preparer =
        new OneToOneDocumentFieldPreparer(applicationConfiguration,
            Map.of(input1Name, "not-for-your-eyes"));

    FormInput input1 = new FormInput();
    input1.setName(input1Name);
    input1.setType(FormInputType.TEXT);

    PageConfiguration page = new PageConfiguration();
    page.setInputs(List.of(input1));
    String pageName = "screen1";
    page.setName(pageName);
    applicationConfiguration.setPageDefinitions(List.of(page));

    List<String> input1Value = List.of("input1Value");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData(pageName, input1Name, input1Value).build();

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(County.Other)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, null, Recipient.CASEWORKER, null);

    assertThat(documentFields).contains(
        new DocumentField(pageName, input1Name, input1Value, DocumentFieldType.SINGLE_VALUE)
    );
  }

  @Test
  void shouldIncludeApplicationInputsForFollowups() {
    FormInput input2 = new FormInput();
    String input2Name = "input 2";
    input2.setName(input2Name);
    input2.setType(FormInputType.TEXT);

    FormInput input3 = new FormInput();
    String input3Name = "input 3";
    input3.setName(input3Name);
    input3.setType(FormInputType.TEXT);

    input2.setFollowUps(List.of(input3));

    PageConfiguration page = new PageConfiguration();
    page.setInputs(List.of(input2));
    String pageName = "screen1";
    page.setName(pageName);
    applicationConfiguration.setPageDefinitions(List.of(page));

    List<String> input2Value = List.of("input2Value");
    List<String> input3Value = List.of("input3Value");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData(pageName, input2Name, input2Value)
        .withPageData(pageName, input3Name, input3Value)
        .build();

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(County.Other)
        .timeToComplete(null)
        .build();
    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, null, Recipient.CLIENT, null);

    assertThat(documentFields).contains(
        new DocumentField(pageName, input2Name, input2Value, DocumentFieldType.SINGLE_VALUE),
        new DocumentField(pageName, input3Name, input3Value, DocumentFieldType.SINGLE_VALUE)
    );
  }

}
