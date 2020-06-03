package org.codeforamerica.shiba;

import org.codeforamerica.shiba.xml.XmlGenerator;
import org.codeforamerica.shiba.xml.FileGenerator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.FormInputType.RADIO;
import static org.hamcrest.Matchers.hasXPath;

class XmlGeneratorTest {
    @Test
    void shouldExcludeElementsWhenExpressionEvaluatesToNull() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";

        FormInput formInput = new FormInput();
        formInput.value = null;
        String formInputName = "some-form-input";
        formInput.name = formInputName;
        String screenName = "some-screen";
        Map<String, List<FormInput>> formInputsMap = Map.of(screenName, List.of(formInput));

        Map<String, String> xmlConfigMap = Map.of(
                screenName + "." + formInputName,
                "SOME_TOKEN"
        );
        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());

        ApplicationFile applicationFile = subject.generate(formInputsMap);

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        Document document = byteArrayToDocument(applicationFile.getFileBytes());
        MatcherAssert.assertThat(document,
                hasXPath("count(/ns:Root/ns:Child)",
                        namespaceContext,
                        Matchers.equalTo("0")));
    }

    @Test
    void shouldIgnoreFormInputsThatDoNotHaveAnXmlMapping() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";

        FormInput formInput = new FormInput();
        formInput.value = List.of("some-value");
        formInput.name = "some-form-input";
        String screenName = "some-screen";
        Map<String, List<FormInput>> formInputsMap = Map.of(screenName, List.of(formInput));

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), Map.of(), Map.of());

        ApplicationFile applicationFile = subject.generate(formInputsMap);

        String actualXml = new String(applicationFile.getFileBytes());
        assertThat(actualXml).isEqualTo(xml);
    }

    @Test
    void shouldPopulateNodeValueFromSourceObjectString() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN1</ns:Child>\n" +
                "    <ns:Child>SOME_TOKEN2</ns:Child>\n" +
                "</ns:Root>";

        String value1 = "some-string-value";
        FormInput formInput1 = new FormInput();
        formInput1.type = FormInputType.NUMBER;
        formInput1.value = List.of(value1);
        String formInputName1 = "some-form-input";
        formInput1.name = formInputName1;

        String value2 = "some-other-string-value";
        FormInput formInput2 = new FormInput();
        formInput2.type = FormInputType.TEXT;
        formInput2.value = List.of(value2);
        String formInputName2 = "some-other-form-input";
        formInput2.name = formInputName2;

        String screenName = "some-screen";
        Map<String, List<FormInput>> formInputsMap = Map.of(screenName, List.of(formInput1, formInput2));

        Map<String, String> xmlConfigMap = Map.of(
                screenName + "." + formInputName1, "SOME_TOKEN1",
                screenName + "." + formInputName2, "SOME_TOKEN2"
        );
        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());

        ApplicationFile applicationFile = subject.generate(formInputsMap);

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
    void shouldPopulateNodeValueFromSourceObjectEnum() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";

        FormInput formInput = new FormInput();
        String formInputValue = "some-value";
        formInput.value = List.of(formInputValue);
        String formInputName = "some-form-input";
        formInput.name = formInputName;
        formInput.type = RADIO;
        String screenName = "some-screen";
        Map<String, List<FormInput>> formInputsMap = Map.of(screenName, List.of(formInput));

        Map<String, String> xmlConfigMap = Map.of(
                screenName + "." + formInputName,
                "SOME_TOKEN"
        );

        String xmlEnumName = "SOME_VALUE";
        Map<String, String> xmlEnum = Map.of(formInputValue, xmlEnumName);

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(formInputsMap);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo(xmlEnumName)));
    }

    @Test
    void shouldPopulateNodeValueFromSourceObjectEnum_whenEnumMappingDoesNotExist() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";

        FormInput formInput = new FormInput();
        String formInputValue = "some-value";
        formInput.value = List.of(formInputValue);
        String formInputName = "some-form-input";
        formInput.name = formInputName;
        formInput.type = RADIO;
        String screenName = "some-screen";
        Map<String, List<FormInput>> formInputsMap = Map.of(screenName, List.of(formInput));

        Map<String, String> xmlConfigMap = Map.of(
                screenName + "." + formInputName,
                "SOME_TOKEN"
        );

        Map<String, String> xmlEnum = Map.of();

        XmlGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        ApplicationFile applicationFile = subject.generate(formInputsMap);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document,
                hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo(formInputValue)));
    }

    @Test
    void shouldPopulateNodeValueFromSourceObjectDate() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";

        FormInput formInput = new FormInput();
        formInput.type = FormInputType.DATE;
        formInput.value = List.of("02", "20", "1999");
        String formInputName = "some-form-input";
        formInput.name = formInputName;
        String screenName = "some-screen";
        Map<String, List<FormInput>> formInputsMap = Map.of(screenName, List.of(formInput));

        Map<String, String> xmlConfigMap = Map.of(
                screenName + "." + formInputName,
                "SOME_TOKEN"
        );
        FileGenerator subject = new XmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());
        ApplicationFile applicationFile = subject.generate(formInputsMap);
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