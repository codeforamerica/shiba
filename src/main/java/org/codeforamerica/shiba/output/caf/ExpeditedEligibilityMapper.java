package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class ExpeditedEligibilityMapper implements ApplicationInputsMapper {

  private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
  private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;

  public ExpeditedEligibilityMapper(SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
      CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider) {
    this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
    this.ccapExpeditedEligibilityDecider = ccapExpeditedEligibilityDecider;
  }

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    ApplicationData data = application.getApplicationData();
    return List.of(
        new ApplicationInput(
            "snapExpeditedEligibility",
            "snapExpeditedEligibility",
            List.of(snapExpeditedEligibilityDecider.decide(data).getStatus()),
            ApplicationInputType.SINGLE_VALUE
        ),
        new ApplicationInput(
            "ccapExpeditedEligibility",
            "ccapExpeditedEligibility",
            List.of(ccapExpeditedEligibilityDecider.decide(data).getStatus()),
            ApplicationInputType.SINGLE_VALUE
        ));
  }
}
