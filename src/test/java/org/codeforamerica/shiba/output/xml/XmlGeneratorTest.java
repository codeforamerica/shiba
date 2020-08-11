package org.codeforamerica.shiba.output.xml;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasXPath;

class XmlGeneratorTest {

    @ParameterizedTest
    @EnumSource(value = ApplicationInputType.class)
    void shouldExcludeElementsWhenInputValueIsEmpty(ApplicationInputType applicationInputType) throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(), applicationInputType);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName,
                "SOME_TOKEN"
        );
        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());

        ApplicationFile applicationFile = subject.generate(applicationInputs);

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        Document document = byteArrayToDocument(applicationFile.getFileBytes());
        MatcherAssert.assertThat(document,
                hasXPath("count(/ns:Root/ns:Child)",
                        namespaceContext,
                        Matchers.equalTo("0")));
    }

    @Test
    void shouldRemovePlaceholdersThatHaveNotBeenReplacedWithAValue() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        List<ApplicationInput> applicationInputs = List.of();

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), Map.of(), Map.of());

        ApplicationFile applicationFile = subject.generate(applicationInputs);
        Document document = byteArrayToDocument(applicationFile.getFileBytes());
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document,
                hasXPath("count(/ns:Root/ns:Child)",
                        namespaceContext,
                        Matchers.equalTo("0")));
    }

    @Test
    void shouldPopulateNodeValueFromSingleValueInput() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN1}}</ns:Child>\n" +
                "    <ns:Child>{{SOME_TOKEN2}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String value1 = "some-string-value";
        String formInputName1 = "some-form-input";
        ApplicationInput applicationInput1 = new ApplicationInput(pageName, formInputName1, List.of(value1), ApplicationInputType.SINGLE_VALUE);

        String value2 = "some-other-string-value";
        String formInputName2 = "some-other-form-input";
        ApplicationInput applicationInput2 = new ApplicationInput(pageName, formInputName2, List.of(value2), ApplicationInputType.SINGLE_VALUE);

        List<ApplicationInput> applicationInputs = List.of(applicationInput1, applicationInput2);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName1, "SOME_TOKEN1",
                pageName + "." + formInputName2, "SOME_TOKEN2"
        );
        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());

        ApplicationFile applicationFile = subject.generate(applicationInputs);

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        Document document = byteArrayToDocument(applicationFile.getFileBytes());
        MatcherAssert.assertThat(document,
                hasXPath("/ns:Root/ns:Child[1]/text()",
                        namespaceContext,
                        Matchers.equalTo(value1)));

        MatcherAssert.assertThat(document,
                hasXPath("/ns:Root/ns:Child[2]/text()",
                        namespaceContext,
                        Matchers.equalTo(value2)));
    }

    @Test
    void shouldPopulateNodeValueFromInputWithIterationNumber() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN_0}}</ns:Child>\n" +
                "    <ns:Child>{{SOME_TOKEN_1}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String value1 = "some-string-value";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput1 = new ApplicationInput(pageName, formInputName, List.of(value1), ApplicationInputType.SINGLE_VALUE, 0);

        String value2 = "some-other-string-value";
        ApplicationInput applicationInput2 = new ApplicationInput(pageName, formInputName, List.of(value2), ApplicationInputType.SINGLE_VALUE, 1);

        List<ApplicationInput> applicationInputs = List.of(applicationInput1, applicationInput2);

        Map<String, String> xmlConfigMap = Map.of(pageName + "." + formInputName, "SOME_TOKEN");
        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());

        ApplicationFile applicationFile = subject.generate(applicationInputs);

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        Document document = byteArrayToDocument(applicationFile.getFileBytes());
        MatcherAssert.assertThat(document,
                hasXPath("/ns:Root/ns:Child[1]/text()",
                        namespaceContext,
                        Matchers.equalTo(value1)));

        MatcherAssert.assertThat(document,
                hasXPath("/ns:Root/ns:Child[2]/text()",
                        namespaceContext,
                        Matchers.equalTo(value2)));
    }

    @Test
    void shouldPopulateNodeValueFromEnumeratedMultiValueInput() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputValue = "some-value";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(formInputValue), ApplicationInputType.ENUMERATED_MULTI_VALUE);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName + "." + formInputValue,
                "SOME_TOKEN"
        );

        String xmlEnumName = "SOME_VALUE";
        Map<String, String> xmlEnum = Map.of(formInputValue, xmlEnumName);

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(applicationInputs);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo(xmlEnumName)));
    }

    @Test
    void shouldPopulateNodeValueFromEnumeratedMultiValueInputWithIterationNumber() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN_0}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputValue = "some-value";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(formInputValue), ApplicationInputType.ENUMERATED_MULTI_VALUE, 0);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName + "." + formInputValue,
                "SOME_TOKEN"
        );

        String xmlEnumName = "SOME_VALUE";
        Map<String, String> xmlEnum = Map.of(formInputValue, xmlEnumName);

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(applicationInputs);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo(xmlEnumName)));
    }

    @Test
    void shouldPopulateAllNodeValuesFromEnumeratedMultiValueInput() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputValue = "some-value";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(formInputValue), ApplicationInputType.ENUMERATED_MULTI_VALUE);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName + "." + formInputValue,
                "SOME_TOKEN"
        );

        String xmlEnumName = "SOME_VALUE";
        Map<String, String> xmlEnum = Map.of(formInputValue, xmlEnumName);

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(applicationInputs);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("count(/ns:Root/ns:Child)", namespaceContext, Matchers.equalTo("2")));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child[1]/text()", namespaceContext, Matchers.equalTo("SOME_VALUE")));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child[2]/text()", namespaceContext, Matchers.equalTo("SOME_VALUE")));
    }

    @Test
    void shouldExcludeNodesForEnumeratedMultiValueInputWhereEnumMappingDoesNotExist() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputValue = "some-value";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(formInputValue), ApplicationInputType.ENUMERATED_MULTI_VALUE);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName,
                "SOME_TOKEN"
        );

        Map<String, String> xmlEnum = Map.of();

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(applicationInputs);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document,
                hasXPath("count(/ns:Root/ns:Child)",
                        namespaceContext,
                        Matchers.equalTo("0")));
    }

    @Test
    void shouldPopulateNodeValueFromEnumeratedSingleValueInput() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputValue = "some-value";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(formInputValue), ApplicationInputType.ENUMERATED_SINGLE_VALUE);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName,
                "SOME_TOKEN"
        );

        String xmlEnumName = "SOME_VALUE";
        Map<String, String> xmlEnum = Map.of(formInputValue, xmlEnumName);

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(applicationInputs);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo(xmlEnumName)));
    }

    @Test
    void shouldExcludeNodeForEnumeratedSingleValueInputWhereEnumMappingDoesNotExist() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputValue = "some-value";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(formInputValue), ApplicationInputType.ENUMERATED_SINGLE_VALUE);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName,
                "SOME_TOKEN"
        );

        Map<String, String> xmlEnum = Map.of();

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(applicationInputs);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document,
                hasXPath("count(/ns:Root/ns:Child)",
                        namespaceContext,
                        Matchers.equalTo("0")));
    }

    @Test
    void shouldPopulateNodeValueFromDateValueInput() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>{{SOME_TOKEN}}</ns:Child>\n" +
                "</ns:Root>";

        String pageName = "some-screen";
        String formInputName = "some-form-input";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of("02", "20", "1999"), ApplicationInputType.DATE_VALUE);
        List<ApplicationInput> applicationInputs = List.of(applicationInput);

        Map<String, String> xmlConfigMap = Map.of(
                pageName + "." + formInputName,
                "SOME_TOKEN"
        );
        FileGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());
        ApplicationFile applicationFile = subject.generate(applicationInputs);
        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo("02/20/1999")));
    }

    private Document byteArrayToDocument(byte[] bytes) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(bytes));
    }
}