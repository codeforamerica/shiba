package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Form {
    public List<FormInput> inputs;

    List<FormInput> getFlattenedInputs() {
        //noinspection SwitchStatementWithTooFewBranches
        return this.inputs.stream()
                .flatMap(formInput -> switch (formInput.type) {
                    case INPUT_WITH_FOLLOW_UP -> Stream.concat(
                            Stream.of(formInput.inputWithFollowUps),
                            formInput.inputWithFollowUps.followUps.stream());
                    default -> Stream.of(formInput);
                })
                .collect(Collectors.toList());
    }

    Boolean isValid() {
        return this.getInputs().stream().allMatch(FormInput::getValid);
    }
}
