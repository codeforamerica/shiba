package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class InvestmentOwnerPreparerTest {

  InvestmentOwnerPreparer preparer = new InvestmentOwnerPreparer();
  TestApplicationDataBuilder applicationDataTest = new TestApplicationDataBuilder();

  @Test
  void preparesFieldsForEveryoneInHouseForInvestmentOwners() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("addHouseholdMembers","addHouseholdMembers","true")
        .withPageData("assets", "assets", List.of("STOCK_BOND"))
        .withPageData("investmentAssetType", "investmentAssetType", List.of("STOCKS", "BONDS", "RETIREMENT_ACCOUNTS"))
        .withPageData("stocksHouseHoldSource", "stocksHouseHoldSource", List.of(
            "Daria Agàta someGuid",
            "Jane Doe applicant",
            "Other Person notSpouse"))
        .withPageData("bondsHouseHoldSource", "bondsHouseHoldSource", List.of(
            "Daria Agàta someGuid",
            "Jane Doe applicant",
            "Other Person notSpouse"))
        .withPageData("retirementAccountsHouseHoldSource", "retirementAccountsHouseHoldSource", List.of(
            "Daria Agàta someGuid",
            "Jane Doe applicant",
            "Other Person notSpouse"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "assetOwnerSource",
            "investmentOwners",
            List.of("Jane Doe"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentType",
            List.of("stocks, bonds, retirement accounts"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentOwners",
            List.of("Other Person"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentType",
            List.of("stocks, bonds, retirement accounts"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentOwners",
            List.of("Daria Agàta"),
            DocumentFieldType.SINGLE_VALUE,
            2
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentType",
            List.of("stocks, bonds, retirement accounts"),
            DocumentFieldType.SINGLE_VALUE,
            2
        )));
  }
  
  @Test
  void preparesFieldsForApplicantOnlyInIndivialFlow() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withPageData("addHouseholdMembers","addHouseholdMembers","false")
        .withPageData("assets", "assets", List.of("STOCK_BOND"))
        .withPageData("investmentTypesIndividual", "investmentTypes", List.of("STOCKS", "BONDS", "RETIREMENT_ACCOUNTS"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "assetOwnerSource",
            "investmentOwners",
            List.of("Jane Doe"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentType",
            List.of("stocks, bonds, retirement accounts"),
            DocumentFieldType.SINGLE_VALUE,
            0
        )));
  }

  @Test
  void preparesNoFieldsIfAssetsIsNone() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withPageData("addHouseholdMembers","addHouseholdMembers","true")
        .withMultipleHouseholdMembers()
        .withPageData("assets", "assets", List.of("NONE"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of());
  }
  
  @Test
  void preparesFieldsForInvestmentOwnerSelectedHouseHoldOnly() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("addHouseholdMembers","addHouseholdMembers","true")
        .withPageData("assets", "assets", List.of("STOCK_BOND"))
        .withPageData("investmentAssetType", "investmentAssetType", List.of("STOCKS", "BONDS", "RETIREMENT_ACCOUNTS"))
        .withPageData("stocksHouseHoldSource", "stocksHouseHoldSource", List.of(
            "Jane Doe applicant",
            "Other Person notSpouse"))
        .withPageData("bondsHouseHoldSource", "bondsHouseHoldSource", List.of(
            "Daria Agàta someGuid",
            "Jane Doe applicant"))
        .withPageData("retirementAccountsHouseHoldSource", "retirementAccountsHouseHoldSource", List.of(
            "Daria Agàta someGuid",
            "Jane Doe applicant",
            "Other Person notSpouse"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "assetOwnerSource",
            "investmentOwners",
            List.of("Jane Doe"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentType",
            List.of("stocks, bonds, retirement accounts"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentOwners",
            List.of("Other Person"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentType",
            List.of("stocks, retirement accounts"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentOwners",
            List.of("Daria Agàta"),
            DocumentFieldType.SINGLE_VALUE,
            2
        ),
        new DocumentField(
            "assetOwnerSource",
            "investmentType",
            List.of("bonds, retirement accounts"),
            DocumentFieldType.SINGLE_VALUE,
            2
        )));
  }
}
