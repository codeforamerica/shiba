package org.codeforamerica.shiba;

import org.codeforamerica.shiba.xml.FileGenerator;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class XmlGeneratorIntegrationTest {
    @Autowired
    private FileGenerator xmlGenerator;

    @Value("classpath:OnlineApplication.xsd")
    private Resource onlineApplicationSchema;

    @Autowired
    private Screens screens;

    @Test
    void shouldProduceAValidDocument() throws IOException, SAXException, ParserConfigurationException {
        screens.values().forEach(value -> value.getInputs().forEach(input -> {
            switch (input.type) {
                case RADIO -> input.setValue(List.of(input.options.get(new Random().nextInt(input.options.size())).value));
                case DATE -> input.setValue(List.of(LocalDate.ofEpochDay(0).plusDays(new Random().nextInt()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")).split("/")));
                default -> input.setValue(List.of("some-value"));
            }
        }));

        ApplicationFile applicationFile = xmlGenerator.generate(screens.unwrapFormWithFlattenedInputs());

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
