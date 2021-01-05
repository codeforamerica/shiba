package org.codeforamerica.shiba.output.xml;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class XmlGenerator implements FileGenerator {
    private final Resource xmlConfiguration;
    private final Map<String, String> config;
    private final Map<String, String> enumMappings;
    private final ApplicationRepository applicationRepository;
    private final ApplicationInputsMappers mappers;
    private final FileNameGenerator fileNameGenerator;
    private final BinaryOperator<String> UNUSED_IN_SEQUENTIAL_STREAM = (s1, s2) -> "";
    private final Function<String, String> tokenFormatter = (token) -> Pattern.quote(String.format("{{%s}}", token));

    public XmlGenerator(
            @Value("classpath:XmlConfiguration.xml") Resource xmlConfiguration,
            Map<String, String> xmlConfigMap,
            Map<String, String> xmlEnum,
            ApplicationRepository applicationRepository,
            ApplicationInputsMappers mappers,
            FileNameGenerator fileNameGenerator) {
        this.xmlConfiguration = xmlConfiguration;
        this.config = xmlConfigMap;
        this.enumMappings = xmlEnum;
        this.applicationRepository = applicationRepository;
        this.mappers = mappers;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    public ApplicationFile generate(String applicationId, Document document, Recipient recipient) {
        Application application = applicationRepository.find(applicationId);
        List<ApplicationInput> applicationInputs = mappers.map(application, recipient);

        try {
            String contentsAfterReplacement = applicationInputs.stream()
                    .filter(input -> !input.getValue().isEmpty())
                    .flatMap(input -> {
                        String defaultXmlConfigKey = String.join(".", input.getGroupName(), input.getName());
                        return switch (input.getType()) {
                            case DATE_VALUE -> Stream.of(new AbstractMap.SimpleEntry<>(
                                    getXmlToken(input, config.get(defaultXmlConfigKey)),
                                    String.join("/", input.getValue())));
                            case ENUMERATED_SINGLE_VALUE -> Optional.ofNullable(enumMappings.get(input.getValue(0)))
                                    .map(mappedValue -> new AbstractMap.SimpleEntry<>(
                                            getXmlToken(input, config.get(defaultXmlConfigKey)),
                                            mappedValue))
                                    .stream();
                            case ENUMERATED_MULTI_VALUE -> input.getValue().stream()
                                        .map(value -> new AbstractMap.SimpleEntry<>(
                                                getXmlToken(input, config.get(String.join(".", defaultXmlConfigKey, value))),
                                                enumMappings.get(value)))
                                        .filter(entry -> entry.getValue() != null);
                            default -> Stream.of(new AbstractMap.SimpleEntry<>(
                                    getXmlToken(input, config.get(defaultXmlConfigKey)),
                                    input.getValue(0)));
                        };
                    })
                    .filter(xmlTokenToInputValueEntry -> xmlTokenToInputValueEntry.getKey() != null)
                    .reduce(
                            new String(xmlConfiguration.getInputStream().readAllBytes()),
                            (partiallyReplacedContent, tokenToValueEntry) ->
                                    partiallyReplacedContent.replaceAll(tokenFormatter.apply(tokenToValueEntry.getKey()), tokenToValueEntry.getValue()),
                            UNUSED_IN_SEQUENTIAL_STREAM
                    );
            String finishedXML = contentsAfterReplacement.replaceAll("\\s*<\\w+:\\w+>\\{\\{\\w+}}</\\w+:\\w+>", "");
            return new ApplicationFile(
                    finishedXML.getBytes(),
                    String.format("%s.xml", fileNameGenerator.generateFileName(application, document)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getXmlToken(ApplicationInput input, String xmlToken) {
        return input.getIteration() != null ? xmlToken + "_" + input.getIteration() : xmlToken;
    }

}
