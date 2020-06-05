package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.ApplicationInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class XmlGenerator implements FileGenerator {
    private final Resource xmlConfiguration;
    private final Map<String, String> config;
    private final Map<String, String> enumMappings;
    private final BinaryOperator<String> UNUSED_IN_SEQUENTIAL_STREAM = (s1, s2) -> "";
    private final Function<String, String> tokenFormatter = (token) -> Pattern.quote(String.format("{{%s}}", token));

    public XmlGenerator(
            @Value("classpath:XmlConfiguration.xml") Resource xmlConfiguration,
            Map<String, String> xmlConfigMap,
            Map<String, String> xmlEnum
    ) {
        this.xmlConfiguration = xmlConfiguration;
        this.config = xmlConfigMap;
        this.enumMappings = xmlEnum;
    }

    @Override
    public ApplicationFile generate(List<ApplicationInput> applicationInputs) {
        try {
            String contentsAfterReplacement = applicationInputs.stream()
                    .filter(input -> !input.getValue().isEmpty())
                    .flatMap(input -> {
                        String xmlToken = config.get(String.join(".", input.getGroupName(), input.getName()));
                        return switch (input.getType()) {
                            case DATE_VALUE -> Stream.of(new AbstractMap.SimpleEntry<>(xmlToken, String.join("/", input.getValue())));
                            case ENUMERATED_SINGLE_VALUE -> Stream.of(new AbstractMap.SimpleEntry<>(xmlToken, enumMappings.getOrDefault(input.getValue().get(0), input.getValue().get(0))));
                            case ENUMERATED_MULTI_VALUE -> input.getValue().stream()
                                    .map(value -> new AbstractMap.SimpleEntry<>(xmlToken, enumMappings.getOrDefault(value, value)));
                            default -> Stream.of(new AbstractMap.SimpleEntry<>(xmlToken, input.getValue().get(0)));
                        };
                    })
                    .filter(xmlTokenToInputValueEntry -> xmlTokenToInputValueEntry.getKey() != null)
                    .reduce(
                            new String(xmlConfiguration.getInputStream().readAllBytes()),
                            (partiallyReplacedContent, tokenToValueEntry) ->
                                    partiallyReplacedContent.replaceFirst(tokenFormatter.apply(tokenToValueEntry.getKey()), tokenToValueEntry.getValue()),
                            UNUSED_IN_SEQUENTIAL_STREAM
                    );
            String finishedXML = contentsAfterReplacement.replaceAll("\\s*<\\w+:\\w+>\\{\\{\\w+}}</\\w+:\\w+>", "");
            return new ApplicationFile(finishedXML.getBytes(), "ApplyMN.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
