package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Form {
    public List<FormInput> inputs;
    public String dataSource;
    private String pageTitle;
    private String backLink;
    private String headerKey;
    private String postEndpoint;
    private String nextPage;

    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return this.headerKey != null;
    }

    public List<FormInput> getFlattenedInputs() {
        return this.inputs.stream()
                .flatMap(formInput -> Stream.concat(Stream.of(formInput), formInput.followUps.stream()))
                .collect(Collectors.toList());
    }

}
