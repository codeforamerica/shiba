package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.codeforamerica.shiba.output.documentfieldpreparers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class ExpeditedEligibilityPreparer implements DocumentFieldPreparer {

  private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
  private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;

  public ExpeditedEligibilityPreparer(
      SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
      CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider) {
    this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
    this.ccapExpeditedEligibilityDecider = ccapExpeditedEligibilityDecider;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    ApplicationData data = application.getApplicationData();
    return List.of(
        new DocumentField(
            "snapExpeditedEligibility",
            "snapExpeditedEligibility",
            List.of(snapExpeditedEligibilityDecider.decide(data).getStatus()),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "ccapExpeditedEligibility",
            "ccapExpeditedEligibility",
            List.of(ccapExpeditedEligibilityDecider.decide(data).getStatus()),
            DocumentFieldType.SINGLE_VALUE
        ));
  }
}
