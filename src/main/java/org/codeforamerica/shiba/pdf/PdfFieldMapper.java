package org.codeforamerica.shiba.pdf;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PdfFieldMapper {
    private final Map<String, String> pdfFieldMap;
    private final Set<Enum<?>> enumExclusions;

    public PdfFieldMapper(
            Map<String, String> pdfFieldMap,
            Set<Enum<?>> enumExclusions
    ) {
        this.pdfFieldMap = pdfFieldMap;
        this.enumExclusions = enumExclusions;
    }

    public List<PdfField> map(Object object) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext(object);
        return pdfFieldMap.entrySet().stream()
                .filter(entry -> parser.parseExpression(entry.getKey()).getValue(context) != null)
                .flatMap(entry -> {
                    Object value = parser.parseExpression(entry.getKey()).getValue(context);
                    if (value instanceof String) {
                        return Stream.of(new SimplePdfField(entry.getValue(), (String) value));
                    } else if (value instanceof Boolean) {
                        return Stream.of(new TogglePdfField(entry.getValue(), (Boolean) value));
                    } else if (value instanceof List) {
                        //noinspection rawtypes
                        return Stream.of(new BinaryPdfField(entry.getValue(), !((List) value).isEmpty()));
                    } else if (value instanceof Enum) {
                        if (enumExclusions.contains(value)) {
                            return Stream.empty();
                        } else {
                            return Stream.of(new SimplePdfField(entry.getValue(), value.toString()));
                        }
                    } else if (value instanceof LocalDate) {
                        return Stream.of(new DatePdfField(entry.getValue(), (LocalDate) value));
                    } else {
                        //noinspection ConstantConditions
                        throw new IllegalArgumentException(String.format(
                                "The SpEL expression given by '%s' on %s yielded an object of type %s " +
                                        "that does not have a field mapping.",
                                entry.getKey(), object, value.getClass()));
                    }
                }).collect(Collectors.toList());
    }
}
