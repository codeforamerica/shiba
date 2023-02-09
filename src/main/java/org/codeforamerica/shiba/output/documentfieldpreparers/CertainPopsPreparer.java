package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME_OTHER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PERSONAL_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PERSONAL_INFO_LAST_NAME;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.documentfieldpreparers.InvestmentOwnerPreparer.Investment;
import org.codeforamerica.shiba.output.documentfieldpreparers.ListNonUSCitizenPreparer.NonUSCitizen;
import org.codeforamerica.shiba.output.documentfieldpreparers.ListRetroCoveragePreparer.RetroCoverageMember;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class CertainPopsPreparer implements DocumentFieldPreparer {
	ApplicationData applicationData = null;
	PagesData pagesData = null;
	List<DocumentField> certainPopsDocumentFields = new ArrayList<>();
	String supplementPageText = "";
	boolean needsSupplementPage = false;
	Set<String> cpAccountTypes = null;
	

	// Question 11, unearned income
	ArrayList<Person> persons = null;
	HashMap<String, Integer> lookup = null;

	@Override
	public List<DocumentField> prepareDocumentFields(Application application, Document document, Recipient recipient) {
		// No need to prepare fields if the document isn't Certain Pops.
		//if (document != Document.CERTAIN_POPS) return new ArrayList<DocumentField>();
		
		cpAccountTypes = Stream.of("SAVINGS", "CHECKING", "MONEY_MARKET", "CERTIFICATE_OF_DEPOSIT").collect(Collectors.toCollection(HashSet::new));
		applicationData = application.getApplicationData();
		pagesData = applicationData.getPagesData();
		certainPopsDocumentFields = new ArrayList<DocumentField>();
		supplementPageText = "";
		needsSupplementPage = false;
		persons = null;
		lookup = null;
		
		return map(application, document, recipient);
	}

	// This method controls the mapping logic for each of the Certain Pops
	// questions.
	private List<DocumentField> map(Application application, Document document, Recipient recipient) {
		// Question 4, household healthcare coverage
		mapHouseholdHealthCareCoverage(application, document, recipient);
		
		// Question 6, non-US citizens, generate the supplement if needed
		createNonUsCitizensSupplementPage(application, document, recipient);
		
		//Question 8, Retroactive coverage
		mapRetroactiveCoverage(application, document, recipient);
		
		//Question 9, Self-employment
		mapSelfEmployment();
		
		// Question 11, unearned income
		mapUnearnedIncomeFields();
		// Question 14, accounts
		mapBankAccountsFields(application, document, recipient);
		//Question 15, investment types
		mapInvestmentType(application, document, recipient);

		if (needsSupplementPage) {
			certainPopsDocumentFields.add(new DocumentField("certainPops", "certainPopsSupplement", supplementPageText,
					ENUMERATED_SINGLE_VALUE));
		}
		return certainPopsDocumentFields;
	}
	
	// Question 4, household healthcare coverage
	private void mapHouseholdHealthCareCoverage(Application application, Document document, Recipient recipient) {
		Subworkflow householdMemberSubworkflow = application.getApplicationData().getSubworkflows().get("household");

		if (householdMemberSubworkflow != null) {
			for (int i = 0; i < householdMemberSubworkflow.size(); i++) {
				Iteration iteration = householdMemberSubworkflow.get(i);
				PagesData pagesData = iteration.getPagesData();
				PageData householdMemberInfo = pagesData.getPage("householdMemberInfo");
				InputData programs = householdMemberInfo.get("programs");
				certainPopsDocumentFields.add(new DocumentField("certainPopsHouseholdMemberInfo",
						"choseHealthcareCoverage", programs.getValue().contains("CERTAIN_POPS") ? "true" : "false",
						DocumentFieldType.ENUMERATED_SINGLE_VALUE, i));
			}
		}
	}

	// Question 6, non-US citizens
	private void createNonUsCitizensSupplementPage(Application application, Document document, Recipient recipient) {
      ListNonUSCitizenPreparer lncp = new ListNonUSCitizenPreparer();
      List<NonUSCitizen> nonCitizens = lncp.getNonUSCitizens(application, document, recipient);
      if (nonCitizens.size() > 2) {
        needsSupplementPage = true;
        supplementPageText = String.format("%sQUESTION 6 continued:", supplementPageText);
        int i = 2;
        for (NonUSCitizen nuc : nonCitizens.subList(2, nonCitizens.size())) {
          supplementPageText = String.format("%s\nPerson %d: %s, Alien ID: %s", supplementPageText,
              i + 1, nuc.fullName, nuc.alienId);
          i++;
        }
      }
	}
  
	// Question 9 Self Employment
	// The SelfEmploymentPreparer will generate the DocumentFields for question #9 but if there
	// are more than two self-employment jobs we need to generate a supplement for the remainder.
	private void mapSelfEmployment() {
		Subworkflow jobs = getGroup(applicationData, ApplicationDataParser.Group.JOBS);
		SelfEmploymentPreparer selfEmploymentPreparer = new SelfEmploymentPreparer();
		List<Iteration> selfEmploymentJobs = selfEmploymentPreparer.getSelfEmploymentJobs(applicationData);
		for (int i = 2; i < selfEmploymentJobs.size(); i++) {
			Iteration job = selfEmploymentJobs.get(i);
			int jobNo = i+1;
			PagesData pagesData = job.getPagesData();
			PageData pageData = pagesData.getPage("householdSelectionForIncome");
			String employee = "";
			if (pageData != null) {
				employee = pageData.get("whoseJobIsItFormatted").getValue(0);
			} else { // when there is no whoseJobIsItFormatted then it has to be the applicant's job
				employee = selfEmploymentPreparer.applicantName(applicationData);
			}
			GrossMonthlyIncomeParser grossMonthlyIncomeParser = new GrossMonthlyIncomeParser();
			JobIncomeInformation jobIncomeInformation = grossMonthlyIncomeParser.parse(jobs, job);
			String grossMonthly = jobIncomeInformation.grossMonthlyIncome().toPlainString();

			if (jobNo == 3) {
				needsSupplementPage = true;
				supplementPageText = String.format("%s\n\n", supplementPageText);
				supplementPageText = String.format("%sQUESTION 9 continued:", supplementPageText);
			}
			if (jobNo > 2) {
				supplementPageText = String.format("%s\n%d) Name: %s, Monthly Income: %s", supplementPageText, jobNo, employee, grossMonthly);
			}
		}
	}

	// Question 11, unearned income
	private void mapUnearnedIncomeFields() {
		boolean hasUnearnedIncome = !mapNoUnearnedIncome();
		if (hasUnearnedIncome) {
			identifyAllPersons();
			// inputs could be on individual unearned income sources pages or on one
			// combined unearned income sources page
			identifyUnearnedIncomeItemsFromIndividualSourcesPages();
			identifyUnearnedIncomeItemsFromCombinedSourcesPages();
			mapUnearnedIncomeItems();
		}
	}

	// Iterate all Persons and generate DocumentFields for each of their unearned
	// income items. Section 11 allows for a maximum of 2 persons and 4 unearned
	// income types per person.
	private void mapUnearnedIncomeItems() {
	    supplementPageText = String.format("%s\n\n", supplementPageText);
		supplementPageText = String.format("%sQUESTION 11 continued:", supplementPageText);
		int personCount = 1;
		for (Person p : persons) {
			if (p.unearnedIncomeItems.size() > 0) {
				String fieldName = String.format("certainPopsUnearnedIncomePerson%d", personCount);
				certainPopsDocumentFields.add(
						new DocumentField("certainPopsUnearnedIncome", fieldName, p.fullName, ENUMERATED_SINGLE_VALUE));
				if (personCount > 2 || p.unearnedIncomeItems.size() > 4) {
					needsSupplementPage = true;
					supplementPageText = String.format("%s\nPerson %d, %s:", supplementPageText, personCount,
							p.fullName);
				}
				int itemCount = 1;
				for (UnearnedIncomeItem item : p.unearnedIncomeItems) {
					createDocumentFields(item.type, item.amount, personCount, itemCount);
					if (personCount > 2 || itemCount > 4) {
						supplementPageText = String.format("%s\n  %d) %s, %s, %s", supplementPageText, itemCount,
								item.type, item.amount, item.frequency);
					}
					itemCount++;
				}
				personCount++;
			}
		}
	}

	// A method to create the document fields for a single unearned income item.
	private void createDocumentFields(String incomeType, String amount, int person, int item) {
		String typeName = String.format("certainPopsUnearnedIncomeType_%d_%d", person, item);
		certainPopsDocumentFields
				.add(new DocumentField("certainPopsUnearnedIncome", typeName, incomeType, ENUMERATED_SINGLE_VALUE));

		String amountName = String.format("certainPopsUnearnedIncomeAmount_%d_%d", person, item);
		certainPopsDocumentFields
				.add(new DocumentField("certainPopsUnearnedIncome", amountName, amount, ENUMERATED_SINGLE_VALUE));

		String frequency = String.format("certainPopsUnearnedIncomeFrequency_%d_%d", person, item);
		certainPopsDocumentFields
				.add(new DocumentField("certainPopsUnearnedIncome", frequency, "Monthly", ENUMERATED_SINGLE_VALUE));

	}

	// Create the Person list (applicant and all household members) and a
	// corresponding "lookup" table
	private void identifyAllPersons() {
		persons = new ArrayList<Person>();
		lookup = new HashMap<String, Integer>();
		String applicantName = composeApplicantName();
		Person applicant = new Person(applicantName, "applicant", 0);
		String key = String.format("%s %s", applicantName, "applicant");
		lookup.put(key, 0);
		persons.add(0, applicant);

		Subworkflow subWorkflow = getGroup(applicationData, ApplicationDataParser.Group.HOUSEHOLD);
		if (subWorkflow != null) {
			for (int i = 0; i < subWorkflow.size(); i++) {
				Iteration iteration = subWorkflow.get(i);
				String id = iteration.getId().toString();
				PagesData pagesData = iteration.getPagesData();
				PageData pageData = pagesData.getPage("householdMemberInfo");
				String fullName = String.format("%s %s", pageData.get("firstName").getValue(0),
						pageData.get("lastName").getValue(0));
				key = String.format("%s %s", fullName, id);
				lookup.put(key, i + 1);
				persons.add(new Person(fullName, id, i + 1));
			}
		}
	}

	// These two methods are used together to reduce duplication of code.
	private void identifyUnearnedIncomeItemsFromIndividualSourcesPages() {
		processIncomeSource("socialSecurityIncomeSource", "monthlyIncomeSSorRSDI", "socialSecurityAmount",
				"Social Security");
		processIncomeSource("supplementalSecurityIncomeSource", "monthlyIncomeSSI", "supplementalSecurityIncomeAmount",
				"SSI");
		processIncomeSource("veteransBenefitsIncomeSource", "monthlyIncomeVeteransBenefits", "veteransBenefitsAmount",
				"Veterans Benefits");
		processIncomeSource("unemploymentIncomeSource", "monthlyIncomeUnemployment", "unemploymentAmount",
				"Unemployment");
		processIncomeSource("workersCompIncomeSource", "monthlyIncomeWorkersComp", "workersCompensationAmount",
				"Workers Compensation");
		processIncomeSource("retirementIncomeSource", "monthlyIncomeRetirement", "retirementAmount", "Retirement");
		processIncomeSource("childOrSpousalSupportIncomeSource", "monthlyIncomeChildOrSpousalSupport",
				"childOrSpousalSupportAmount", "Child or spousal support");
		processIncomeSource("tribalPaymentIncomeSource", "monthlyIncomeTribalPayment", "tribalPaymentsAmount",
				"Tribal payments");

		processIncomeSource("insurancePaymentsIncomeSource", "monthlyIncomeInsurancePayments",
				"insurancePaymentsAmount", "Insurance payments");
		processIncomeSource("trustMoneyIncomeSource", "monthlyIncomeTrustMoney", "trustMoneyAmount", "Trust money");
		processIncomeSource("rentalIncomeSource", "monthlyIncomeRental", "rentalIncomeAmount", "Rental income");
		processIncomeSource("interestDividendsIncomeSource", "monthlyIncomeInterestDividends",
				"interestDividendsAmount", "Interest or dividends");
		processIncomeSource("healthcareReimbursementIncomeSource", "monthlyIncomeHealthcareReimbursement",
				"healthCareReimbursementAmount", "Healthcare reimbursement");
		processIncomeSource("contractForDeedIncomeSource", "monthlyIncomeContractForDeed", "contractForDeedAmount",
				"Contract for Deed");
		processIncomeSource("benefitsProgramsIncomeSource", "monthlyIncomeBenefitsPrograms", "benefitsAmount",
				"Benefits programs");
		processIncomeSource("otherPaymentsIncomeSource", "monthlyIncomeOtherPayments", "otherPaymentsAmount",
				"Other payments");
	}

	private void processIncomeSource(String pageName, String personsKey, String amountsKey, String description) {
		PageData pageData = pagesData.getPage(pageName);
		if (pageData != null) {
			List<String> keys = pageData.get(personsKey).getValue();
			List<String> amounts = pageData.get(amountsKey).getValue();
			for (String key : keys) {
				int lookupIndex = lookup.get(key);
				Person person = persons.get(lookupIndex);
				String amount = amounts.get(person.personIndex);
				person.unearnedIncomeItems.add(new UnearnedIncomeItem(description, amount));
			}
		}
	}

	// These two methods are used together to reduce duplication of code.
	private void identifyUnearnedIncomeItemsFromCombinedSourcesPages() {
		PageData pageData = pagesData.getPage("unearnedIncomeSources");
		if (pageData != null) {
			processUnearnedIncomeSource(pageData, "socialSecurityAmount", "Social Security");
			processUnearnedIncomeSource(pageData, "supplementalSecurityIncomeAmount", "SSI");
			processUnearnedIncomeSource(pageData, "veteransBenefitsAmount", "Veterans Benefits");
			processUnearnedIncomeSource(pageData, "unemploymentAmount", "Unemployment");
			processUnearnedIncomeSource(pageData, "workersCompensationAmount", "Workers Compensation");
			processUnearnedIncomeSource(pageData, "retirementAmount", "Retirement");
			processUnearnedIncomeSource(pageData, "childOrSpousalSupportAmount", "Child or spousal support");
			processUnearnedIncomeSource(pageData, "tribalPaymentsAmount", "Tribal payments");
		}
		pageData = pagesData.getPage("otherUnearnedIncomeSources");
		if (pageData != null) {
			processUnearnedIncomeSource(pageData, "insurancePaymentsAmount", "Insurance payments");
			processUnearnedIncomeSource(pageData, "trustMoneyAmount", "Trust money");
			processUnearnedIncomeSource(pageData, "rentalIncomeAmount", "Rental income");
			processUnearnedIncomeSource(pageData, "interestDividendsAmount", "Interest or dividends");
			processUnearnedIncomeSource(pageData, "healthCareReimbursementAmount", "Healthcare reimbursement");
			processUnearnedIncomeSource(pageData, "contractForDeedAmount", "Contract for Deed");
			processUnearnedIncomeSource(pageData, "benefitsAmount", "Benefits programs");
			processUnearnedIncomeSource(pageData, "otherPaymentsAmount", "Other payments");
		}
	}

	private void processUnearnedIncomeSource(PageData pageData, String amountKey, String description) {
		InputData inputData = pageData.get(amountKey);
		if (!inputData.getValue().isEmpty()) {
			String amount = inputData.getValue(0);
			Person person = persons.get(0);
			person.unearnedIncomeItems.add(new UnearnedIncomeItem(description, amount));
		}
	}

	private String composeApplicantName() {
		String person1FirstName = getFirstValue(pagesData, PERSONAL_INFO_FIRST_NAME);
		String person1LastName = getFirstValue(pagesData, PERSONAL_INFO_LAST_NAME);
		return String.format("%s %s", person1FirstName, person1LastName);
	}

	private boolean mapNoUnearnedIncome() {
		boolean hasNoUnearnedIncome = true;
		String unearnedIncomeChoice = getFirstValue(pagesData, UNEARNED_INCOME);
		if (unearnedIncomeChoice != null) {
			if (!unearnedIncomeChoice.equals("NO_UNEARNED_INCOME_SELECTED")) {
				hasNoUnearnedIncome = false;
			}
		}
		boolean hasNoOtherUnearnedIncome = true;
		String otherUnearnedIncomeChoice = getFirstValue(pagesData, UNEARNED_INCOME_OTHER);
		if (otherUnearnedIncomeChoice != null) {
			if (!otherUnearnedIncomeChoice.equals("NO_OTHER_UNEARNED_INCOME_SELECTED")) {
				hasNoOtherUnearnedIncome = false;
			}
		}
		certainPopsDocumentFields.add(new DocumentField("certainPopsUnearnedIncome", "noCertainPopsUnearnedIncome",
				String.valueOf(!(hasNoUnearnedIncome && hasNoOtherUnearnedIncome)), ENUMERATED_SINGLE_VALUE));

		return hasNoUnearnedIncome && hasNoOtherUnearnedIncome;
	}

	// This internal class is used to keep track of one person who has one or more
	// unearned income items.
	public class Person {
		String fullName = "";
		String id = "";
		int personIndex = -1;
		ArrayList<UnearnedIncomeItem> unearnedIncomeItems = null;

		public Person(String fullName, String id, int personIndex) {
			this.fullName = fullName;
			this.id = id;
			this.personIndex = personIndex;
			this.unearnedIncomeItems = new ArrayList<UnearnedIncomeItem>();
		}
	}

	// This internal class is used to keep track of the details of one unearned
	// income item.
	public class UnearnedIncomeItem {
		String type = "";
		String amount = "";
		String frequency = "Monthly";

		public UnearnedIncomeItem(String type, String amount) {
			this.type = type;
			this.amount = amount;
		}
	}
	
	// Question 14, accounts.  Document has space for 3 lines, the rest is written to the supplement.
	private void mapBankAccountsFields(Application application, Document document, Recipient recipient) {
		var householdSize = application.getApplicationData().getApplicantAndHouseholdMemberSize();
		int lineNumber = 1;
		if (householdSize > 1) {
			lineNumber = createBankAccountFields(lineNumber, "savingsAccountSource", "savingsAccountSource", "Savings account");
			lineNumber = createBankAccountFields(lineNumber, "checkingAccountSource", "checkingAccountSource", "Checking account");
			lineNumber = createBankAccountFields(lineNumber, "moneyMarketSource", "moneyMarketSource", "Money market account");
			lineNumber = createBankAccountFields(lineNumber, "certOfDepositSource", "certOfDepositSource", "Certificate of deposit");
			certainPopsDocumentFields.add(new DocumentField("certainPopsBankAccounts", "hasCertainPopsBankAccounts",
					String.valueOf(lineNumber > 1), ENUMERATED_SINGLE_VALUE));
			if (lineNumber > 3) {
				supplementPageText = String.format("%s\n\n", supplementPageText);
			}
		} else {
			PageData pageData = pagesData.getPage("bankAccountTypes");
			if (pageData != null) {
				InputData inputData = pageData.get("bankAccountTypes");
				@NotNull
				List<String> accountTypes = inputData.getValue();
				if (accountTypes.size() > 0) {
					certainPopsDocumentFields.add(new DocumentField("certainPopsBankAccounts",
							"hasCertainPopsBankAccounts", "true", ENUMERATED_SINGLE_VALUE));
					String fullName = FullNameFormatter.getFullName(application);

					for (String accountType : accountTypes) {
						switch (accountType) {
						case "SAVINGS":
							lineNumber = createBankAccountFieldsForIndividual(lineNumber, fullName, "Savings account");
							break;
						case "CHECKING":
							lineNumber = createBankAccountFieldsForIndividual(lineNumber, fullName, "Checking account");
							break;
						case "MONEY_MARKET":
							lineNumber = createBankAccountFieldsForIndividual(lineNumber, fullName,	"Money market account");
							break;
						case "CERTIFICATE_OF_DEPOSIT":
							lineNumber = createBankAccountFieldsForIndividual(lineNumber, fullName,	"Certificate of deposit");
							break;
						}
					}
				}
			} else {
				certainPopsDocumentFields.add(new DocumentField("certainPopsBankAccounts", "hasCertainPopsBankAccounts",
						"false", ENUMERATED_SINGLE_VALUE));
			}
		}
	}
	
	private int createBankAccountFields(int lineNumber, String pageName, String pageAttributeName, String bankAccountType) {
			PageData pageData = pagesData.getPage(pageName);
		if (pageData != null) {
			InputData inputData = pageData.get(pageAttributeName);
			if (inputData != null) {
				List<String> personsNames = inputData.getValue();
				for (String name : personsNames) {
					name = name.substring(0,name.lastIndexOf(" "));  // strip off the id
					String ownerCoordinate = String.format("certainPopsBankAccountOwnerLine_%d", lineNumber);
					String typeCoordinate = String.format("certainPopsBankAccountTypeLine_%d", lineNumber);
					if (lineNumber < 4) {
						certainPopsDocumentFields.add(new DocumentField("certainPopsBankAccounts", ownerCoordinate, name, ENUMERATED_SINGLE_VALUE));
						certainPopsDocumentFields.add(new DocumentField("certainPopsBankAccounts", typeCoordinate, bankAccountType, ENUMERATED_SINGLE_VALUE));
					}

				    if (lineNumber == 4) {
				          needsSupplementPage = true;
				          supplementPageText = String.format("%s\n\n", supplementPageText);
				          supplementPageText = String.format("%sQUESTION 14 continued:", supplementPageText);
				    }
				    if (lineNumber >= 4) {
				          supplementPageText = String.format("%s\n%d) Owner name: %s, Type of account: %s", supplementPageText, lineNumber, name, bankAccountType);
				    }
					
					lineNumber++;
				}
			}
		}

		return lineNumber;
	}
	
	private int createBankAccountFieldsForIndividual(int lineNumber, String fullName, String bankAccountType) {
		String ownerCoordinate = String.format("certainPopsBankAccountOwnerLine_%d", lineNumber);
		String typeCoordinate = String.format("certainPopsBankAccountTypeLine_%d", lineNumber);
		if (lineNumber < 4) {
			certainPopsDocumentFields.add(new DocumentField("certainPopsBankAccounts", ownerCoordinate, fullName, ENUMERATED_SINGLE_VALUE));
			certainPopsDocumentFields.add(new DocumentField("certainPopsBankAccounts", typeCoordinate, bankAccountType,	ENUMERATED_SINGLE_VALUE));
		}

		if (lineNumber == 4) {
			needsSupplementPage = true;
			supplementPageText = String.format("%s\n\n", supplementPageText);
			supplementPageText = String.format("%sQUESTION 14 continued:", supplementPageText);
		}
		if (lineNumber >= 4) {
			supplementPageText = String.format("%s\n%d) Owner name: %s, Type of account: %s", supplementPageText,
					lineNumber, fullName, bankAccountType);
		}

		lineNumber++;
		return lineNumber;
	}

	
	// Question 15, investment types
	private void mapInvestmentType(Application application, Document document, Recipient recipient) {
	  InvestmentOwnerPreparer iop = new InvestmentOwnerPreparer();
	  List<Investment> investmentOwnerList = iop.getInvestmentOwners(application, document, recipient);
	  if(investmentOwnerList.size()>3) {
        needsSupplementPage = true;
        supplementPageText = String.format("%s\n\n", supplementPageText);
        supplementPageText = String.format("%sQUESTION 15 continued:", supplementPageText);
        int i = 3;
        for (Investment inv : investmentOwnerList.subList(3, investmentOwnerList.size())) {
          supplementPageText = String.format("%s\nPerson %d: %s, Investment Type: %s",
              supplementPageText, i + 1, inv.fullName,
              inv.investmentType.stream().map(Object::toString).collect(Collectors.joining(", ")));
          i++;
        }
	  }

	}
	
	private void mapRetroactiveCoverage(Application application, Document document, Recipient recipient) {
	  ListRetroCoveragePreparer lrcp = new ListRetroCoveragePreparer();
      List<RetroCoverageMember> retroCoverageMemberList = lrcp.getRetroactiveMembers(application, document, recipient);
      if(retroCoverageMemberList.size()>2) {
        needsSupplementPage = true;
        supplementPageText = String.format("%s\n\n", supplementPageText);
        supplementPageText = String.format("%sQUESTION 8 continued:", supplementPageText);
        int i = 2;
        for (RetroCoverageMember inv : retroCoverageMemberList.subList(2, retroCoverageMemberList.size())) {
          supplementPageText = String.format("%s\nPerson %d: %s, Month/s: %s",
              supplementPageText, i + 1, inv.fullName, inv.month);
          i++;
        }
      }

    }
	
}
