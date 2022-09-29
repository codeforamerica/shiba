package org.codeforamerica.shiba.inputconditions;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.output.LogicalOperator;
import org.codeforamerica.shiba.pages.data.PageData;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@With
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Condition implements Serializable {

  @Serial
  private static final long serialVersionUID = -7300484979833484734L;
  String pageName;
  String input;
  String value;
  @JsonIgnore
  ValueMatcher matcher = ValueMatcher.CONTAINS;
  @JsonIgnore
  private List<Condition> conditions;
  @JsonIgnore
  private LogicalOperator logicalOperator = LogicalOperator.AND;

  public Condition(List<Condition> conditions, LogicalOperator logicalOperator) {
    this.conditions = conditions;
    this.logicalOperator = logicalOperator;
  }

  public Condition(String pageName, String input, String value, ValueMatcher matcher) {
    this.pageName = pageName;
    this.input = input;
    this.value = value;
    this.matcher = matcher;
  }

  public boolean matches(PageData pageData, Map<String, PageData> pagesData) {
    if (pageName != null) {
      return satisfies(pagesData.get(pageName));
    } else {
      return satisfies(pageData);
    }
  }

  public boolean satisfies(PageData pageData) {
    return pageData != null && !pageData.isEmpty() && matcher
        .matches(pageData.get(input).getValue(), value);
  }

  @SuppressWarnings("unused")
  public void setConditions(List<Condition> conditions) {
    assertCompositeCondition();
    this.conditions = conditions;
  }

  @SuppressWarnings("unused")
  public void setLogicalOperator(LogicalOperator logicalOperator) {
    assertCompositeCondition();
    this.logicalOperator = logicalOperator;
  }

  public void setPageName(String pageName) {
    assertNotCompositeCondition();
    this.pageName = pageName;
  }

  public void setInput(String input) {
    assertNotCompositeCondition();
    this.input = input;
  }

  public void setValue(String value) {
    assertNotCompositeCondition();
    this.value = value;
  }

  private void assertCompositeCondition() {
    if (pageName != null || input != null) {
      throw new IllegalStateException("Cannot set composite condition fields");
    }
  }

  private void assertNotCompositeCondition() {
    if (conditions != null) {
      throw new IllegalStateException("Cannot set noncomposite condition fields");
    }
  }
}
