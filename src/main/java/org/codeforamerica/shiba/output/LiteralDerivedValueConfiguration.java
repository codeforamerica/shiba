package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.pages.ApplicationData;

import java.util.List;

@Data
public class LiteralDerivedValueConfiguration implements DerivedValueConfiguration {
    private String literal;

    @Override
    public List<String> resolve(ApplicationData data) {
        return List.of(literal);
    }
}
