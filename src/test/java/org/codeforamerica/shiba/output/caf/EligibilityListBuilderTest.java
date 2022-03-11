package org.codeforamerica.shiba.output.caf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class EligibilityListBuilderTest {
	
	@Test
	public void testBothCCAPandSNAP_NotEligible() {
		Eligibility ccap = CcapExpeditedEligibility.NOT_ELIGIBLE;
		Eligibility snap = SnapExpeditedEligibility.NOT_ELIGIBLE;
		List<Eligibility> expeditedEligibilityList = new ArrayList<Eligibility>();
		expeditedEligibilityList.add(ccap);
		expeditedEligibilityList.add(snap);
		EligibilityListBuilder listBuilder = new EligibilityListBuilder();
		List<ExpeditedEligibility> list = listBuilder.buildEligibilityList(expeditedEligibilityList);
		assertTrue(list.contains(ExpeditedEligibility.UNDETERMINED));
	}
	
	@Test
	public void testCCAP_EligibleAndSNAP_NotEligible() {
		Eligibility ccap = CcapExpeditedEligibility.ELIGIBLE;
		Eligibility snap = SnapExpeditedEligibility.NOT_ELIGIBLE;
		List<Eligibility> expeditedEligibilityList = new ArrayList<Eligibility>();
		expeditedEligibilityList.add(ccap);
		expeditedEligibilityList.add(snap);
		EligibilityListBuilder listBuilder = new EligibilityListBuilder();
		List<ExpeditedEligibility> list = listBuilder.buildEligibilityList(expeditedEligibilityList);
		assertTrue(list.size() == 1);
		assertTrue(list.contains(ExpeditedEligibility.CCAP));
	}
	
	@Test
	public void testSNAP_EligibleAndCCAP_NotEligible() {
		Eligibility ccap = CcapExpeditedEligibility.NOT_ELIGIBLE;
		Eligibility snap = SnapExpeditedEligibility.ELIGIBLE;
		List<Eligibility> expeditedEligibilityList = new ArrayList<Eligibility>();
		expeditedEligibilityList.add(ccap);
		expeditedEligibilityList.add(snap);
		EligibilityListBuilder listBuilder = new EligibilityListBuilder();
		List<ExpeditedEligibility> list = listBuilder.buildEligibilityList(expeditedEligibilityList);
		assertTrue(list.size() == 1);
		assertTrue(list.contains(ExpeditedEligibility.SNAP));
	}
	
	@Test
	public void testCCAP_EligibleAndSNAP_Eligible() {
		Eligibility ccap = CcapExpeditedEligibility.ELIGIBLE;
		Eligibility snap = SnapExpeditedEligibility.ELIGIBLE;
		List<Eligibility> expeditedEligibilityList = new ArrayList<Eligibility>();
		expeditedEligibilityList.add(ccap);
		expeditedEligibilityList.add(snap);
		EligibilityListBuilder listBuilder = new EligibilityListBuilder();
		List<ExpeditedEligibility> list = listBuilder.buildEligibilityList(expeditedEligibilityList);
		assertTrue(list.size() == 2);
		assertTrue(list.contains(ExpeditedEligibility.CCAP));
		assertTrue(list.contains(ExpeditedEligibility.SNAP));
	}

}
