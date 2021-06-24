package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class PageConfiguration {
    private String name;
    private List<FormInput> inputs = List.of();
    private Value pageTitle;
    private Value headerKey;
    private Value headerHelpMessageKey;
    private Value subtleLinkTextKey;
    private String primaryButtonTextKey = "general.continue";
    private Boolean hasPrimaryButton = true;
    private String contextFragment;
    private boolean isCustomPage = false;

    public List<FormInput> getFlattenedInputs() {
        return this.inputs.stream()
                .flatMap(formInput -> Stream.concat(Stream.of(formInput), formInput.getFollowUps().stream()))
                .collect(Collectors.toList());
    }

    public boolean isStaticPage() {
        return !isCustomPage && this.inputs.isEmpty();
    }

}
