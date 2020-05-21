package org.codeforamerica.shiba;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PdfFieldMapper {
    private final Map<String, String> pdfFieldMap;

    public PdfFieldMapper(Map<String, String> pdfFieldMap) {
        this.pdfFieldMap = pdfFieldMap;
    }

    public List<PDFField> map(Object object) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext(object);
        return pdfFieldMap.entrySet().stream()
                .filter(entry -> parser.parseExpression(entry.getKey()).getValue(context) != null)
                .map(entry -> {
                    Object value = parser.parseExpression(entry.getKey()).getValue(context);
                    if (value instanceof String) {
                        return new SimplePDFField(entry.getValue(), (String) value);
                    } else if (value instanceof Boolean) {
                        return new TogglePDFField(entry.getValue(), (Boolean) value);
                    } else if (value instanceof List) {
                        return new BinaryPDFField(entry.getValue());
                    } else if (value instanceof Enum) {
                        return new SimplePDFField(entry.getValue(), value.toString());
                    } else if (value instanceof LocalDate) {
                        return new DatePDFField(entry.getValue(), (LocalDate) value);
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
