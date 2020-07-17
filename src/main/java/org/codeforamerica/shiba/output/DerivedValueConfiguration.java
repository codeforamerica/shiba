package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.pages.ApplicationData;

import java.util.List;

@Data
public class DerivedValueConfiguration {
    private String literal;
    private DerivedValueType type = DerivedValueType.LITERAL;
    private ValueReference reference;

    List<String> resolve(ApplicationData data) {
        return switch (type) {
            case LITERAL -> List.of(literal);
            case REFERENCE -> {
                ValueReference reference = this.reference;
                yield data.getFormData(reference.getPageName()).get(reference.getInputName()).getValue();
            }
        };
    }
}
