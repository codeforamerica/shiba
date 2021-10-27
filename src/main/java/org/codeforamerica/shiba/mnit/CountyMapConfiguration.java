package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.County.*;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination.CountyRoutingDestinationBuilder;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SuppressWarnings("DuplicatedCode")
@Configuration
public class CountyMapConfiguration {

  private CountyMap<CountyRoutingDestination> countyMap;

  @Bean
  @Profile({"default", "test"})
  CountyMap<CountyRoutingDestination> localMapping() {
    initializeDefaultCountyMap();
    countyMap.setDefaultValue(countyMap.get(Hennepin));
    return countyMap;
  }

  @Bean
  @Profile({"staging", "demo"})
  CountyMap<CountyRoutingDestination> demoMapping() {
    initializeDefaultCountyMap();
    addCounty(Anoka,
        "763-422-7200",
        "help+staging@mnbenefits.org",
        "f7c1c89c-34e1-4025-921c-f98ff385ea5a",
        "1023250115");
    addCounty(Carver,
        "952-361-1660",
        "help+staging@mnbenefits.org",
        "58e2bd12-b96e-45a0-a1e8-e3e8ad009fe6",
        "A000010800");
    addCounty(Clay,
        "218-299-5200",
        "help+staging@mnbenefits.org",
        "fdb3083f-ec76-45bf-a775-36a45b30974e",
        "A000014100");
    addCounty(Cook,
        "218-387-3620",
        "help+staging@mnbenefits.org",
        "d9eb7b20-7ab3-48a6-a62d-5f99a5b783bb",
        "A000016700");
    addCounty(Dodge,
        "507-431-5600",
        "help+staging@mnbenefits.org",
        "3434ceda-f829-47b0-adf7-db31a0c705bd",
        "A000020500");
    addCounty(Hennepin,
        "612-596-1300",
        "help+staging@mnbenefits.org",
        "45ed1eea-d045-48fe-9970-383e1b889ec5",
        "A000027200",
        "100 S 1st St", "Minneapolis", "55401");
    addCounty(Morrison,
        "320-631-3599",
        "help+staging@mnbenefits.org",
        "06ac8b00-ce5e-4639-86ca-6aeeb4e29e99",
        "1255406286");
    addCounty(Olmsted,
        "507-328-6500",
        "help+staging@mnbenefits.org",
        "47c53627-123e-4c74-a5c0-42f1668a5266",
        "A000055800");
    addCounty(OtterTail,
        "218-998-8281",
        "help+staging@mnbenefits.org",
        "ffb34634-ff95-4b9a-9e44-9f38b2865e79",
        "A000056600");
    addCounty(Sherburne,
        "763-765-4000",
        "help+staging@mnbenefits.org",
        "4cdb160a-3e22-4039-a8b0-301089a30570",
        "1447381660");
    addCounty(Steele,
        "507-431-5600",
        "help+staging@mnbenefits.org",
        "c103fb77-dae0-4d81-b689-0dd255136a64",
        "A000074400");
    addCounty(StLouis,
        "800-450-9777 or 218-726-2101",
        "help+staging@mnbenefits.org",
        "dd6389b4-3292-4713-9b9e-d6444e721551",
        "A000069800");
    addCounty(Wabasha,
        "651-565-3351",
        "help+staging@mnbenefits.org",
        "fbbd1e32-e3da-4f4f-9f74-e709e5da53ef",
        "A000079500");
    addCounty(Wadena,
        "218-631-7605",
        "help+staging@mnbenefits.org",
        "5b0cc12d-a9c0-4f10-9e51-37e4e1d32e1f",
        "A000080900");
    addCounty(Waseca,
        "507-431-5600",
        "help+staging@mnbenefits.org",
        "ed3aaedd-d315-43b5-8c31-c5c762d69d09",
        "A000081700");
    addCounty(Wright,
        "763-682-7400",
        "help+staging@mnbenefits.org",
        "c4aea212-5139-4323-a28d-fbd97f80540c",
        "1124197249");
    countyMap.setDefaultValue(countyMap.get(Hennepin));
    return countyMap;
  }

  @Bean
  @Profile("production")
  CountyMap<CountyRoutingDestination> productionMapping() {
    initializeDefaultCountyMap();
    addCounty(Anoka,
        "763-422-7200",
        "EADocs@co.anoka.mn.us",
        "2daf63e7-64f7-4c59-9a1e-4da18cfbeb10",
        "1023250115");
    addCounty(Carver,
        "952-361-1660",
        "cssfinancial@co.carver.mn.us",
        "1882e083-60e7-467a-ae66-ae0de945ad8e",
        "A000010800");
    addCounty(Clay,
        "218-299-5200",
        "social.services@co.clay.mn.us",
        "c0e972ab-c0f3-4172-9c75-ea593f912e6e",
        "A000014100");
    addCounty(Cook,
        "218-387-3620",
        "IM@co.cook.mn.us",
        "00ba833d-41ff-49e9-815a-e1f3f82826e4",
        "A000016700");
    addCounty(Dodge,
        "507-431-5600",
        "OSS@MNPrairie.org",
        "4d5d9570-33ed-4511-8792-dde6711dde36",
        "A000020500");
    addCounty(Hennepin,
        "612-596-1300",
        "hhsews@hennepin.us",
        "39e0ea57-cc12-4cc1-a074-0ad3d7216d01",
        "A000027200",
        "100 S 1st St", "Minneapolis", "55401");
    addCounty(Morrison,
        "320-631-3599",
        "callcenter@co.morrison.mn.us",
        "ad862c99-4292-4674-a7cd-2dc019eb464e",
        "1255406286");
    addCounty(Olmsted,
        "507-328-6500",
        "PAQ@co.olmsted.mn.us",
        "0dcade2f-4c2a-4450-8347-5888c9f966fb",
        "A000055800");
    addCounty(OtterTail,
        "218-998-8281",
        "imques@co.ottertail.mn.us",
        "77ba1a64-524d-492a-892e-09243ca4b9ab",
        "A000056600");
    addCounty(Sherburne,
        "763-765-4000",
        "PADocs@co.sherburne.mn.us",
        "7587e3c7-5409-4caf-b3c0-e4864f8b09b6",
        "1447381660");
    addCounty(Steele,
        "507-431-5600",
        "OSS@MNPrairie.org",
        "2e0023fa-2ffb-4582-a28f-495f7606b41f",
        "A000074400");
    addCounty(StLouis,
        "800-450-9777 or 218-726-2101",
        "ESS@stlouiscountymn.gov",
        "8e50a17a-6f8e-43bd-9de1-da4dff40a478",
        "A000069800");
    addCounty(Wabasha,
        "651-565-3351",
        "imuinterview@co.wabasha.mn.us",
        "4b087cdb-ccee-44ad-b461-e5a829988b01",
        "A000079500");
    addCounty(Wadena,
        "218-631-7605",
        "wchs.benefits@co.wadena.mn.us",
        "8d10df90-416f-46e7-80e1-e5f3bf02ae73",
        "A000080900");
    addCounty(Waseca,
        "507-431-5600",
        "OSS@MNPrairie.org",
        "f8fcee94-ae6e-4a1c-9b97-1e1bcf683249",
        "A000081700");
    addCounty(Wright,
        "763-682-7400",
        "HSFSPrograms@co.wright.mn.us",
        "644cae25-1cd5-481a-8f71-19446b83d3e1",
        "1124197249");
    countyMap.setDefaultValue(countyMap.get(Hennepin));
    return countyMap;
  }

  private void initializeDefaultCountyMap() {
    countyMap = new CountyMap<>();
    addCounty(Aitkin, "218-927-7200");
    addCounty(Anoka,
        "763-422-7200",
        "help+dev@mnbenefits.org",
        "05042a59-cb59-4b52-8bd1-8dd73e4e7e3f",
        "1023250115"
    );
    addCounty(Becker, "218-847-5628");
    addCounty(Beltrami, "218-333-8300");
    addCounty(Benton, "320-968-5087");
    addCounty(BigStone, "320-839-2555");
    addCounty(BlueEarth, "507-304-4335");
    addCounty(Brown, "507-354-8246");
    addCounty(Carlton, "218-879-4583");
    addCounty(Carver, "952-361-1660",
        "help+dev@mnbenefits.org",
        "f2002ee6-5201-432c-970e-fc152569dc10",
        "A000010800"
    );
    addCounty(Cass, "218-547-1340");
    addCounty(Chippewa, "320-269-6401");
    addCounty(Chisago, "651-213-5640");
    addCounty(Clay, "218-299-5200",
        "help+dev@mnbenefits.org",
        "a4a01aa6-2d9b-4510-9bbf-dc4c00442060",
        "A000014100"
    );
    addCounty(Clearwater, "218-694-6164");
    addCounty(Cook,
        "218-387-3620",
        "help+dev@mnbenefits.org",
        "057f1c2f-3a40-4e53-a208-ecfbe9a0022e",
        "A000016700");
    addCounty(Cottonwood, "507-831-1891");
    addCounty(CrowWing, "218-824-1250");
    addCounty(Dakota, "651-554-5611");
    addCounty(Dodge,
        "507-431-5600",
        "help+dev@mnbenefits.org",
        "aceeedc9-f094-4fa3-9665-8313674b61d0",
        "A000020500");
    addCounty(Douglas, "320-762-2302");
    addCounty(Faribault, "507-526-3265");
    addCounty(Fillmore, "507-765-2175");
    addCounty(Freeborn, "507-377-5400");
    addCounty(Goodhue, "651-385-320");
    addCounty(Grant, "218-685-8200");
    addCounty(Hennepin,
        "612-596-1300",
        "help+dev@mnbenefits.org",
        "5195b061-9bdc-4d31-9840-90a99902d329",
        "A000027200",
        "100 S 1st St", "Minneapolis", "55401");
    addCounty(Houston, "507-725-5811");
    addCounty(Hubbard, "218-732-1451");
    addCounty(Isanti, "763-689-1711");
    addCounty(Itasca, "218-327-2941");
    addCounty(Jackson, "507-847-4000");
    addCounty(Kanabec, "320-679-6350");
    addCounty(Kandiyohi, "320-231-7800");
    addCounty(Kittson, "218-843-2689");
    addCounty(Koochiching, "218-283-7000");
    addCounty(LacQuiParle, "320-598-7594");
    addCounty(Lake, "218-834-8400");
    addCounty(LakeOfTheWoods, "218-634-2642");
    addCounty(LeSueur, "507-357-8288");
    addCounty(Lincoln, "507-694-1452");
    addCounty(Lyon, "507-537-6747");
    addCounty(McLeod, "320-864-3144");
    addCounty(Mahnomen, "218-935-2568");
    addCounty(Marshall, "218-745-5124");
    addCounty(Martin, "507-238-4757");
    addCounty(Meeker, "320-693-5300");
    addCounty(MilleLacs, "320-983-8208");
    addCounty(Morrison,
        "320-631-3599",
        "help+dev@mnbenefits.org",
        "d0408807-e6a0-48d6-9829-2b78ceb94654",
        "1255406286");
    addCounty(Mower, "507-437-9700");
    addCounty(Murray, "507-836-6144");
    addCounty(Nicollet, "507-934-8559");
    addCounty(Nobles, "507-295-5213");
    addCounty(Norman, "218-784-5400");
    addCounty(Olmsted,
        "507-328-6500",
        "help+dev@mnbenefits.org",
        "6875aa2f-8852-426f-a618-d394b9a32be5",
        "A000055800");
    addCounty(OtterTail,
        "218-998-8281",
        "help+dev@mnbenefits.org",
        "4ceb43cf-e67a-4edb-9ff8-6ddff1911ff9",
        "A000056600");
    addCounty(Pennington, "218-681-2880");
    addCounty(Pine, "320-591-1570");
    addCounty(Pipestone, "507-825-6720");
    addCounty(Polk, "218-281-3127");
    addCounty(Pope, "320-634-7755");
    addCounty(Ramsey, "651-266-4444");
    addCounty(RedLake, "218-253-4131");
    addCounty(Redwood, "507-637-4050");
    addCounty(Renville, "320-523-2202");
    addCounty(Rice, "507-332-6115");
    addCounty(Rock, "507-283-5070");
    addCounty(Roseau, "218-463-2411");
    addCounty(Scott, "952-496-8686");
    addCounty(Sherburne,
        "763-765-4000",
        "help+dev@mnbenefits.org",
        "2f430ec0-5113-447a-af8a-4c972ac29de4",
        "1447381660");
    addCounty(Sibley, "507-237-4000");
    addCounty(Stearns, "320-656-6000");
    addCounty(Steele,
        "507-431-5600",
        "help+dev@mnbenefits.org",
        "dbe6702b-fd3b-4e6f-847e-7d3a41f2941a",
        "A000074400");
    addCounty(Stevens, "320-208-6600");
    addCounty(StLouis,
        "800-450-9777 or 218-726-2101",
        "help+dev@mnbenefits.org",
        "e1be079d-99cf-4efb-b7c2-3f92911dc992",
        "A000069800");
    addCounty(Swift, "320-843-3160");
    addCounty(Todd, "320-732-4500");
    addCounty(Traverse, "320-422-7777");
    addCounty(Wabasha,
        "651-565-3351",
        "help+dev@mnbenefits.org",
        "88c8dc91-20e0-449b-8cac-6aad788d0f4e",
        "A000079500");
    addCounty(Wadena,
        "218-631-7605",
        "help+dev@mnbenefits.org",
        "62dbc104-3e2f-4418-8a93-fd4f892a5152",
        "A000080900");
    addCounty(Waseca,
        "507-431-5600",
        "help+dev@mnbenefits.org",
        "94534f77-8f05-4e7e-8204-2e09bc6b873d",
        "A000081700");
    addCounty(Washington, "651-430-6455");
    addCounty(Watonwan, "507-375-3294");
    addCounty(Wilkin, "218-643-7161");
    addCounty(Winona, "507-457-6200");
    addCounty(Wright,
        "763-682-7400",
        "help+dev@mnbenefits.org",
        "359cf4ee-7147-4c3c-bfc0-f29213b68fc0",
        "1124197249");
    addCounty(YellowMedicine, "320-564-2211");
  }

  private void addCounty(County county, String phoneNumber, String email, String folderId,
      String dhsProviderId, String mailingStreetAddress, String mailingCity,
      String mailingZipcode) {
    addCounty(county, CountyRoutingDestination.builder()
        .email(email)
        .phoneNumber(phoneNumber)
        .folderId(folderId).dhsProviderId(dhsProviderId)
        .postOfficeAddress(
            new Address(mailingStreetAddress, mailingCity, "MN", mailingZipcode, "",
                county.displayName())));
  }

  private void addCounty(County county, String phoneNumber, String email, String folderId,
      String dhsProviderId) {
    addCounty(county, CountyRoutingDestination.builder().email(email)
        .phoneNumber(phoneNumber).folderId(folderId)
        .dhsProviderId(dhsProviderId));
  }

  private void addCounty(County county, String phoneNumber) {
    addCounty(county, CountyRoutingDestination.builder().phoneNumber(phoneNumber));
  }

  private void addCounty(County county,
      CountyRoutingDestinationBuilder builder) {
    countyMap.getCounties().put(county, builder.county(county).build());
  }
}
