package org.codeforamerica.shiba.output.caf;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class EligibilityListBuilder {
	
	public List<ExpeditedEligibility> buildEligibilityList(List<Eligibility> eligibilityList){
		List<ExpeditedEligibility> expeditedEligibilityList = new ArrayList<ExpeditedEligibility>();
		boolean allEmpty = eligibilityList.stream().allMatch(e -> e.getStatus().equals(""));
		if(allEmpty) {
			expeditedEligibilityList.add(ExpeditedEligibility.UNDETERMINED);
			return expeditedEligibilityList;
		}
		if(eligibilityList.contains(SnapExpeditedEligibility.ELIGIBLE)) {
			expeditedEligibilityList.add(ExpeditedEligibility.SNAP);
		}
		
		if(eligibilityList.contains(CcapExpeditedEligibility.ELIGIBLE)) {
			expeditedEligibilityList.add(ExpeditedEligibility.CCAP);
		}
		return expeditedEligibilityList;
	}

}
