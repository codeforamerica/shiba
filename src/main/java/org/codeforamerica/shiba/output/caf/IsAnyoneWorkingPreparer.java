package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.springframework.stereotype.Component;

/**
 * If anybody in the household(either single applicant or household members) has
 * a self employment job, and nobody works for others(a nonSelfEmployment job), then mark the
 * IS_WORKING radio button to No on the CCAP and Certain Pops PDF.
 *
 */
@Component
public class IsAnyoneWorkingPreparer implements DocumentFieldPreparer {

	private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

	public IsAnyoneWorkingPreparer(GrossMonthlyIncomeParser grossMonthlyIncomeParser) {
		this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
	}

	@Override
	public List<DocumentField> prepareDocumentFields(Application application, Document document, Recipient recipient) {
		List<JobIncomeInformation> jobsList = getAllJobsToFindSelfEmployment(application, document);
		List<DocumentField> fields = new ArrayList<>();
		boolean somebodyInHouseholdHasNonSelfEmployment = false;//works for somebody else
		for (JobIncomeInformation job : jobsList) {
			boolean isSelfEmployment = getBooleanValue(job.getIteration().getPagesData(), IS_SELF_EMPLOYMENT);
			if (!isSelfEmployment) {
					somebodyInHouseholdHasNonSelfEmployment = true;
					break;
			}
		}

		if (somebodyInHouseholdHasNonSelfEmployment) {
			fields.add(new DocumentField("employmentStatus", "isAnyoneWorking", "true", ENUMERATED_SINGLE_VALUE, null));
		} else{
			fields.add(new DocumentField("employmentStatus", "isAnyoneWorking", "false", ENUMERATED_SINGLE_VALUE, null));
		}
		return fields;
	}

	private List<JobIncomeInformation> getAllJobsToFindSelfEmployment(Application application,
			Document document) {
		List<JobIncomeInformation> listOfJobsForAllHouseholdMembers = grossMonthlyIncomeParser
				.parse(application.getApplicationData());
		return listOfJobsForAllHouseholdMembers;
	}

}
