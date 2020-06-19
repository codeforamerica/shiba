package org.codeforamerica.shiba;

import org.codeforamerica.shiba.xml.FileGenerator;
import org.jetbrains.annotations.NotNull;
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
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class XmlGeneratorIntegrationTest {
    @Autowired
    private FileGenerator xmlGenerator;

    @Autowired
    private OneToOneApplicationInputsMapper oneToOneApplicationInputsMapper;

    @Value("classpath:OnlineApplication.xsd")
    private Resource onlineApplicationSchema;

    @Autowired
    private PagesConfiguration pagesConfiguration;

    @Test
    void shouldProduceAValidDocument() throws IOException, SAXException, ParserConfigurationException {
        Map<String, FormData> data = pagesConfiguration.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> FormData.initialize(entry.getValue(), FormData.literalInputDataCreator())));
        PagesData pagesData = new PagesData();
        pagesData.setData(data);
        List<ApplicationInput> applicationInputsWithoutData = oneToOneApplicationInputsMapper.map(pagesData);
        List<ApplicationInput> applicationInputs = applicationInputsWithoutData.stream()
                .map(input -> {
                    FormInput formInput = pagesConfiguration.get(input.getGroupName()).getFlattenedInputs().stream()
                            .filter(screensInput -> screensInput.getName().equals(input.getName()))
                            .findFirst()
                            .orElseThrow();
                    @NotNull List<String> value = switch (input.getType()) {
                        case ENUMERATED_SINGLE_VALUE -> List.of(formInput.getOptions().get(new Random().nextInt(formInput.getOptions().size())).value);
                        case ENUMERATED_MULTI_VALUE -> formInput.getOptions().subList(0, new Random().nextInt(formInput.getOptions().size()) + 1).stream()
                                .map(Option::getValue)
                                .collect(Collectors.toList());
                        case DATE_VALUE -> List.of(LocalDate.ofEpochDay(0).plusDays(new Random().nextInt()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")).split("/"));
                        default -> switch (formInput.getValidator().getValidation()) {
                            case SSN -> List.of("123456789");
                            case ZIPCODE -> List.of("12345");
                            default -> List.of("some-value");
                        };
                    };
                    return new ApplicationInput(input.getGroupName(), value, input.getName(), input.getType());
                })
                .collect(Collectors.toList());
        ApplicationFile applicationFile = xmlGenerator.generate(applicationInputs);

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
