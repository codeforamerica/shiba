package org.codeforamerica.shiba.pages.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

/**
 * A page's PageConfiguration describes the content of the page to be rendered.
 */
@Data
public class PageConfiguration {

  /**
   * Path of the webpage and usually where the pageData is stored in applicationData.
   */
  private String name;

  /**
   * List of inputs for the page. These may be readonly.
   */
  private List<FormInput> inputs = List.of();

  /**
   * "title" component of the webpage.
   */
  private Value pageTitle;

  /**
   * Webpage heading.
   */
  private Value headerKey;

  /**
   * Webpage heading subtext.
   */
  private Value headerHelpMessageKey;

  /**
   * Hyperlink at the bottom of the page used for navigation. Destination is set at {@link
   * PageWorkflowConfiguration#getSubtleLinkTargetPage()}
   *
   * @see PageWorkflowConfiguration#getSubtleLinkTargetPage()
   */
  private Value subtleLinkTextKey;

  /**
   * Subtle footer text at the bottom of card page used for additional information.
   */
  private Value cardFooterTextKey;

  /**
   * Text for the (submit) button for the page form or page navigation. Defaults to "Continue". Only
   * applicable if {@link #hasPrimaryButton} is {@code true}.
   *
   * @see #hasPrimaryButton
   */
  private String primaryButtonTextKey = "general.continue";

  /**
   * boolean on whether to show the button (true) or not (false).
   *
   * @see #primaryButtonTextKey
   */
  private Boolean hasPrimaryButton = true;
  
  /**
   * boolean on whether to have a go back navigation link.
   *
   */
  private Boolean excludeGoBack = false;

  /**
   * Additional content displayed at the top of the page card. Usually an image.
   */
  private String contextFragment;

  /**
   * Boolean to indicate whether this page should be rendered with the pageTemplate (true) or not
   * (false). Custom pages do not use the same template that static and form pages use.
   */
  private boolean usingPageTemplateFragment = true;

  /**
   * Warning or notice message for page.
   */
  private AlertBox alertBox;

  public List<FormInput> getFlattenedInputs() {
    return this.inputs.stream()
        .flatMap(
            formInput -> Stream.concat(Stream.of(formInput), formInput.getFollowUps().stream()))
        .collect(Collectors.toList());
  }

  /**
   * Static pages use the same template format as form pages and requires an html page/fragment to
   * be present even if it's empty.
   *
   * @return True - is a static page; False - otherwise
   */
  public boolean isStaticPage() {
    return usingPageTemplateFragment && this.inputs.isEmpty();
  }

}
