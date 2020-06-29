package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codeforamerica.shiba.FormData.getFormDataFrom;

@Data
public class PageConfiguration {
    public List<FormInput> inputs = List.of();
    private String pageTitle;
    private String headerKey;
    private String headerHelpMessageKey;
    private List<String> nextPage;
    private String previousPage;
    private PageDatasource datasource;
    private Condition skipCondition;
    private boolean startTimer = false;

    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return this.headerKey != null;
    }

    public List<FormInput> getFlattenedInputs() {
        return this.inputs.stream()
                .flatMap(formInput -> Stream.concat(Stream.of(formInput), formInput.followUps.stream()))
                .collect(Collectors.toList());
    }

    boolean isStaticPage() {
        return this.inputs.isEmpty();
    }

    boolean shouldSkip(PagesData pagesData) {
        if (this.datasource == null || this.skipCondition == null) {
            return false;
        }
        return this.skipCondition.appliesTo(getFormDataFrom(datasource, pagesData));
    }

    String getAdjacentPageName(boolean isBackwards) {
        return getAdjacentPageName(isBackwards, 0);
    }

    public String getAdjacentPageName(boolean isBackwards, Integer option) {
        if (isBackwards) {
            return previousPage;
        }
        return nextPage.get(option);
    }
}
