package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Form {
    public List<FormInput> inputs = List.of();
    private String path;
    private String pageTitle;
    private String headerKey;
    private String headerHelpMessageKey;
    private String nextPage;
    private String previousPage;
    private String dataSource;

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
