package org.codeforamerica.shiba.pages.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageTemplateTest {

  private String headerKey;
  private String headerHelpMessageKey;
  private String subtleLinkTextKey;
  private String cardFooterTextKey;
  private AlertBox alertBox;

  @BeforeEach
  void setup() {
    headerKey = "aHeaderKey";
    headerHelpMessageKey = "aHeaderHelpMessageKey";
    subtleLinkTextKey = "aSubtleLinkTextKey";
    cardFooterTextKey = "aCardFooterTextKey";
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

  @Test
  void hasCardFooterTextKey() {
    assertThat(makePageTemplate().hasCardFooterTextKey()).isTrue();
    cardFooterTextKey = "";
    assertThat(makePageTemplate().hasCardFooterTextKey()).isFalse();
    cardFooterTextKey = null;
    assertThat(makePageTemplate().hasCardFooterTextKey()).isFalse();
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
        cardFooterTextKey,
        true,
        false,
        "context-frag",
        alertBox
    );
  }
}
