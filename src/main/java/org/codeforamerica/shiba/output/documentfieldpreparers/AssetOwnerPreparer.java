package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.FullNameFormatter.getFullName;
import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class AssetOwnerPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> results = new ArrayList<>();
    boolean hasHouseHold = application.getApplicationData().getPageData("addHouseholdMembers").get("addHouseholdMembers").getValue().contains("true");
    List<DocumentField> stockOwners = getAssetsOwnerSection(application, "stockAssetSource", "stockAssetSource","stockOwners", hasHouseHold, "STOCK_BOND" );
    results.addAll(stockOwners);
    List<DocumentField> vechicleOwners = getAssetsOwnerSection(application, "vehicleAssetSource", "vehicleAssetSource","vehicleOwners", hasHouseHold, "VEHICLE" );
    results.addAll(vechicleOwners);
    List<DocumentField> lifeInsuranceOwners = getAssetsOwnerSection(application, "lifeInsuranceAssetSource", "lifeInsuranceAssetSource","lifeInsuranceOwners", hasHouseHold, "LIFE_INSURANCE" );
    results.addAll(lifeInsuranceOwners);
    List<DocumentField> burialAccountOwners = getAssetsOwnerSection(application, "burialAccountAssetSource", "burialAccountAssetSource","burialAccountOwners", hasHouseHold, "BURIAL_ACCOUNT" );
    results.addAll(burialAccountOwners);
    List<DocumentField> businessOwnershipOwners = getAssetsOwnerSection(application, "businessOwnershipAssetSource", "businessOwnershipAssetSource","businessOwnershipOwners", hasHouseHold, "OWNERSHIP_BUSINESS" );
    results.addAll(businessOwnershipOwners);
    List<DocumentField> realEstateOwners = getAssetsOwnerSection(application, "realEstateAssetSource", "realEstateAssetSource","realEstateOwners", hasHouseHold, "REAL_ESTATE" );
    results.addAll(realEstateOwners);
    return results;
  }
  
  @NotNull
  private static List<DocumentField> getAssetsOwnerSection(Application application, String pageName,
      String inputName, String outputName, boolean hasHouseHold, String assetType) {
    List<String> assetOwnersSource =
        getListOfSelectedFullNames(application, pageName, inputName);
    List<DocumentField> fields = new ArrayList<>();
    AtomicInteger i = new AtomicInteger(0);
    if (hasHouseHold) {
      fields = assetOwnersSource
          .stream().map(fullName -> new DocumentField("assetOwnerSource", outputName,
              List.of(fullName), DocumentFieldType.SINGLE_VALUE, i.getAndIncrement()))
          .collect(Collectors.toList());
    } else {
      if (application.getApplicationData().getPageData("assets").get("assets").getValue()
          .contains(assetType))
        fields.add(
            new DocumentField("assetOwnerSource", outputName, List.of(getFullName(application)),
                DocumentFieldType.SINGLE_VALUE, i.getAndIncrement()));
    }
    return fields;
  }
}
