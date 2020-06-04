package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.ApplicationInput;
import org.codeforamerica.shiba.ApplicationInputType;
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
    public ApplicationFile generate(Map<String, List<ApplicationInput>> formInputsMap) {
        try {
            String contentsAfterReplacement = formInputsMap.entrySet().stream()
                    .flatMap(screenToFormInputsEntry ->
                            screenToFormInputsEntry.getValue().stream()
                                    .map(input -> new AbstractMap.SimpleEntry<>(screenToFormInputsEntry.getKey(), input))
                    )
                    .flatMap(screenToInputEntry -> {
                        ApplicationInput input = screenToInputEntry.getValue();
                        //noinspection SwitchStatementWithTooFewBranches
                        return switch (input.getType()) {
                            case ENUMERATED_MULTI_VALUE -> Stream.ofNullable(input.getValue())
                                    .flatMap(list -> list.stream()
                                            .map(value -> new AbstractMap.SimpleEntry<>(
                                                    String.join(".", screenToInputEntry.getKey(), input.getName()),
                                                    new ApplicationInput(List.of(value), input.getName(), ApplicationInputType.ENUMERATED_SINGLE_VALUE)
                                            ))
                                    );
                            default -> Stream.of(new AbstractMap.SimpleEntry<>(
                                    String.join(".", screenToInputEntry.getKey(), input.getName()),
                                    input
                            ));
                        };
                    })
                    .filter(xmlConfigKeyToInputEntry -> xmlConfigKeyToInputEntry.getValue().getValue() != null)
                    .filter(xmlConfigKeyToInputEntry -> !xmlConfigKeyToInputEntry.getValue().getValue().isEmpty())
                    .map(xmlConfigKeyToInputEntry -> {
                        String xmlToken = config.get(xmlConfigKeyToInputEntry.getKey());
                        ApplicationInput input = xmlConfigKeyToInputEntry.getValue();
                        String value = switch (input.getType()) {
                            case DATE_VALUE -> String.join("/", input.getValue());
                            case ENUMERATED_SINGLE_VALUE -> enumMappings.getOrDefault(input.getValue().get(0), input.getValue().get(0));
                            default -> input.getValue().get(0);
                        };
                        return new AbstractMap.SimpleEntry<>(xmlToken, value);
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
