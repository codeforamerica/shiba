package org.codeforamerica.shiba;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PdfFieldMapper {
    public static final String APPLICANT_LAST_NAME = "APPLICANT_LAST_NAME";
    public static final String APPLICANT_FIRST_NAME = "APPLICANT_FIRST_NAME";
    public static final String NEED_INTERPRETER = "NEED_INTERPRETER";
    public static final String FOOD = "FOOD";
    public static final String CASH = "CASH";
    public static final String EMERGENCY = "EMERGENCY";
    public static final String MARITAL_STATUS = "MARITAL_STATUS";
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
