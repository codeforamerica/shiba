package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

// Test cases for the CertainPopsPreparer class
public class CertainPopsPreparerTest {

	private final CertainPopsPreparer preparer = new CertainPopsPreparer();

	// Neither unearned income nor other unearned income types are selected.
	@Test
	public void shouldMapNoUnearndIncomeAndNoOtherUnearnedIncomeToFalse() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome",
						List.of("NO_OTHER_UNEARNED_INCOME_SELECTED"))
				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"false");
		assertThat(result).contains(documentField);
	}

	// An unearned income type is selected but no other unearned income types are
	// selected.
	@Test
	public void shouldMapUnearndIncomeAndNoOtherUnearnedIncomeToTrue() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome",
						List.of("NO_OTHER_UNEARNED_INCOME_SELECTED"))
				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"true");
		assertThat(result).contains(documentField);
	}

	// No unearned income types are selected but an other unearned income type is
	// selected.
	@Test
	public void shouldMapNoUnearndIncomeAndOtherUnearnedIncomeToTrue() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("TRUST_MONEY")).build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"true");
		assertThat(result).contains(documentField);
	}

	// An unearned income type is selected and an other unearned income type is
	// selected.
	@Test
	public void shouldMapUnearndIncomeAndOtherUnearnedIncomeToTrue() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("TRUST_MONEY")).build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"true");
		assertThat(result).contains(documentField);
	}

	// The applicant has unearned incomes, he is person 1. There are no household
	// members
	@Test
	public void shouldMapApplicantUnearndIncomeFields() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("TRUST_MONEY"))

				.withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of("100"))
				.withPageData("unearnedIncomeSources", "retirementAmount", List.of())
				.withPageData("unearnedIncomeSources", "unemploymentAmount", List.of())
				.withPageData("unearnedIncomeSources", "tribalPaymentsAmount", List.of())
				.withPageData("unearnedIncomeSources", "veteransBenefitsAmount", List.of())
				.withPageData("unearnedIncomeSources", "workersCompensationAmount", List.of())
				.withPageData("unearnedIncomeSources", "childOrSpousalSupportAmount", List.of())
				.withPageData("unearnedIncomeSources", "supplementalSecurityIncomeAmount", List.of())

				.withPageData("otherUnearnedIncomeSources", "trustMoneyAmount", List.of("200"))
				.withPageData("otherUnearnedIncomeSources", "benefitsAmount", List.of())
				.withPageData("otherUnearnedIncomeSources", "rentalIncomeAmount", List.of())
				.withPageData("otherUnearnedIncomeSources", "otherPaymentsAmount", List.of())
				.withPageData("otherUnearnedIncomeSources", "contractForDeedAmount", List.of())
				.withPageData("otherUnearnedIncomeSources", "insurancePaymentsAmount", List.of())
				.withPageData("otherUnearnedIncomeSources", "interestDividendsAmount", List.of())
				.withPageData("otherUnearnedIncomeSources", "healthCareReimbursementAmount", List.of())

				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"true");
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

	// The applicant has no unearned income. A single household member has unearned
	// income, he is person 1.
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
				"true");
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

	// Applicant has all unearned income types. Verify that DocumentFields are
	// generated for all income types.
	@Test
	public void shouldMapAllUnearndIncomeTypes() {
		List<String> unearnedIncomeTypes = List.of("SOCIAL_SECURITY", "SSI", "VETERANS_BENEFITS", "UNEMPLOYMENT",
				"WORKERS_COMPENSATION", "RETIREMENT", "CHILD_OR_SPOUSAL_SUPPORT", "TRIBAL_PAYMENTS");
		List<String> otherUnearnedIncomeTypes = List.of("INSURANCE_PAYMENTS", "TRUST_MONEY", "RENTAL_INCOME",
				"INTEREST_DIVIDENDS", "HEALTH_CARE_REIMBURSEMENT", "CONTRACT_FOR_DEED", "BENEFITS", "OTHER_PAYMENTS");
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withPageData("unearnedIncome", "unearnedIncome", unearnedIncomeTypes)
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome", otherUnearnedIncomeTypes)

				.withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of("100"))
				.withPageData("unearnedIncomeSources", "supplementalSecurityIncomeAmount", List.of("101"))
				.withPageData("unearnedIncomeSources", "veteransBenefitsAmount", List.of("102"))
				.withPageData("unearnedIncomeSources", "unemploymentAmount", List.of("103"))
				.withPageData("unearnedIncomeSources", "workersCompensationAmount", List.of("104"))
				.withPageData("unearnedIncomeSources", "retirementAmount", List.of("105"))
				.withPageData("unearnedIncomeSources", "childOrSpousalSupportAmount", List.of("106"))
				.withPageData("unearnedIncomeSources", "tribalPaymentsAmount", List.of("107"))

				.withPageData("otherUnearnedIncomeSources", "insurancePaymentsAmount", List.of("200"))
				.withPageData("otherUnearnedIncomeSources", "trustMoneyAmount", List.of("201"))
				.withPageData("otherUnearnedIncomeSources", "rentalIncomeAmount", List.of("202"))
				.withPageData("otherUnearnedIncomeSources", "interestDividendsAmount", List.of("203"))
				.withPageData("otherUnearnedIncomeSources", "healthCareReimbursementAmount", List.of("204"))
				.withPageData("otherUnearnedIncomeSources", "contractForDeedAmount", List.of("205"))
				.withPageData("otherUnearnedIncomeSources", "benefitsAmount", List.of("206"))
				.withPageData("otherUnearnedIncomeSources", "otherPaymentsAmount", List.of("207"))

				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				"true");
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

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_2", "SSI");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_2",
				"101");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_2",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_3",
				"Veterans Benefits");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_3",
				"102");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_3",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_4",
				"Unemployment");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_4",
				"103");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_4",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_5",
				"Workers Compensation");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_5",
				"104");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_5",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_6",
				"Retirement");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_6",
				"105");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_6",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_7",
				"Child or spousal support");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_7",
				"106");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_7",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_8",
				"Tribal payments");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_8",
				"107");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_8",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_9",
				"Insurance payments");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_9",
				"200");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_9",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_10",
				"Trust money");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_10",
				"201");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_10",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_11",
				"Rental income");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_11",
				"202");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_11",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_12",
				"Interest or dividends");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_12",
				"203");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_12",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_13",
				"Healthcare reimbursement");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_13",
				"204");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_13",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_14",
				"Contract for Deed");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_14",
				"205");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_14",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_15",
				"Benefits programs");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_15",
				"206");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_15",
				"Monthly");
		assertThat(result).contains(documentField);

		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeType_1_16",
				"Other payments");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeAmount_1_16",
				"207");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsUnearnedIncome", "certainPopsUnearnedIncomeFrequency_1_16",
				"Monthly");
		assertThat(result).contains(documentField);
	}

	// Answer is No on savings page, result is "false" for hasCertainPopsBankAccounts.
	@Test
	public void shouldMapBankAccountFieldsToFalse() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withSubworkflow("household", new PagesDataBuilder().withPageData("householdMemberInfo",
						Map.of("firstName", List.of("Jane"), "lastName", List.of("Smith"))))
				.withPageData("savings", "haveSavings", "false")
				.build();

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsBankAccounts", "hasCertainPopsBankAccounts",
				"false");
		assertThat(result).contains(documentField);
	}

	// The applicant and one household member each have two bank accounts.  Test DocumentFields for the first three items.
	@Test
	public void shouldMapBankAccountFieldsForHousehold() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withSubworkflow("household", new PagesDataBuilder().withPageData("householdMemberInfo",
						Map.of("firstName", List.of("Jane"), "lastName", List.of("Smith"))))
				.withPageData("savingsAccountSource", "savingsAccountSource", List.of("David Smith applicant"))
				.withPageData("checkingAccountSource", "checkingAccountSource", List.of("Jane Smith 12345678-1234-1234-1234-123456789012"))
				.withPageData("moneyMarketSource", "moneyMarketSource", List.of("David Smith applicant"))
				.withPageData("certOfDepositSource", "certOfDepositSource", List.of("Jane Smith 12345678-1234-1234-1234-123456789012"))
				.build();
		
		applicationData.getSubworkflows().get("household").get(0)
				.setId(UUID.fromString("12345678-1234-1234-1234-123456789012"));

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);
		DocumentField documentField = createApplicationInput("certainPopsBankAccounts", "hasCertainPopsBankAccounts",
				"true");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountOwnerLine_1",
				"David Smith");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountTypeLine_1",
				"Savings account");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountOwnerLine_2",
				"Jane Smith");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountTypeLine_2",
				"Checking account");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountOwnerLine_3",
				"David Smith");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountTypeLine_3",
				"Money market account");
		assertThat(result).contains(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountOwnerLine_4",
				"Jane Smith");
		assertThat(result).doesNotContain(documentField);
		documentField = createApplicationInput("certainPopsBankAccounts", "certainPopsBankAccountTypeLine_4",
				"Certificate of deposit");
		assertThat(result).doesNotContain(documentField);
		// Verify supplement
		DocumentField supplementDocumentField = findAndVerifyCertainPopsSupplement(result);
		String supplementText = supplementDocumentField.getValue(0);
		assertThat(supplementText).contains(
				"QUESTION 14 continued:\n4) Owner name: Jane Smith, Type of account: Certificate of deposit");
	}


	// Question 11 supplement text is generated when more than 2 people have unearned income or
	// when a person has more than 4 unearned income types.
	// This test has 3 persons with unearned income. Person 3 has 5 unearned income
	// types.
	// Expected text:
	//
	// QUESTION 11 continued:
	// Person 3, John Smith:
	// 1) Social Security, 102, Monthly
	// 2) Insurance payments, 200, Monthly
	// 3) Trust money, 201, Monthly
	// 4) Rental income, 202, Monthly
	// 5) Interest or dividends, 203, Monthly
	@Test
	public void shouldMapQuestion11SupplementText() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
				.withPageData("otherUnearnedIncome", "otherUnearnedIncome",
						List.of("INSURANCE_PAYMENTS", "TRUST_MONEY", "RENTAL_INCOME", "INTEREST_DIVIDENDS"))

				.withPageData("socialSecurityIncomeSource", "socialSecurityAmount", List.of("100", "101", "102"))
				.withPageData("socialSecurityIncomeSource", "monthlyIncomeSSorRSDI",
						List.of("David Smith applicant", "Jane Smith 12345678-1234-1234-1234-123456789012",
								"John Smith 22345678-1234-1234-1234-223456789012"))

				.withPageData("insurancePaymentsIncomeSource", "insurancePaymentsAmount", List.of("", "", "200"))
				.withPageData("insurancePaymentsIncomeSource", "monthlyIncomeInsurancePayments",
						List.of("John Smith 22345678-1234-1234-1234-223456789012"))

				.withPageData("trustMoneyIncomeSource", "trustMoneyAmount", List.of("", "", "201"))
				.withPageData("trustMoneyIncomeSource", "monthlyIncomeTrustMoney",
						List.of("John Smith 22345678-1234-1234-1234-223456789012"))

				.withPageData("rentalIncomeSource", "rentalIncomeAmount", List.of("", "", "202"))
				.withPageData("rentalIncomeSource", "monthlyIncomeRental",
						List.of("John Smith 22345678-1234-1234-1234-223456789012"))

				.withPageData("interestDividendsIncomeSource", "interestDividendsAmount", List.of("", "", "203"))
				.withPageData("interestDividendsIncomeSource", "monthlyIncomeInterestDividends",
						List.of("John Smith 22345678-1234-1234-1234-223456789012"))

				.withSubworkflow("household",
						new PagesData(Map.of("householdMemberInfo",
								new PageData(Map.of("firstName", new InputData(List.of("Jane")), "lastName",
										new InputData(List.of("Smith")))))),
						new PagesData(Map.of("householdMemberInfo", new PageData(Map.of("firstName",
								new InputData(List.of("John")), "lastName", new InputData(List.of("Smith")))))))
				.build();

		// fix the iteration IDs
		Subworkflow subworkflow = applicationData.getSubworkflows().get("household");
		subworkflow.get(0).setId(UUID.fromString("12345678-1234-1234-1234-123456789012"));
		subworkflow.get(1).setId(UUID.fromString("22345678-1234-1234-1234-223456789012"));

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);

		DocumentField supplementDocumentField = findAndVerifyCertainPopsSupplement(result);
		String supplementText = supplementDocumentField.getValue(0);
		assertThat(supplementText).contains(
				"QUESTION 11 continued:\nPerson 3, John Smith:\n  1) Social Security, 102, Monthly\n  2) Insurance payments, 200, Monthly\n  3) Trust money, 201, Monthly\n  4) Rental income, 202, Monthly\n  5) Interest or dividends, 203, Monthly");
	}

	// Question 6 supplement text is generated when more than 2 people are non-US citizens.
	// This test has 4 persons that are non-US citizens. The alien ID for person 3 was specified as "C33333333C" but
	// the alien ID for person 4 is not provided so displayed as blank.
	// Expected text:
	//
	// QUESTION 6 continued:
	// Person 3: John Smith, Alien ID: C33333333C
	// Person 4: Jill Smith, Alien ID:
	@Test
	public void shouldMapQuestion6SupplementText() {
		ApplicationData applicationData = new TestApplicationDataBuilder()
				.withPageData("personalInfo", "firstName", List.of("David"))
				.withPageData("personalInfo", "lastName", List.of("Smith"))
				.withPageData("whoIsNonCitizen", "whoIsNonCitizen", List.of("David Smith applicant", "Jane Smith 22345678-1234-1234-1234-123456789012",
						"John Smith 32345678-1234-1234-1234-223456789013", "Jill Smith 42345678-1234-1234-1234-223456789014"))
				.withPageData("alienIdNumbers", "alienIdNumber", List.of("A11111111A", "B22222222B", "C33333333C", ""))
				.withPageData("alienIdNumbers", "alienIdMap", List.of("applicant", "22345678-1234-1234-1234-123456789012",
						"32345678-1234-1234-1234-223456789013", "42345678-1234-1234-1234-223456789014"))

				.withSubworkflow("household",
						new PagesData(Map.of("householdMemberInfo",
								new PageData(Map.of("firstName", new InputData(List.of("Jane")), "lastName",
										new InputData(List.of("Smith")))))),
						new PagesData(Map.of("householdMemberInfo",
								new PageData(Map.of("firstName", new InputData(List.of("John")), "lastName",
										new InputData(List.of("Smith")))))),
						new PagesData(Map.of("householdMemberInfo", new PageData(Map.of("firstName",
								new InputData(List.of("Jill")), "lastName", new InputData(List.of("Smith")))))))
				.build();

		// fix the iteration IDs
		Subworkflow subworkflow = applicationData.getSubworkflows().get("household");
		subworkflow.get(0).setId(UUID.fromString("22345678-1234-1234-1234-123456789012"));
		subworkflow.get(1).setId(UUID.fromString("32345678-1234-1234-1234-223456789013"));
		subworkflow.get(2).setId(UUID.fromString("42345678-1234-1234-1234-223456789014"));

		List<DocumentField> result = preparer
				.prepareDocumentFields(Application.builder().applicationData(applicationData).build(), null, null);

		DocumentField supplementDocumentField = findAndVerifyCertainPopsSupplement(result);
		String supplementText = supplementDocumentField.getValue(0);
		assertThat(supplementText).contains(
				"QUESTION 6 continued:\nPerson 3: John Smith, Alien ID: C33333333C\nPerson 4: Jill Smith, Alien ID:");
	}
	
	//QUESTION 15
	@Test
    public void shouldMapQuestion15SupplementText() {
      ApplicationData applicationData = new TestApplicationDataBuilder().withPersonalInfo()
          .withPageData("addHouseholdMembers", "addHouseholdMembers", "true")
          .withSubworkflow("household",
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("Jane")), "lastName",
                      new InputData(List.of("Smith")))))),
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("John")), "lastName",
                      new InputData(List.of("Smith")))))),
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("Jill")), "lastName",
                      new InputData(List.of("Smith")))))),
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("Jack")), "lastName",
                      new InputData(List.of("Smith")))))))
          .withPageData("assets", "assets", List.of("STOCK_BOND"))
          .withPageData("investmentAssetType", "investmentAssetType",
              List.of("STOCKS", "BONDS", "RETIREMENT_ACCOUNTS"))
          .withPageData("stocksHouseHoldSource", "stocksHouseHoldSource",
              List.of("Jane Doe applicant", "Jane Smith notSpouse"))
          .withPageData("bondsHouseHoldSource", "bondsHouseHoldSource",
              List.of("Jane Smith notSpouse", "Jane Doe applicant"))
          .withPageData("retirementAccountsHouseHoldSource", "retirementAccountsHouseHoldSource",
              List.of("Jane Smith notSpouse", "Jane Doe applicant", "Jill Smith member1",
                  "Jack Smith member2"))
          .build();
      List<DocumentField> result = preparer.prepareDocumentFields(
          Application.builder().applicationData(applicationData).build(), null,
          Recipient.CASEWORKER);
      assertThat(result).containsAll(List.of(
          new DocumentField("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome", "false", DocumentFieldType.ENUMERATED_SINGLE_VALUE),
          new DocumentField("certainPops", "certainPopsSupplement",
          List.of(
              "\n\nQUESTION 15 continued:\nPerson 4: Jack Smith, Investment Type: retirement accounts"),
          DocumentFieldType.ENUMERATED_SINGLE_VALUE)));
	}
	
	//QUESTION 8
    @Test
    public void shouldMapQuestion8SupplementText() {
      ApplicationData applicationData = new TestApplicationDataBuilder().withPersonalInfo()
          .withPageData("addHouseholdMembers", "addHouseholdMembers", "true")
          .withSubworkflow("household",
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("Jane")), "lastName",
                      new InputData(List.of("Smith")))))),
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("John")), "lastName",
                      new InputData(List.of("Smith")))))),
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("Jill")), "lastName",
                      new InputData(List.of("Smith")))))),
              new PagesData(Map.of("householdMemberInfo",
                  new PageData(Map.of("firstName", new InputData(List.of("Jack")), "lastName",
                      new InputData(List.of("Smith")))))))
  
          .withPageData("retroactiveCoverage", "retroactiveCoverageQuestion", "true")
          .withPageData("retroactiveCoverageSource", "retroactiveCoverageSourceQuestion",
              List.of("Jane Smith 0", "John Smith 1", "Jill Smith 2", "Jack Smith 3"))
          .withPageData("retroactiveCoverageTimePeriod", "retroactiveCoverageNumberMonths",
              List.of("1", "2", "3", "2"))
          .withPageData("retroactiveCoverageTimePeriod", "retroactiveCoverageMap",
              List.of("0", "1", "2", "3"))
          .build();
      List<DocumentField> result = preparer.prepareDocumentFields(
          Application.builder().applicationData(applicationData).build(), null,
          Recipient.CASEWORKER);
      assertThat(result).containsAll(List.of(
          new DocumentField("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome", "false", DocumentFieldType.ENUMERATED_SINGLE_VALUE),
          new DocumentField("certainPops", "certainPopsSupplement",
          List.of(
              "\n\nQUESTION 8 continued:\nPerson 3: Jill Smith, Month/s: 3"
              + "\nPerson 4: Jack Smith, Month/s: 2"),
          DocumentFieldType.ENUMERATED_SINGLE_VALUE)));
    }
    
    private DocumentField findAndVerifyCertainPopsSupplement(List<DocumentField> result) {
		DocumentField supplementDocumentField = null;
		for (DocumentField documentField : result) {
			if (documentField.getName().compareTo("certainPopsSupplement") == 0) {
				supplementDocumentField = documentField;
				break;
			}
		}
		assertThat(supplementDocumentField).isNotNull();
		return supplementDocumentField;
    }

}
