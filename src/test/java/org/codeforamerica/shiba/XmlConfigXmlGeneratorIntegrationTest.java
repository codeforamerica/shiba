package org.codeforamerica.shiba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class XmlConfigXmlGeneratorIntegrationTest {
    @Autowired
    private XmlGenerator xmlGenerator;

    @Value("classpath:OnlineApplication.xsd")
    private Resource onlineApplicationSchema;

    @Test
    void shouldProduceAValidDocument() throws IOException, SAXException, ParserConfigurationException {
        BenefitsApplication benefitsApplication = new BenefitsApplication();
        benefitsApplication.setPersonalInfo(PersonalInfo.builder()
                .firstName("someFirstName")
                .maritalStatus(MaritalStatus.DIVORCED)
                .dateOfBirth(LocalDate.of(2019, 2, 1))
                .livedInMNWholeLife(false)
                .build());
        ApplicationFile applicationFile = xmlGenerator.generate(benefitsApplication);

        Document document = byteArrayToDocument(applicationFile.getFileBytes());

        Validator schemaValidator = SchemaFactory.newDefaultInstance()
                .newSchema(onlineApplicationSchema.getFile())
                .newValidator();
        assertThatCode(() -> schemaValidator.validate(new DOMSource(document))).doesNotThrowAnyException();
    }

    private Document byteArrayToDocument(byte[] bytes) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(bytes));
    }
}
