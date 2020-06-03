package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.FormInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;

@Component
public class XmlGenerator implements FileGenerator {
    private final Resource xmlConfiguration;
    private final Map<String, String> config;
    private final Map<String, String> enumMappings;
    private final BinaryOperator<String> UNUSED_IN_SEQUENTIAL_STREAM = (s1, s2) -> "";

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
    public ApplicationFile generate(Map<String, List<FormInput>> formInputsMap) {
        try {
            String contentsAfterReplacement = formInputsMap.entrySet().stream()
                    .flatMap(screenToFormInputsEntry ->
                            screenToFormInputsEntry.getValue().stream()
                                    .map(input -> new AbstractMap.SimpleEntry<>(screenToFormInputsEntry.getKey(), input))
                    )
                    .filter(screenToInputEntry -> config.get(getScopedName(screenToInputEntry)) != null)
                    .map(screenToInputEntry -> {
                        FormInput input = screenToInputEntry.getValue();
                        String value = Optional.ofNullable(input.getValue())
                                .map(list -> switch (input.getType()) {
                                    case DATE -> String.join("/", list);
                                    case RADIO -> enumMappings.getOrDefault(input.getValue().get(0), input.getValue().get(0));
                                    default -> list.get(0);
                                })
                                .orElse("");
                        return new AbstractMap.SimpleEntry<>(
                                config.get(getScopedName(screenToInputEntry)),
                                value
                        );
                    })
                    .reduce(
                            new String(xmlConfiguration.getInputStream().readAllBytes()),
                            (partiallyReplacedContent, tokenToValueEntry) ->
                                    partiallyReplacedContent.replace(tokenToValueEntry.getKey(), tokenToValueEntry.getValue()),
                            UNUSED_IN_SEQUENTIAL_STREAM
                    );
            String finishedXML = contentsAfterReplacement.replaceAll("\\s*<\\w+:\\w+></\\w+:\\w+>", "");
            return new ApplicationFile(finishedXML.getBytes(), "ApplyMN.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getScopedName(Map.Entry<String, FormInput> screenToInputEntry) {
        return screenToInputEntry.getKey() + "." + screenToInputEntry.getValue().getName();
    }
}
