package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

// Test cases for the CertainPopsPreparer class
public class CertainPopsPreparerTest {

	private final CertainPopsPreparer preparer = new CertainPopsPreparer();

	// Neither unearned income nor other unearned income types are selected.
	@Test
	public void shouldMapNoUnearndIncomeAndNoOtherUnearnedIncomeToTrue() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome",
						List.of("NO_OTHER_UNEARNED_INCOME_SELECTED"))
				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"true");
		assertThat(result).contains(documentField);
	}

	// An unearned income type is selected but no other unearned income types are
	// selected.
	@Test
	public void shouldMapUnearndIncomeAndNoOtherUnearnedIncomeToFalse() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome",
						List.of("NO_OTHER_UNEARNED_INCOME_SELECTED"))
				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"false");
		assertThat(result).contains(documentField);
	}

	// No unearned income types are selected but an other unearned income type is
	// selected.
	@Test
	public void shouldMapNoUnearndIncomeAndOtherUnearnedIncomeToFalse() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("TRUST_MONEY")).build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"false");
		assertThat(result).contains(documentField);
	}

	// An unearned income type is selected and an other unearned income type is
	// selected.
	@Test
	public void shouldMapUnearndIncomeAndOtherUnearnedIncomeToFalse() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("TRUST_MONEY")).build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"false");
		assertThat(result).contains(documentField);
	}

	// Applicant has unearned incomes, is person 1. There are no household members
	@Test
	public void shouldMapApplicantUnearndIncomeFields() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("TRUST_MONEY"))
				.withPageData("socialSecurityIncomeSource", "socialSecurityAmount", List.of("100"))
				.withPageData("socialSecurityIncomeSource", "monthlyIncomeSSorRSDI", List.of("David Smith applicant"))
				.withPageData("trustMoneyIncomeSource", "trustMoneyAmount", List.of("200"))
				.withPageData("trustMoneyIncomeSource", "monthlyIncomeTrustMoney", List.of("David Smith applicant"))
				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"false");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomePerson1",
				"David Smith");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_1",
				"Social Security");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_1",
				"100");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_1",
				"Monthly");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_2",
				"Trust money");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_2",
				"200");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_2",
				"Monthly");
		assertThat(result).contains(documentField);
	}

	// Applicant has no unearned income. A single household member has unearned
	// income, is person 1.
	@Test
	public void shouldMapHouseholdMemberUnearndIncomeFields() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("TRUST_MONEY"))
				.withPageData("socialSecurityIncomeSource", "socialSecurityAmount", List.of("", "100"))
				.withPageData("socialSecurityIncomeSource", "monthlyIncomeSSorRSDI",
						List.of("Jane Smith 12345678-1234-1234-1234-123456789012"))
				.withPageData("trustMoneyIncomeSource", "trustMoneyAmount", List.of("", "200"))
				.withPageData("trustMoneyIncomeSource", "monthlyIncomeTrustMoney",
						List.of("Jane Smith 12345678-1234-1234-1234-123456789012"))
				.withSubworkflow("household", new PagesDataBuilder().withPageData("householdMemberInfo",
						Map.of("firstName", List.of("Jane"), "lastName", List.of("Smith"))))
				.build();
		applicationData.getSubworkflows().get("household").get(0)
				.setId(UUID.fromString("12345678-1234-1234-1234-123456789012"));

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"false");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomePerson1",
				"Jane Smith");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_1",
				"Social Security");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_1",
				"100");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_1",
				"Monthly");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_2",
				"Trust money");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_2",
				"200");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_2",
				"Monthly");
		assertThat(result).contains(documentField);
	}

}
