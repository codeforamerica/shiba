package org.codeforamerica.shiba.inputconditions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class Condition {
    String pageName;
    String input;
    String value;
    @JsonIgnore
    ValueMatcher matcher = ValueMatcher.CONTAINS;

    String subworkflow;
    Integer iteration;

    public boolean appliesForAllIterations() {
        return getSubworkflow() != null && getIteration() == null;
    }

    public boolean matches(PageData pageData, PagesData pagesData) {
        if (this.getPageName() != null) {
            return this.satisfies(pagesData.getPage(this.getPageName()));
        } else {
            return this.satisfies(pageData);
        }
    }

    public boolean satisfies(PageData pageData) {
        return this.matcher.matches(pageData.get(this.getInput()).getValue(), value);
    }

}
