package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;

@Data
public class Form {
    public List<FormInput> inputs;

    Boolean isValid() {
        return this.getInputs().stream().allMatch(FormInput::getValid);
    }
}
