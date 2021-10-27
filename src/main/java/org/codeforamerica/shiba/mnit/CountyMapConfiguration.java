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

    addCounty(Aitkin,
        "800-328-3744",
        "help+staging@mnbenefits.org",
        "f0beb4fc-10be-4840-97e2-6739866d56be",
        "A000001900");
    addCounty(Anoka,
        "763-422-7200",
        "help+staging@mnbenefits.org",
        "f7c1c89c-34e1-4025-921c-f98ff385ea5a",
        "1023250115");
    addCounty(Becker,
        "218-847-5628",
        "help+staging@mnbenefits.org",
        "7346fec1-bef6-4b07-bf4e-78c91e7becd5",
        "A000003500");
    addCounty(Beltrami,
        "218-333-8300",
        "help+staging@mnbenefits.org",
        "67518013-afce-43cf-b9e8-d7d5bc91fe2f",
        "A000004300");
    addCounty(Benton,
        "800-530-6254",
        "help+staging@mnbenefits.org",
        "89cd5377-3971-47ce-aafd-a8bff39b038c",
        "1174697148");
    addCounty(BigStone,
        "320-839-2555",
        "help+staging@mnbenefits.org",
        "d816052d-a864-4b25-b52a-78ffb7acf0c8",
        "A000006000");
    addCounty(BlueEarth,
        "507-304-4335",
        "help+staging@mnbenefits.org",
        "7ec26423-dc27-488a-9a19-6ea1390f59a5",
        "A000007800");
    addCounty(Brown,
        "800-450-8246",
        "help+staging@mnbenefits.org",
        "92938bef-bbb4-41ac-bad7-0a9dedcfd72e",
        "A000008600");
    addCounty(Carlton,
        "800-642-9082",
        "help+staging@mnbenefits.org",
        "7747065e-445e-4504-982d-08fd28a9c99a",
        "1003921875");
    addCounty(Carver,
        "952-361-1600",
        "help+staging@mnbenefits.org",
        "58e2bd12-b96e-45a0-a1e8-e3e8ad009fe6",
        "A000010800");
    addCounty(Cass,
        "218-547-1340",
        "help+staging@mnbenefits.org",
        "7f8a1f8d-e485-44fb-895e-9939e06ceea1",
        "1760489769");
    addCounty(Chippewa,
        "320-269-6401",
        "help+staging@mnbenefits.org",
        "4124d5ee-b30e-4cd3-94df-4b9ce11975fd",
        "A000012400");
    addCounty(Chisago,
        "888-234-1246",
        "help+staging@mnbenefits.org",
        "44ba95a8-4c71-45a3-8fe5-c17aeab45488",
        "1659408904");
    addCounty(Clay,
        "800-757-3880",
        "help+staging@mnbenefits.org",
        "fdb3083f-ec76-45bf-a775-36a45b30974e",
        "A000014100");
    addCounty(Clearwater,
        "800-245-6064",
        "help+staging@mnbenefits.org",
        "c8fe2d4d-826c-4986-8c69-c129b055224e",
        "A000015900");
    addCounty(Cook,
        "218-387-3620",
        "help+staging@mnbenefits.org",
        "d9eb7b20-7ab3-48a6-a62d-5f99a5b783bb",
        "A000016700");
    addCounty(Cottonwood,
        "507-831-1891",
        "help+staging@mnbenefits.org",
        "d6749ae0-4a41-491d-86a2-d5e35ec60030",
        "A000017500");
    addCounty(CrowWing,
        "888-772-8212",
        "help+staging@mnbenefits.org",
        "8f51d9ed-f3bc-4a15-9c46-60103c026ead",
        "A000018300");
    addCounty(Dakota,
        "888-850-9419",
        "help+staging@mnbenefits.org",
        "98e9e88f-9e92-412e-a861-ef2c0a1613ef",
        "1427127620");
    addCounty(Dodge,
        "507-923-2900",
        "help+staging@mnbenefits.org",
        "3434ceda-f829-47b0-adf7-db31a0c705bd",
        "A000020500");
    addCounty(Douglas,
        "320-762-2302",
        "help+staging@mnbenefits.org",
        "d3e2e3b0-312d-4767-98d5-67bf6d4ed382",
        "A000021300");
    addCounty(Faribault,
        "507-526-3265",
        "help+staging@mnbenefits.org",
        "aaee184d-f5c1-46ff-bffa-73185e3689a3",
        "A000022100");
    addCounty(Fillmore,
        "507-765-2175",
        "help+staging@mnbenefits.org",
        "32d43818-107a-4071-9349-04fc1cc561bb",
        "1437228236");
    addCounty(Freeborn,
        "507-377-5400",
        "help+staging@mnbenefits.org",
        "fc9bbdb6-ea7c-4fbc-997a-d40fd8aaf3ce",
        "A000024800");
    addCounty(Goodhue,
        "651-385-3200",
        "help+staging@mnbenefits.org",
        "c3a52682-8baf-46cd-ae04-c8993b435b26",
        "A000025600");
    addCounty(Grant,
        "800-291-2827",
        "help+staging@mnbenefits.org",
        "cbd936a5-b2e5-4e4f-8dc1-a4bcc29da43c",
        "1225119381");
    addCounty(Hennepin,
        "612-596-1300",
        "help+staging@mnbenefits.org",
        "45ed1eea-d045-48fe-9970-383e1b889ec5",
        "A000027200",
        "100 S 1st St", "Minneapolis", "55401");
    addCounty(Houston,
        "507-725-5811",
        "help+staging@mnbenefits.org",
        "ea96e8e2-9ba1-4c64-b4ae-5194d2d4243c",
        "A000028100");
    addCounty(Hubbard,
        "877-450-1451",
        "help+staging@mnbenefits.org",
        "21d7ec7e-ca67-4bd6-ba22-dd9e42d22234",
        "A000029900");
    addCounty(Isanti,
        "763-689-1711",
        "help+staging@mnbenefits.org",
        "83594223-6aaa-42ff-94ec-dde1ddbe4d6d",
        "A000030200");
    addCounty(Itasca,
        "800-422-0312",
        "help+staging@mnbenefits.org",
        "f9c63d9c-412b-4809-af23-eb57761651d2",
        "A000031100");
    addCounty(Jackson,
        "507-847-4000",
        "help+staging@mnbenefits.org",
        "f5dbbab6-118b-4581-8e68-187f75ec3131",
        "A000032900");
    addCounty(Kanabec,
        "320-679-6350",
        "help+staging@mnbenefits.org",
        "6ed85045-92ae-4c3a-8cf2-f17303d0ee32",
        "1396819108");
    addCounty(Kandiyohi,
        "877-464-7800",
        "help+staging@mnbenefits.org",
        "25970b90-3df8-4c35-90c1-931bb461ca38",
        "A000034500");
    addCounty(Kittson,
        "800-672-8026",
        "help+staging@mnbenefits.org",
        "e42ba010-7923-45b0-a52c-4ead327c798d",
        "A000035300");
    addCounty(Koochiching,
        "800-950-4630",
        "help+staging@mnbenefits.org",
        "41a69131-a9e3-4b7a-b38d-842acd45aefc",
        "A000036100");
    addCounty(LacQuiParle,
        "320-598-7594",
        "help+staging@mnbenefits.org",
        "81989a6b-c1b3-444a-bba1-964b08fddfed",
        "A000037000");
    addCounty(Lake,
        "218-834-8400",
        "help+staging@mnbenefits.org",
        "53eed7dd-1c9f-480c-a649-02dfb1b2ff43",
        "A000038800");
    addCounty(LakeOfTheWoods,
        "218-634-2642",
        "help+staging@mnbenefits.org",
        "26d1e3f2-d38d-4b4d-925f-d0c417b99cd7",
        "A000039600");
    addCounty(LeSueur,
        "507-357-8288",
        "help+staging@mnbenefits.org",
        "cc3d485e-f8ee-4d82-82f8-c49dae68e9e5",
        "A000040000");
    addCounty(Lincoln,
        "800-657-3781",
        "help+staging@mnbenefits.org",
        "e5f9f5dc-074a-4231-9a51-78a750cf01b8",
        "A000041800");
    addCounty(Lyon,
        "800-657-3760",
        "help+staging@mnbenefits.org",
        "d6c7c690-8048-4921-9d52-4e9adc663a93",
        "A000042600");
    addCounty(McLeod,
        "800-247-1756",
        "help+staging@mnbenefits.org",
        "59f1fbfb-767f-4a23-a50c-b9c550d1f6be",
        "A000043400");
    addCounty(Mahnomen,
        "218-935-2568",
        "help+staging@mnbenefits.org",
        "fc4307e6-b570-4f77-888d-a8087799b441",
        "A000044200");
    addCounty(Marshall,
        "800-642-5444",
        "help+staging@mnbenefits.org",
        "9456a3d8-fddf-439e-84fb-8eee8cc3d38f",
        "A000045100");
    addCounty(Martin,
        "507-238-4757",
        "help+staging@mnbenefits.org",
        "8d893dcc-40d1-413a-9a7e-998ca55a6f55",
        "A000046900");
    addCounty(Meeker,
        "877-915-5300",
        "help+staging@mnbenefits.org",
        "10d0cca9-18de-4379-8293-ddda158c46a2",
        "A000047700");
    addCounty(MilleLacs,
        "888-270-8208",
        "help+staging@mnbenefits.org",
        "a0d9cae7-ed77-4dff-9bc3-9bd199751fdb",
        "A000048500");
    addCounty(Morrison,
        "320-631-3599",
        "help+staging@mnbenefits.org",
        "06ac8b00-ce5e-4639-86ca-6aeeb4e29e99",
        "1255406286");
    addCounty(Mower,
        "507-437-9700",
        "help+staging@mnbenefits.org",
        "3c2b9ef0-4571-4a83-b3eb-31f10cab813d",
        "M000050700");
    addCounty(Murray,
        "800-657-3811",
        "help+staging@mnbenefits.org",
        "e9de023b-af1f-4f61-aa4d-9b44f2e47aa5",
        "M000051500");
    addCounty(Nicollet,
        "507-934-8559",
        "help+staging@mnbenefits.org",
        "87bbe6af-4dce-4bc9-a350-f1c4ec8f8a7f",
        "M000052300");
    addCounty(Nobles,
        "507-295-5213",
        "help+staging@mnbenefits.org",
        "6db244a6-a0cd-4f81-abd8-a021cbc4b262",
        "M000053100");
    addCounty(Norman,
        "218-784-5400",
        "help+staging@mnbenefits.org",
        "84c8baa2-1e32-4ddc-b17f-2061dba38624",
        "A000054000");
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
    addCounty(Pennington,
        "218-681-2880",
        "help+staging@mnbenefits.org",
        "5592ce15-128f-4017-8b17-765a0167ab03",
        "A000057400");
    addCounty(Pine,
        "320-591-1570",
        "help+staging@mnbenefits.org",
        "f9c142b4-b85e-4af4-9289-c847bac60e6f",
        "A000058200");
    addCounty(Pipestone,
        "507-825-6720",
        "help+staging@mnbenefits.org",
        "cdc4444d-0d25-4794-9e58-5ba8e7fff6ba",
        "A000059100");
    addCounty(Polk,
        "877-281-3127",
        "help+staging@mnbenefits.org",
        "c4b70873-4077-405a-8dff-dd54a04ceebf",
        "A000060400");
    addCounty(Pope,
        "320-634-7755",
        "help+staging@mnbenefits.org",
        "abf2001a-7ed6-4a2f-af2f-0dfd07e47bb7",
        "A000061200");
    addCounty(Ramsey,
        "651-266-4444",
        "help+staging@mnbenefits.org",
        "4960b9e1-5691-4a20-9383-b88a96026ddc",
        "1811055957");
    addCounty(RedLake,
        "877-294-0846",
        "help+staging@mnbenefits.org",
        "dffda4ff-0b29-40e9-bd22-f01a28c261d8",
        "A000063900");
    addCounty(Redwood,
        "888-234-1292",
        "help+staging@mnbenefits.org",
        "c89e26f5-0f89-4275-ad9f-ca2b65138206",
        "A000064700");
    addCounty(Renville,
        "320-523-2202",
        "help+staging@mnbenefits.org",
        "1c642ab9-b835-434f-b7da-b89e86800870",
        "M000065500");
    addCounty(Rice,
        "507-332-6115",
        "help+staging@mnbenefits.org",
        "31133371-c44e-45fa-9ab9-984023e34689",
        "M000066300");
    addCounty(Rock,
        "507-283-5070",
        "help+staging@mnbenefits.org",
        "95bcb13c-13bf-47f2-b879-6b1b89ef093a",
        "M000067100");
    addCounty(Roseau,
        "866-255-2932",
        "help+staging@mnbenefits.org",
        "db6c0052-1b08-4c69-ab35-1bcea2e3f68a",
        "A000068000");
    addCounty(Scott,
        "952-496-8686",
        "help+staging@mnbenefits.org",
        "e8c868b1-58b0-4370-b92a-7f3b7b0ce011",
        "A000070100");
    addCounty(Sherburne,
        "800-433-5239",
        "help+staging@mnbenefits.org",
        "4cdb160a-3e22-4039-a8b0-301089a30570",
        "1447381660");
    addCounty(Sibley,
        "507-237-4000",
        "help+staging@mnbenefits.org",
        "86f61eed-ec69-428f-ba38-ee7f6b9fe908",
        "A000072800");
    addCounty(Stearns,
        "800-450-3663",
        "help+staging@mnbenefits.org",
        "7f5eb65f-0bac-449a-a5ba-a5d38054e00e",
        "A000073600");
    addCounty(Steele,
        "507-431-5600",
        "help+staging@mnbenefits.org",
        "c103fb77-dae0-4d81-b689-0dd255136a64",
        "A000074400");
    addCounty(Stevens,
        "800-950-4429",
        "help+staging@mnbenefits.org",
        "7072d5ee-c66c-4553-9d0a-31f26b7e3296",
        "A000075200");
    addCounty(StLouis,
        "800-450-9777 or 218-726-2101",
        "help+staging@mnbenefits.org",
        "dd6389b4-3292-4713-9b9e-d6444e721551",
        "A000069800");
    addCounty(Swift,
        "320-843-3160",
        "help+staging@mnbenefits.org",
        "31e5ce43-45fd-4a48-ae2b-46da6bf614ed",
        "A000076100");
    addCounty(Todd,
        "888-838-4066",
        "help+staging@mnbenefits.org",
        "528fb023-802e-47bc-b711-5ed99616f2bb",
        "1336372465");
    addCounty(Traverse,
        "855-735-8916",
        "help+staging@mnbenefits.org",
        "e75463bc-292d-4c64-a6ea-61fb562ca9fe",
        "A000078700");
    addCounty(Wabasha,
        "888-315-8815",
        "help+staging@mnbenefits.org",
        "fbbd1e32-e3da-4f4f-9f74-e709e5da53ef",
        "A000079500");
    addCounty(Wadena,
        "888-662-2737",
        "help+staging@mnbenefits.org",
        "5b0cc12d-a9c0-4f10-9e51-37e4e1d32e1f",
        "A000080900");
    addCounty(Waseca,
        "507-837-6600",
        "help+staging@mnbenefits.org",
        "ed3aaedd-d315-43b5-8c31-c5c762d69d09",
        "A000081700");
    addCounty(Washington,
        "651-430-6455",
        "help+staging@mnbenefits.org",
        "8f60cfb2-1796-4ec6-b041-a62722a6ef7d",
        "1700969334");
    addCounty(Watonwan,
        "888-299-5941",
        "help+staging@mnbenefits.org",
        "6256214c-3c37-4dd9-bb28-c3a7ebcef5d3",
        "1942539846");
    addCounty(Wilkin,
        "218-643-7161",
        "help+staging@mnbenefits.org",
        "b64ad5d3-2fbd-4418-bac8-db07e1e1d4f9",
        "1962567529");
    addCounty(Winona,
        "507-457-6200",
        "help+staging@mnbenefits.org",
        "6f2b101d-896f-44d4-a5e2-4afb707e7dd4",
        "A000085000");
    addCounty(Wright,
        "763-682-7400",
        "help+staging@mnbenefits.org",
        "c4aea212-5139-4323-a28d-fbd97f80540c",
        "1124197249");
    addCounty(YellowMedicine,
        "320-564-2211",
        "help+staging@mnbenefits.org",
        "231e5c4a-85e6-4ea1-8e59-7093d76b880e",
        "A000087600");
    countyMap.setDefaultValue(countyMap.get(Hennepin));
    return countyMap;
  }

  @Bean
  @Profile("production")
  CountyMap<CountyRoutingDestination> productionMapping() {
    initializeDefaultCountyMap();
    addCounty(Aitkin,
        "800-328-3744",
        "achhs@co.aitkin.mn.us",
        "7c1a4324-a65b-4977-b0dd-1ab8123ca63d",
        "A000001900");
    addCounty(Anoka,
        "763-422-7200",
        "EADocs@co.anoka.mn.us",
        "2daf63e7-64f7-4c59-9a1e-4da18cfbeb10",
        "1023250115");
    addCounty(Becker,
        "218-847-5628",
        "stacie@co.becker.mn.us ",
        "60a4cc7e-2186-42c6-b6b3-bd5957b66d36",
        "A000003500");
    addCounty(Beltrami,
        "218-333-8300",
        "BeltramiCoPubAssntOffice-DO-NOT-REPLY@co.beltrami.mn.us",
        "cb73c4f8-97f1-4812-9885-21668596ecfb",
        "A000004300");
    addCounty(Benton,
        "800-530-6254",
        "financial@co.benton.mn.us",
        "965f1616-132e-4b45-b1f4-d450da0ce785",
        "1174697148");
    addCounty(BigStone,
        "320-839-2555",
        "familyservicecenter@bigstonecounty.org ",
        "a81f65f8-54be-4306-be29-7029f6ec4e9d",
        "A000006000");
    addCounty(BlueEarth,
        "507-304-4335",
        "DeAnn.Boney@blueearthcountymn.gov ",
        "efae7dad-3a03-4ec7-8be1-d2e3ea3f850a",
        "A000007800");
    addCounty(Brown,
        "800-450-8246",
        "FrontDesk1@co.brown.mn",
        "3be208a9-078c-4ee1-a2c1-4c4c08d13b46",
        "A000008600");
    addCounty(Carlton,
        "800-642-9082",
        "patti.hart@co.carlton.mn.us",
        "6ec6bf88-2b44-4e34-b10b-c35635b9e948",
        "1003921875");
    addCounty(Carver,
        "952-361-1600",
        "cssfinancial@co.carver.mn.us",
        "1882e083-60e7-467a-ae66-ae0de945ad8e",
        "A000010800");
    addCounty(Cass,
        "218-547-1340",
        "cass.socserv@co.cass.mn.us",
        "ba4cc43c-87b3-4ce3-a434-f6ae13042883",
        "1760489769");
    addCounty(Chippewa,
        "320-269-6401",
        "nicole.draeger@co.nicollet.mn.us",
        "c03494f9-4646-4ff7-87cc-19836ed97fe7",
        "A000012400");
    addCounty(Chisago,
        "888-234-1246",
        "hhsim@chisagocounty.us ",
        "516563d9-d1c2-4112-b3d0-d515d089bafb",
        "1659408904");
    addCounty(Clay,
        "800-757-3880",
        "social.services@co.clay.mn.us",
        "c0e972ab-c0f3-4172-9c75-ea593f912e6e",
        "A000014100");
    addCounty(Clearwater,
        "800-245-6064",
        "samantha.coyle@co.clearwater.mn.us",
        "e743a0bc-5230-44c1-8bd5-a23a85f8fd80",
        "A000015900");
    addCounty(Cook,
        "218-387-3620",
        "IM@co.cook.mn.us",
        "00ba833d-41ff-49e9-815a-e1f3f82826e4",
        "A000016700");
    addCounty(Cottonwood,
        "507-831-1891",
        "angela.holmen@dvhhs.org",
        "df0bf5a2-5267-4fbd-9d91-211d86855d46",
        "A000017500");
    addCounty(CrowWing,
        "888-772-8212",
        "cwcss@crowwing.us ",
        "af92e7fd-a52e-48c9-85f3-d961c1745661",
        "A000018300");
    addCounty(Dakota,
        "888-850-9419",
        "eeadocs@co.dakota.mn.us",
        "cb1db222-a612-475a-9fc5-0a0b5466cb79",
        "1427127620");
    addCounty(Dodge,
        "507-923-2900",
        "OSS@MNPrairie.org",
        "4d5d9570-33ed-4511-8792-dde6711dde36",
        "A000020500");
    addCounty(Douglas,
        "320-762-2302",
        "dcss@co.douglass.mn.us",
        "f6f9ad77-3a2e-4c8f-aed7-194b5eae9c77",
        "A000021300");
    addCounty(Faribault,
        "507-526-3265",
        "Carol.becker@fmchs.com",
        "6bb1caab-bd7f-4bef-938c-47998ffd1eb8",
        "A000022100");
    addCounty(Fillmore,
        "507-765-2175",
        "SS-FAX@co.fillmore.mn.us ",
        "c0c52c0d-b3d7-4092-b134-89fa17e21715",
        "1437228236");
    addCounty(Freeborn,
        "507-377-5400",
        "im.dhs@co.freeborn.mn.us",
        "3000eeff-0e44-4fa9-bb01-75bd966f3019",
        "A000024800");
    addCounty(Goodhue,
        "651-385-3200",
        "hhs.imu@co.goodhue.mn.us",
        "3058aa16-eace-4b12-b4e8-7a792acab26a",
        "A000025600");
    addCounty(Grant,
        "800-291-2827",
        "front@co.grant.mn.us ",
        "6498cbd4-d283-40c2-b3ba-9fb9153546fb",
        "1225119381");
    addCounty(Hennepin,
        "612-596-1300",
        "hhsews@hennepin.us",
        "39e0ea57-cc12-4cc1-a074-0ad3d7216d01",
        "A000027200",
        "100 S 1st St", "Minneapolis", "55401");
    addCounty(Houston,
        "507-725-5811",
        "dhsinfo@co.houston.mn.us",
        "b9c2d258-8146-4a9d-a24e-fea32c35682d",
        "A000028100");
    addCounty(Hubbard,
        "877-450-1451",
        "beth.vredenburg@co.hubbard.mn.us",
        "9c572f51-4e0a-4947-b501-aa1c8e36dc35",
        "A000029900");
    addCounty(Isanti,
        "763-689-1711",
        "Jennifer.johnson@co.isanti.mn.us",
        "31ab351e-8288-46d4-bba9-f59401693bea",
        "A000030200");
    addCounty(Itasca,
        "800-422-0312",
        "FAUsupport@co.itasca.mn.us",
        "f489b6eb-0660-4358-8ef3-270cb40cf063",
        "A000031100");
    addCounty(Jackson,
        "507-847-4000",
        "financial@dvhhs.org ",
        "7a1f9364-3f2f-4fba-8449-c92796f58562",
        "A000032900");
    addCounty(Kanabec,
        "320-679-6350",
        "family.services@co.kanabec.mn.us",
        "be86d7e8-c1bb-4025-8b7e-84a3c22c77eb",
        "1396819108");
    addCounty(Kandiyohi,
        "877-464-7800",
        "Human.Services@kcmn.us",
        "57f87edb-c7d9-464a-acb1-c73930822add",
        "A000034500");
    addCounty(Kittson,
        "800-672-8026",
        "bseed@co.kittson.mn.us",
        "adfe885b-8f4a-4d23-ae25-f1ed1bcf7d90",
        "A000035300");
    addCounty(Koochiching,
        "800-950-4630",
        "valerie.long@co.koochiching.mn.us",
        "cf652a8b-132e-44a2-bbed-5eef7f2d97ad",
        "A000036100");
    addCounty(LacQuiParle,
        "320-598-7594",
        "familyservices@co.lac-qui-parle.mn.us",
        "8510a300-75c1-4625-a4d8-b075c2bcd4ff",
        "A000037000");
    addCounty(Lake,
        "218-834-8400",
        "financial.assistance@co.lake.mn.us ",
        "5ade0387-9a23-4d7d-b752-d6ba80539d1d",
        "A000038800");
    addCounty(LakeOfTheWoods,
        "218-634-2642",
        "cassondra_b@co.lotw.mn.us",
        "35b050a5-b690-49c9-977e-561c576315b2",
        "A000039600");
    addCounty(LeSueur,
        "507-357-8288",
        "IMDocs@co.le-sueur.mn.us",
        "9e3adfb8-06a9-4e7c-a0e7-5392d077506e",
        "A000040000");
    addCounty(Lincoln,
        "800-657-3781",
        "ssintake@swmhhs.com ",
        "8390e1e9-2860-441f-ad95-7d27605ff706",
        "A000041800");
    addCounty(Lyon,
        "800-657-3760",
        "ssintake@swmhhs.com ",
        "a045a4e2-bf25-40a4-b206-d42319049a7f",
        "A000042600");
    addCounty(McLeod,
        "800-247-1756",
        "mcleod.fw@co.mcleod.mn.us",
        "e67e0ebb-933c-439e-8b4e-ee0ddee7df76",
        "A000043400");
    addCounty(Mahnomen,
        "218-935-2568",
        "info@co.mahnomen.mn.us",
        "8972497d-15bb-4e09-a13a-c1a0b9044204",
        "A000044200");
    addCounty(Marshall,
        "800-642-5444",
        "sarah.noble@co.marshall.mn.us",
        "16ce4830-ccd7-462c-8f72-cfb17405e008",
        "A000045100");
    addCounty(Martin,
        "507-238-4757",
        "Carol.becker@fmchs.com",
        "d0a5d6b2-be53-4f7e-a927-c267ae0ed77e",
        "A000046900");
    addCounty(Meeker,
        "877-915-5300",
        "socserv.info@co.meeker.mn.us",
        "a10f19da-7829-4149-8a81-aa3fc4429441",
        "A000047700");
    addCounty(MilleLacs,
        "888-270-8208",
        "Beth.Sumner@millelacs.mn.gov>",
        "34c71473-445d-45b6-8b51-a1346f99b8ed",
        "A000048500");
    addCounty(Morrison,
        "800-269-1464",
        "callcenter@co.morrison.mn.us",
        "ad862c99-4292-4674-a7cd-2dc019eb464e",
        "1255406286");
    addCounty(Mower,
        "507-437-9700",
        "dhsrecep@co.mower.mn.us",
        "7680711b-f022-45c4-a325-c27d2cb468dc",
        "M000050700");
    addCounty(Murray,
        "800-657-3811",
        "ssintake@swmhhs.com ",
        "04dc7d5e-0694-4cf0-9d01-f7ba15f4de33",
        "M000051500");
    addCounty(Nicollet,
        "507-934-8559",
        "hhsinfo@co.nicollet.mn.us",
        "9213b60f-9427-49fa-996a-b920f6f71346",
        "M000052300");
    addCounty(Nobles,
        "507-295-5213",
        "CommunityServices@co.nobles.mn.us",
        "2c393a3a-45b0-4b07-bd58-b8740962b5ad",
        "M000053100");
    addCounty(Norman,
        "218-784-5400",
        "mary.doyea@co.norman.mn.us",
        "5d9a1042-2e69-4d69-93de-94f007e49595",
        "A000054000");
    addCounty(Olmsted,
        "507-328-6500",
        "PAQ@co.olmsted.mn.us",
        "0dcade2f-4c2a-4450-8347-5888c9f966fb",
        "A000055800");
    addCounty(OtterTail,
        "218-998-8230",
        "imques@co.ottertail.mn.us",
        "77ba1a64-524d-492a-892e-09243ca4b9ab",
        "A000056600");
    addCounty(Pennington,
        "218-681-2880",
        "jasjostrand@co.pennington.mn.us",
        "09218b77-8e8b-4e99-9aaa-f8e2b8b84eb6",
        "A000057400");
    addCounty(Pine,
        "320-591-1570",
        "MNSure@co.pine.mn.us and INCOME.PROOF@CO.PINE.MN.US and snap@lakesandpines.org ",
        "8a429d36-b734-46a6-a70d-51688aebcbb4",
        "A000058200");
    addCounty(Pipestone,
        "507-825-6720",
        "webmaster@co.pipestone.mn.us",
        "86eb961c-f54f-4855-ac6e-b65531df3591",
        "A000059100");
    addCounty(Polk,
        "877-281-3127",
        "Jon.street@co.polk.mn.us",
        "2b8a2117-9488-4dd4-b6bd-dcf01fb1e699",
        "A000060400");
    addCounty(Pope,
        "320-634-7755",
        "intake@co.pope.mn.us",
        "5a869a7f-4ebb-44ae-b2db-a6f40396d6a0",
        "A000061200");
    addCounty(Ramsey,
        "651-266-4444",
        "FAS.Forms@co.ramsey.mn.us and ASKHumanServices@CO.RAMSEY.MN.US",
        "c9db444b-05a2-4566-8a80-2f669f557c00",
        "1811055957");
    addCounty(RedLake,
        "877-294-0846",
        "sswintake@mail.co.red-lake.mn.us",
        "b8dcbee4-7fdd-4eac-b1f4-5f7f7b8ae526",
        "A000063900");
    addCounty(Redwood,
        "888-234-1292",
        "Redwood.frontdesk@swmhhs.com",
        "34e9e8a2-85a1-48f5-baec-f3191f9e66ba",
        "A000064700");
    addCounty(Renville,
        "320-523-2202",
        "hs@renvillecountymn.com",
        "ffdb09bd-7354-4690-9494-04aa6e91afdc",
        "M000065500");
    addCounty(Rice,
        "507-332-6115",
        "RCsocialservices@co.rice.mn.com",
        "4f131207-1680-4834-a53b-cee6d7d3fad0",
        "M000066300");
    addCounty(Rock,
        "507-283-5070",
        "ssintake@swmhhs.com ",
        "28d44e96-9a18-432d-a756-db41d2f6edf5",
        "M000067100");
    addCounty(Roseau,
        "866-255-2932",
        "case.bank@co.roseau.mn.us",
        "eef81190-0c7e-4702-8256-6e48b2f43666",
        "A000068000");
    addCounty(Scott,
        "952-496-8686",
        "scottcountyincomemaintenance@co.scott.mn.us",
        "992e4728-7b65-468d-a8bd-37cb3c532f97",
        "A000070100");
    addCounty(Sherburne,
        "800-433-5239",
        "PADocs@co.sherburne.mn.us",
        "7587e3c7-5409-4caf-b3c0-e4864f8b09b6",
        "1447381660");
    addCounty(Sibley,
        "507-237-4000",
        "contactphhf@co.sibley.mn.us ",
        "9bc21fe3-74bd-4655-93fa-7f3f91dda8db",
        "A000072800");
    addCounty(Stearns,
        "800-450-3663",
        "HSGatewayOSIII@co.stearns.mn.us",
        "057d8047-a162-49e8-abbb-1776d00beee7",
        "A000073600");
    addCounty(Steele,
        "507-431-5600",
        "OSS@MNPrairie.org",
        "2e0023fa-2ffb-4582-a28f-495f7606b41f",
        "A000074400");
    addCounty(Stevens,
        "800-950-4429",
        "mariaburns@co.stevens.mn.us",
        "3e0b6d5d-01e0-4fc4-8b83-e2fb92ca6128",
        "A000075200");
    addCounty(StLouis,
        "800-450-9777 or 218-726-2101",
        "ESS@stlouiscountymn.gov",
        "8e50a17a-6f8e-43bd-9de1-da4dff40a478",
        "A000069800");
    addCounty(Swift,
        "320-843-3160",
        "julie.jahn@co.swift.mn.us ",
        "452c72d8-0637-4c86-b49d-a880479d6beb",
        "A000076100");
    addCounty(Todd,
        "888-838-4066",
        "Ricoh@co.todd.mn.us",
        "699ab84a-d698-454b-900b-65138462aa98",
        "1336372465");
    addCounty(Traverse,
        "855-735-8916",
        "stacey.hennen@co.traverse.mn.us",
        "04f37360-e898-4aab-a0ba-479d717006a2",
        "A000078700");
    addCounty(Wabasha,
        "888-315-8815",
        "imuinterview@co.wabasha.mn.us",
        "4b087cdb-ccee-44ad-b461-e5a829988b01",
        "A000079500");
    addCounty(Wadena,
        "888-662-2737",
        "wchs.benefits@co.wadena.mn.us",
        "8d10df90-416f-46e7-80e1-e5f3bf02ae73",
        "A000080900");
    addCounty(Waseca,
        "507-837-6600",
        "OSS@MNPrairie.org",
        "f8fcee94-ae6e-4a1c-9b97-1e1bcf683249",
        "A000081700");
    addCounty(Washington,
        "651-430-6455",
        "stephanie.schlageter@co.washington.mn.us",
        "3e23953a-68ad-446b-8da6-f6f8c1b89c01",
        "1700969334");
    addCounty(Watonwan,
        "888-299-5941",
        "randee.nelson@co.watonwan.mn.us",
        "6d1b4962-ef66-407c-9191-39a53647de85",
        "1942539846");
    addCounty(Wilkin,
        "218-643-7161",
        "intake@co.wilkin.mn.us",
        "584963b6-63cc-4b49-a1b7-3dcc8410fae7",
        "1962567529");
    addCounty(Winona,
        "507-457-6200",
        "HHS@co.winona.mn.us",
        "19b94efa-f39a-4699-881f-a261c38c1d41",
        "A000085000");
    addCounty(Wright,
        "800-362-3667",
        "HSFSPrograms@co.wright.mn.us",
        "644cae25-1cd5-481a-8f71-19446b83d3e1",
        "1124197249");
    addCounty(YellowMedicine,
        "320-564-2211",
        "robin.schoep@co.ym.mn.gov",
        "31a2b2cb-be71-48b8-8824-9e98d78c597f",
        "A000087600");
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
