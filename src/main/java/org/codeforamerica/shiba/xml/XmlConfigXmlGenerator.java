package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Component
public class XmlConfigXmlGenerator implements XmlGenerator {
    private final Resource xmlConfiguration;
    private final Map<String, String> config;
    private final Map<String, String> enumMappings;

    public XmlConfigXmlGenerator(
            @Value("classpath:XmlConfiguration.xml") Resource xmlConfiguration,
            Map<String, String> xmlConfigMap,
            Map<String, String> xmlEnum
    ) {
        this.xmlConfiguration = xmlConfiguration;
        this.config = xmlConfigMap;
        this.enumMappings = xmlEnum;
    }

    @Override
    public ApplicationFile generate(Object sourceObject) {
        ExpressionParser parser = new SpelExpressionParser();

        try {
            String contents = new String(xmlConfiguration.getInputStream().readAllBytes());
            StandardEvaluationContext context = new StandardEvaluationContext(sourceObject);
            for (Map.Entry<String, String> entry : config.entrySet()) {
                Object value = parser.parseExpression(entry.getKey()).getValue(context);
                String xmlValue;
                if (value == null) {
                    xmlValue = "";
                } else if (value instanceof String) {
                    xmlValue = (String) value;
                } else if (value instanceof LocalDate) {
                    xmlValue = ((LocalDate) value).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                } else if (value instanceof Enum) {
                    xmlValue = Optional.ofNullable(enumMappings.get(((Enum<?>) value).name())).orElseThrow(() -> {
                        throw new RuntimeException(String.format("XML Enum mapping missing for %s", value));
                    });
                } else if (value instanceof Boolean) {
                    xmlValue = value.toString();
                } else {
                    throw new IllegalArgumentException(String.format(
                            "The SpEL expression given by '%s' on %s yielded an object of type %s " +
                                    "that does not have an xml mapping.",
                            entry.getValue(), sourceObject, value.getClass()));
                }
                contents = contents.replace(entry.getValue(), xmlValue);
            }
            contents = contents.replaceAll("\\s*<\\w+:\\w+></\\w+:\\w+>", "");
            return new ApplicationFile(contents.getBytes(), "ApplyMN.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
