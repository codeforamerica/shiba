package org.codeforamerica.shiba.output.documentfieldpreparers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_US_CITIZEN;

@Component
public class HouseholdUsCitizenPreparer implements DocumentFieldPreparer {

	@Override
	public List<DocumentField> prepareDocumentFields(Application application, Document document, Recipient recipient) {

		List<String> nonUsCitizenHouseholdMembers = Optional
				.ofNullable(application.getApplicationData().getPageData("whoIsNonCitizen"))
				.map(pageData -> pageData.get("whoIsNonCitizen")).map(InputData::getValue).orElse(List.of(""));

		List<String> householdMemberIDs = nonUsCitizenHouseholdMembers.stream().map(selectedHouseholdMember -> {
			String[] householdMemberParts = selectedHouseholdMember.split(" ");
			return householdMemberParts[householdMemberParts.length - 1];
		}).collect(Collectors.toList());

		List<DocumentField> result = new ArrayList<>();
		boolean usCitizenAsked = application.getApplicationData().getPagesData().getPage("usCitizen") != null;
		boolean whoIsNonCitizenAsked = application.getApplicationData().getPagesData().getPage("whoIsNonCitizen") != null;
		Subworkflow householdMemberSubworkflow = application.getApplicationData().getSubworkflows().get("household");
	    PagesData data = application.getApplicationData().getPagesData();

	    Boolean isApplicantUsCitizen = Boolean.valueOf(getFirstValue(data, IS_US_CITIZEN));

		if (!usCitizenAsked)
		{
			result.add(new DocumentField("usCitizen", "applicantIsUsCitizen", "Off", DocumentFieldType.SINGLE_VALUE, null));
		} else {
			if (!whoIsNonCitizenAsked) {
				result.add(new DocumentField("usCitizen", "applicantIsUsCitizen",
						isApplicantUsCitizen ? "true" : "false", DocumentFieldType.SINGLE_VALUE, null));
			} else {
				result.add(new DocumentField("usCitizen", "applicantIsUsCitizen",
						List.of(householdMemberIDs.contains("applicant") ? "false" : "true"),
						DocumentFieldType.SINGLE_VALUE, null));
			}

			if (householdMemberSubworkflow != null) {
				for (int i = 0; i < householdMemberSubworkflow.size(); i++) {
					result.add(
							new DocumentField("usCitizen", "isUsCitizen",
									List.of(householdMemberIDs.contains(
											householdMemberSubworkflow.get(i).getId().toString()) ? "false" : "true"),
									DocumentFieldType.SINGLE_VALUE, i));
				}

			}
		}
	    return result;
		
	}
}
