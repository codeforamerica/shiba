package org.codeforamerica.shiba.application;

/**
 * FlowType indicates the path taken by the applicant as determined by
 * their choices in certain pages of the application. 
 * FlowType is set in pages.config.yaml using nextPages.
 */
public enum FlowType {
  EXPEDITED,
  FULL,
  MINIMUM,
  UNDETERMINED,
  LATER_DOCS,
  HEALTHCARE_RENEWAL
}
