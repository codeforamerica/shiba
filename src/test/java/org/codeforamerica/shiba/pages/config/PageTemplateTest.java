package org.codeforamerica.shiba.pages.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageTemplateTest {

  private String headerKey;
  private String headerHelpMessageKey;
  private String subtleLinkTextKey;
  private AlertBox alertBox;

  @BeforeEach
  void setup() {
    headerKey = "aHeaderKey";
    headerHelpMessageKey = "aHeaderHelpMessageKey";
    subtleLinkTextKey = "aSubtleLinkTextKey";
  }

  @Test
  void hasHeader() {
    assertThat(makePageTemplate().hasHeader()).isTrue();
    headerKey = "";
    assertThat(makePageTemplate().hasHeader()).isFalse();
    headerKey = null;
    assertThat(makePageTemplate().hasHeader()).isFalse();
  }

  @Test
  void hasHeaderHelpMessageKey() {
    assertThat(makePageTemplate().hasHeaderHelpMessageKey()).isTrue();
    headerHelpMessageKey = "";
    assertThat(makePageTemplate().hasHeaderHelpMessageKey()).isFalse();
    headerHelpMessageKey = null;
    assertThat(makePageTemplate().hasHeaderHelpMessageKey()).isFalse();
  }

  @Test
  void hasSubtleLinkTextKey() {
    assertThat(makePageTemplate().hasSubtleLinkTextKey()).isTrue();
    subtleLinkTextKey = "";
    assertThat(makePageTemplate().hasSubtleLinkTextKey()).isFalse();
    subtleLinkTextKey = null;
    assertThat(makePageTemplate().hasSubtleLinkTextKey()).isFalse();
  }

  private PageTemplate makePageTemplate() {
    return new PageTemplate(
        List.of(),
        "a-name",
        "aPageTitle",
        headerKey,
        headerHelpMessageKey,
        "aPrimaryButtonTextKey",
        subtleLinkTextKey,
        "aSubtleLinkTargetPage",
        true,
        "context-frag",
        alertBox
    );
  }
}
