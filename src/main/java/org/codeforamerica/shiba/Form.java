package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Form {
    public List<FormInput> inputs;
    public String dataSource;

    public List<FormInput> getFlattenedInputs() {
        return this.inputs.stream()
                .flatMap(formInput -> Stream.concat(Stream.of(formInput), formInput.followUps.stream()))
                .collect(Collectors.toList());
    }

}
