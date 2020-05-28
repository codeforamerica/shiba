package org.codeforamerica.shiba;

import org.codeforamerica.shiba.xml.XmlConfigXmlGenerator;
import org.codeforamerica.shiba.xml.XmlGenerator;
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
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.hasXPath;

class XmlConfigXmlGeneratorTest {
    static class XmlSourceObject {
        public String stringValue;
        public XmlEnum enumValue;
        public boolean booleanValue;
        public LocalDate localDateValue;
    }

    enum XmlEnum {MEMBER1}

    @Test
    void shouldExcludeElementsWhenExpressionEvaluatesToNull() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";
        Map<String, String> xmlConfigMap = Map.of(
                "null",
                "SOME_TOKEN"
        );
        XmlGenerator subject = new XmlConfigXmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());
        ApplicationFile applicationFile = subject.generate(new XmlSourceObject());

        Document document = byteArrayToDocument(applicationFile.getFileBytes());
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document,
                hasXPath("count(/ns:Root/ns:Child)",
                        namespaceContext,
                        Matchers.equalTo("0")));
    }

    @Test
    void shouldPopulateNodeValueFromSourceObjectString() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";

        String value = "some-string-value";
        XmlSourceObject xmlSourceObject = new XmlSourceObject();
        xmlSourceObject.stringValue = value;

        Map<String, String> xmlConfigMap = Map.of(
                "stringValue",
                "SOME_TOKEN"
        );
        XmlConfigXmlGenerator subject = new XmlConfigXmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());

        ApplicationFile applicationFile = subject.generate(xmlSourceObject);

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        Document document = byteArrayToDocument(applicationFile.getFileBytes());
        MatcherAssert.assertThat(document,
                hasXPath("/ns:Root/ns:Child/text()",
                        namespaceContext,
                        Matchers.equalTo(value)));
    }

    @Test
    void shouldPopulateNodeValueFromSourceObjectEnum() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";
        Map<String, String> xmlConfigMap = Map.of(
                "enumValue",
                "SOME_TOKEN"
        );
        Map<String, String> xmlEnum = Map.of(XmlEnum.MEMBER1.name(), "MEMBER1_FOR_XML");
        XmlGenerator subject = new XmlConfigXmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, xmlEnum);
        XmlSourceObject sourceObject = new XmlSourceObject();
        sourceObject.enumValue = XmlEnum.MEMBER1;
        ApplicationFile applicationFile = subject.generate(sourceObject);
        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo("MEMBER1_FOR_XML")));
    }

    @Test
    void shouldPopulateNodeValueFromSourceObjectBoolean() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";
        Map<String, String> xmlConfigMap = Map.of(
                "booleanValue",
                "SOME_TOKEN"
        );

        XmlSourceObject sourceObject = new XmlSourceObject();
        sourceObject.booleanValue = false;
        XmlGenerator subject = new XmlConfigXmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());
        ApplicationFile applicationFile = subject.generate(sourceObject);
        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        namespaceContext.setBindings(Map.of("ns", "some-url"));
        MatcherAssert.assertThat(document, hasXPath("/ns:Root/ns:Child/text()", namespaceContext, Matchers.equalTo("false")));
    }

    @Test
    void shouldPopulateNodeValueFromSourceObjectDate() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<ns:Root xmlns:ns='some-url'>\n" +
                "    <ns:Child>SOME_TOKEN</ns:Child>\n" +
                "</ns:Root>";
        Map<String, String> xmlConfigMap = Map.of(
                "localDateValue",
                "SOME_TOKEN"
        );

        XmlSourceObject sourceObject = new XmlSourceObject();
        sourceObject.localDateValue = LocalDate.of(1999, 2, 20);
        XmlGenerator subject = new XmlConfigXmlGenerator(new ByteArrayResource(xml.getBytes()), xmlConfigMap, Map.of());
        ApplicationFile applicationFile = subject.generate(sourceObject);
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