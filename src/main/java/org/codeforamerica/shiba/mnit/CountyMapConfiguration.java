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

    updateCounty(Aitkin,
        "help+staging@mnbenefits.org",
        "A000001900");
    updateCounty(Anoka,
        "help+staging@mnbenefits.org",
        "1023250115");
    updateCounty(Becker,
        "help+staging@mnbenefits.org",
        "A000003500");
    updateCounty(Beltrami,
        "help+staging@mnbenefits.org",
        "A000004300");
    updateCounty(Benton,
        "help+staging@mnbenefits.org",
        "1174697148");
    updateCounty(BigStone,
        "help+staging@mnbenefits.org",
        "A000006000");
    updateCounty(BlueEarth,
        "help+staging@mnbenefits.org",
        "A000007800");
    updateCounty(Brown,
        "help+staging@mnbenefits.org",
        "A000008600");
    updateCounty(Carlton,
        "help+staging@mnbenefits.org",
        "1003921875");
    updateCounty(Carver,
        "help+staging@mnbenefits.org",
        "A000010800");
    updateCounty(Cass,
        "help+staging@mnbenefits.org",
        "1760489769");
    updateCounty(Chippewa,
        "help+staging@mnbenefits.org",
        "A000012400");
    updateCounty(Chisago,
        "help+staging@mnbenefits.org",
        "1659408904");
    updateCounty(Clay,
        "help+staging@mnbenefits.org",
        "A000014100");
    updateCounty(Clearwater,
        "help+staging@mnbenefits.org",
        "A000015900");
    updateCounty(Cook,
        "help+staging@mnbenefits.org",
        "A000016700");
    updateCounty(Cottonwood,
        "help+staging@mnbenefits.org",
        "A000017500");
    updateCounty(CrowWing,
        "help+staging@mnbenefits.org",
        "A000018300");
    updateCounty(Dakota,
        "help+staging@mnbenefits.org",
        "1427127620");
    updateCounty(Dodge,
        "help+staging@mnbenefits.org",
        "A000020500");
    updateCounty(Douglas,
        "help+staging@mnbenefits.org",
        "A000021300");
    updateCounty(Faribault,
        "help+staging@mnbenefits.org",
        "A000022100");
    updateCounty(Fillmore,
        "help+staging@mnbenefits.org",
        "1437228236");
    updateCounty(Freeborn,
        "help+staging@mnbenefits.org",
        "A000024800");
    updateCounty(Goodhue,
        "help+staging@mnbenefits.org",
        "A000025600");
    //Grant and Pope are merged, now using Pope NPI
    updateCounty(Grant,
        "help+staging@mnbenefits.org",
        "A000061200");
    updateCounty(Hennepin,
        "help+staging@mnbenefits.org",
        "A000027200");
    updateCounty(Houston,
        "help+staging@mnbenefits.org",
        "A000028100");
    updateCounty(Hubbard,
        "help+staging@mnbenefits.org",
        "A000029900");
    updateCounty(Isanti,
        "help+staging@mnbenefits.org",
        "A000030200");
    updateCounty(Itasca,
        "help+staging@mnbenefits.org",
        "A000031100");
    updateCounty(Jackson,
        "help+staging@mnbenefits.org",
        "A000032900");
    updateCounty(Kanabec,
        "help+staging@mnbenefits.org",
        "1396819108");
    updateCounty(Kandiyohi,
        "help+staging@mnbenefits.org",
        "A000034500");
    updateCounty(Kittson,
        "help+staging@mnbenefits.org",
        "A000035300");
    updateCounty(Koochiching,
        "help+staging@mnbenefits.org",
        "A000036100");
    updateCounty(LacQuiParle,
        "help+staging@mnbenefits.org",
        "A000037000");
    updateCounty(Lake,
        "help+staging@mnbenefits.org",
        "A000038800");
    updateCounty(LakeOfTheWoods,
        "help+staging@mnbenefits.org",
        "A000039600");
    updateCounty(LeSueur,
        "help+staging@mnbenefits.org",
        "A000040000");
    updateCounty(Lincoln,
        "help+staging@mnbenefits.org",
        "A000041800");
    updateCounty(Lyon,
        "help+staging@mnbenefits.org",
        "A000042600");
    updateCounty(McLeod,
        "help+staging@mnbenefits.org",
        "A000043400");
    updateCounty(Mahnomen,
        "help+staging@mnbenefits.org",
        "A000044200");
    updateCounty(Marshall,
        "help+staging@mnbenefits.org",
        "A000045100");
    updateCounty(Martin,
        "help+staging@mnbenefits.org",
        "A000046900");
    updateCounty(Meeker,
        "help+staging@mnbenefits.org",
        "A000047700");
    updateCounty(MilleLacs,
        "help+staging@mnbenefits.org",
        "A000048500");
    updateCounty(Morrison,
        "help+staging@mnbenefits.org",
        "1255406286");
    updateCounty(Mower,
        "help+staging@mnbenefits.org",
        "M000050700");
    updateCounty(Murray,
        "help+staging@mnbenefits.org",
        "M000051500");
    updateCounty(Nicollet,
        "help+staging@mnbenefits.org",
        "M000052300");
    updateCounty(Nobles,
        "help+staging@mnbenefits.org",
        "M000053100");
    updateCounty(Norman,
        "help+staging@mnbenefits.org",
        "A000054000");
    updateCounty(Olmsted,
        "help+staging@mnbenefits.org",
        "A000055800");
    updateCounty(OtterTail,
        "help+staging@mnbenefits.org",
        "A000056600");
    updateCounty(Pennington,
        "help+staging@mnbenefits.org",
        "A000057400");
    updateCounty(Pine,
        "help+staging@mnbenefits.org",
        "A000058200");
    updateCounty(Pipestone,
        "help+staging@mnbenefits.org",
        "A000059100");
    updateCounty(Polk,
        "help+staging@mnbenefits.org",
        "A000060400");
    updateCounty(Pope,
        "help+staging@mnbenefits.org",
        "A000061200");
    updateCounty(Ramsey,
        "help+staging@mnbenefits.org",
        "1811055957");
    updateCounty(RedLake,
        "help+staging@mnbenefits.org",
        "A000063900");
    updateCounty(Redwood,
        "help+staging@mnbenefits.org",
        "A000064700");
    updateCounty(Renville,
        "help+staging@mnbenefits.org",
        "M000065500");
    updateCounty(Rice,
        "help+staging@mnbenefits.org",
        "M000066300");
    updateCounty(Rock,
        "help+staging@mnbenefits.org",
        "M000067100");
    updateCounty(Roseau,
        "help+staging@mnbenefits.org",
        "A000068000");
    updateCounty(Scott,
        "help+staging@mnbenefits.org",
        "A000070100");
    updateCounty(Sherburne,
        "help+staging@mnbenefits.org",
        "1447381660");
    updateCounty(Sibley,
        "help+staging@mnbenefits.org",
        "A000072800");
    updateCounty(Stearns,
        "help+staging@mnbenefits.org",
        "A000073600");
    updateCounty(Steele,
        "help+staging@mnbenefits.org",
        "A000074400");
    updateCounty(Stevens,
        "help+staging@mnbenefits.org",
        "A000075200");
    updateCounty(StLouis,
        "help+staging@mnbenefits.org",
        "A000069800");
    updateCounty(Swift,
        "help+staging@mnbenefits.org",
        "A000076100");
    updateCounty(Todd,
        "help+staging@mnbenefits.org",
        "1336372465");
    updateCounty(Traverse,
        "help+staging@mnbenefits.org",
        "A000078700");
    updateCounty(Wabasha,
        "help+staging@mnbenefits.org",
        "A000079500");
    updateCounty(Wadena,
        "help+staging@mnbenefits.org",
        "A000080900");
    updateCounty(Waseca,
        "help+staging@mnbenefits.org",
        "A000081700");
    updateCounty(Washington,
        "help+staging@mnbenefits.org",
        "1700969334");
    updateCounty(Watonwan,
        "help+staging@mnbenefits.org",
        "1942539846");
    updateCounty(Wilkin,
        "help+staging@mnbenefits.org",
        "1962567529");
    updateCounty(Winona,
        "help+staging@mnbenefits.org",
        "A000085000");
    updateCounty(Wright,
        "help+staging@mnbenefits.org",
        "1124197249");
    updateCounty(YellowMedicine,
        "help+staging@mnbenefits.org",
        "A000087600");
    countyMap.setDefaultValue(countyMap.get(Hennepin));
    return countyMap;
  }

  @Bean
  @Profile("production")
  CountyMap<CountyRoutingDestination> productionMapping() {
    initializeDefaultCountyMap();
    updateCounty(Aitkin,
        "achhs@co.aitkin.mn.us",
        "A000001900");
    updateCounty(Anoka,
        "EADocs@co.anoka.mn.us",
        "1023250115");
    updateCounty(Becker,
        "hsosu2@co.becker.mn.us",
        "A000003500");
    updateCounty(Beltrami,
        "BeltramiCoPubAssntOffice-DO-NOT-REPLY@co.beltrami.mn.us",
        "A000004300");
    updateCounty(Benton,
        "financial@co.benton.mn.us",
        "1174697148");
    updateCounty(BigStone,
        "familyservicecenter@bigstonecounty.org",
        "A000006000");
    updateCounty(BlueEarth,
        "DeAnn.Boney@blueearthcountymn.gov",
        "A000007800");
    updateCounty(Brown,
        "FrontDesk1@co.brown.mn.us",
        "A000008600");
    updateCounty(Carlton,
        "IMOSS@co.carlton.mn.us",
        "1003921875");
    updateCounty(Carver,
        "cssfinancial@co.carver.mn.us",
        "A000010800");
    updateCounty(Cass,
        "cass.socserv@co.cass.mn.us",
        "1760489769");
    updateCounty(Chippewa,
        "Tracy.kittelson@chippewa.mn",
        "A000012400");
    updateCounty(Chisago,
        "hhsim@chisagocounty.us",
        "1659408904");
    updateCounty(Clay,
        "social.services@co.clay.mn.us",
        "A000014100");
    updateCounty(Clearwater,
        "samantha.coyle@co.clearwater.mn.us",
        "A000015900");
    updateCounty(Cook,
        "IM@co.cook.mn.us",
        "A000016700");
    updateCounty(Cottonwood,
        "financial@dvhhs.org",
        "A000017500");
    updateCounty(CrowWing,
        "cwcss@crowwing.us",
        "A000018300");
    updateCounty(Dakota,
        "EEARIGAppResearch@CO.DAKOTA.MN.US",
        "1427127620");
    updateCounty(Dodge,
        "OSS@MNPrairie.org",
        "A000020500");
    updateCounty(Douglas,
        "dcss@co.douglass.mn.us",
        "A000021300");
    updateCounty(Faribault,
        "lea.silverthorn@fmchs.com ",
        "A000022100");
    updateCounty(Fillmore,
        "SS-FAX@co.fillmore.mn.us",
        "1437228236");
    updateCounty(Freeborn,
        "im.dhs@co.freeborn.mn.us",
        "A000024800");
    updateCounty(Goodhue,
        "hhs.imu@co.goodhue.mn.us",
        "A000025600");
    // Grant and Pope counties merged, now using Pope NPI and folder id
    updateCounty(Grant,
        "crystal.zaviska@westernprairiemn.us",
        "A000061200");
    updateCounty(Hennepin,
        "hhsews@hennepin.us",
        "A000027200");
    updateCounty(Houston,
        "dhsinfo@co.houston.mn.us",
        "A000028100");
    updateCounty(Hubbard,
        "beth.vredenburg@co.hubbard.mn.us",
        "A000029900");
    updateCounty(Isanti,
        "Jennifer.Ann.Johnson@co.isanti.mn.us",
        "A000030200");
    updateCounty(Itasca,
        "FAUsupport@co.itasca.mn.us",
        "A000031100");
    updateCounty(Jackson,
        "financial@dvhhs.org",
        "A000032900");
    updateCounty(Kanabec,
        "family.services@co.kanabec.mn.us",
        "1396819108");
    updateCounty(Kandiyohi,
        "hs-financial@kcmn.us",
        "A000034500");
    updateCounty(Kittson,
        "bseed@co.kittson.mn.us",
        "A000035300");
    updateCounty(Koochiching,
        "valerie.long@co.koochiching.mn.us",
        "A000036100");
    updateCounty(LacQuiParle,
        "familyservices@co.lac-qui-parle.mn.us",
        "A000037000");
    updateCounty(Lake,
        "financial.assistance@co.lake.mn.us",
        "A000038800");
    updateCounty(LakeOfTheWoods,
        "cassondra_b@co.lotw.mn.us",
        "A000039600");
    updateCounty(LeSueur,
        "IMDocs@co.le-sueur.mn.us",
        "A000040000");
    updateCounty(Lincoln,
        "ivanhoe.frontdesk@swmhhs.com",
        "A000041800");
    updateCounty(Lyon,
        "marshallss.frontdesk@swmhhs.com",
        "A000042600");
    updateCounty(McLeod,
        "mcleod.fw@co.mcleod.mn.us",
        "A000043400");
    updateCounty(Mahnomen,
        "info@co.mahnomen.mn.us",
        "A000044200");
    updateCounty(Marshall,
        "sarah.noble@co.marshall.mn.us",
        "A000045100");
    updateCounty(Martin,
        "lea.silverthorn@fmchs.com",
        "A000046900");
    updateCounty(Meeker,
        "socserv.info@co.meeker.mn.us",
        "A000047700");
    updateCounty(MilleLacs,
        "Beth.Sumner@millelacs.mn.gov",
        "A000048500");
    updateCounty(Morrison,
        "callcenter@co.morrison.mn.us",
        "1255406286");
    updateCounty(Mower,
        "dhsrecep@co.mower.mn.us",
        "M000050700");
    updateCounty(Murray,
        "slayton.frontdesk@swmhhs.com",
        "M000051500");
    updateCounty(Nicollet,
        "hhsinfo@co.nicollet.mn.us",
        "M000052300");
    updateCounty(Nobles,
        "CommunityServices@co.nobles.mn.us",
        "M000053100");
    updateCounty(Norman,
        "mary.doyea@co.norman.mn.us",
        "A000054000");
    updateCounty(Olmsted,
        "PAQ@co.olmsted.mn.us",
        "A000055800");
    updateCounty(OtterTail,
        "imques@co.ottertail.mn.us",
        "A000056600");
    updateCounty(Pennington,
        "jasjostrand@co.pennington.mn.us",
        "A000057400");
    updateCounty(Pine,
        "income.proof@co.pine.mn.us",
        "A000058200");
    updateCounty(Pipestone,
        "pipestone.frontdesk@swmhhs.com",
        "A000059100");
    updateCounty(Polk,
        "ssoss@co.polk.mn.us",
        "A000060400");
    //Grant and Pope merged into Western Prairie, using Pope NPI and westernprairie email
    updateCounty(Pope,
        "crystal.zaviska@westernprairiemn.us",
        "A000061200");
    updateCounty(Ramsey,
        "fas.forms@co.ramsey.mn.us",
        "1811055957");
    updateCounty(RedLake,
        "sswintake@mail.co.red-lake.mn.us",
        "A000063900");
    updateCounty(Redwood,
        "Redwood.frontdesk@swmhhs.com",
        "A000064700");
    updateCounty(Renville,
        "hs@renvillecountymn.com",
        "M000065500");
    updateCounty(Rice,
        "RCsocialservices@co.rice.mn.com",
        "M000066300");
    updateCounty(Rock,
        "luverne.frontdesk@swmhhs.com",
        "M000067100");
    updateCounty(Roseau,
        "case.bank@co.roseau.mn.us",
        "A000068000");
    updateCounty(Scott,
        "scottcountyincomemaintenance@co.scott.mn.us",
        "A000070100");
    updateCounty(Sherburne,
        "PADocs@co.sherburne.mn.us",
        "1447381660");
    updateCounty(Sibley,
        "ContactPHHS@co.sibley.mn.us",
        "A000072800");
    updateCounty(Stearns,
        "HSGatewayOSIII@co.stearns.mn.us",
        "A000073600");
    updateCounty(Steele,
        "OSS@MNPrairie.org",
        "A000074400");
    updateCounty(Stevens,
        "mariaburns@co.stevens.mn.us",
        "A000075200");
    updateCounty(StLouis,
        "ESS@stlouiscountymn.gov",
        "A000069800");
    updateCounty(Swift,
        "julie.jahn@co.swift.mn.us",
        "A000076100");
    updateCounty(Todd,
        "Ricoh@co.todd.mn.us",
        "1336372465");
    updateCounty(Traverse,
        "stacey.hennen@co.traverse.mn.us",
        "A000078700");
    updateCounty(Wabasha,
        "imuinterview@co.wabasha.mn.us",
        "A000079500");
    updateCounty(Wadena,
        "wchs.benefits@co.wadena.mn.us",
        "A000080900");
    updateCounty(Waseca,
        "OSS@MNPrairie.org",
        "A000081700");
    updateCounty(Washington,
        "stephanie.schlageter@co.washington.mn.us",
        "1700969334");
    updateCounty(Watonwan,
        "randee.nelson@co.watonwan.mn.us",
        "1942539846");
    updateCounty(Wilkin,
        "cnoetzelman@co.wilkin.mn.us",
        "1962567529");
    updateCounty(Winona,
        "dhs@co.winona.mn.us",
        "A000085000");
    updateCounty(Wright,
        "HSFSPrograms@co.wright.mn.us",
        "1124197249");
    updateCounty(YellowMedicine,
        "robin.schoep@co.ym.mn.gov",
        "A000087600");
    countyMap.setDefaultValue(countyMap.get(Hennepin));
    return countyMap;
  }

  private void initializeDefaultCountyMap() {
    countyMap = new CountyMap<>();
    addCounty(Aitkin, "800-328-3744");
    addCounty(Anoka,
        "763-422-7200",
        "help+dev@mnbenefits.org",
        "1023250115"
    );
    addCounty(Becker, "218-847-5628");
    addCounty(Beltrami, "218-333-8300");
    addCounty(Benton, "800-530-6254");
    addCounty(BigStone, "320-839-2555");
    addCounty(BlueEarth, "507-304-4335");
    addCounty(Brown, "800-450-8246");
    addCounty(Carlton, "800-642-9082");
    addCounty(Carver, "952-361-1600",
        "help+dev@mnbenefits.org",
        "A000010800"
    );
    addCounty(Cass, "218-547-1340");
    addCounty(Chippewa, "320-269-6401");
    addCounty(Chisago, "888-234-1246");
    addCounty(Clay, "800-757-3880",
        "help+dev@mnbenefits.org",
        "A000014100"
    );
    addCounty(Clearwater, "800-245-6064");
    addCounty(Cook,
        "218-387-3620",
        "help+dev@mnbenefits.org",
        "A000016700");
    addCounty(Cottonwood, "507-831-1891");
    addCounty(CrowWing, "888-772-8212");
    addCounty(Dakota, "888-850-9419");
    addCounty(Dodge,
        "507-923-2900",
        "help+dev@mnbenefits.org",
        "A000020500");
    addCounty(Douglas, "320-762-2302");
    addCounty(Faribault, "507-526-3265");
    addCounty(Fillmore, "507-765-2175");
    addCounty(Freeborn, "507-377-5400");
    addCounty(Goodhue, "651-385-3200");
    addCounty(Grant, "320-634-7758", //Grant and Pope share same phone number and NPI
        "help+dev@mnbenefits.org",
        "A000061200");
    addCounty(Hennepin,
        "612-596-1300",
        "help+dev@mnbenefits.org",
        "A000027200",
        "100 S 1st St", "Minneapolis", "55401");
    addCounty(Houston, "507-725-5811");
    addCounty(Hubbard, "877-450-1451");
    addCounty(Isanti, "763-689-1711");
    addCounty(Itasca, "800-422-0312");
    addCounty(Jackson, "507-847-4000");
    addCounty(Kanabec, "320-679-6350");
    addCounty(Kandiyohi, "877-464-7800");
    addCounty(Kittson, "800-672-8026");
    addCounty(Koochiching, "800-950-4630");
    addCounty(LacQuiParle, "320-598-7594");
    addCounty(Lake, "218-834-8400");
    addCounty(LakeOfTheWoods, "218-634-2642");
    addCounty(LeSueur, "507-357-8288");
    addCounty(Lincoln, "800-657-3781");
    addCounty(Lyon, "800-657-3760");
    addCounty(McLeod, "800-247-1756");
    addCounty(Mahnomen, "218-935-2568");
    addCounty(Marshall, "800-642-5444");
    addCounty(Martin, "507-238-4757");
    addCounty(Meeker, "877-915-5300");
    addCounty(MilleLacs, "888-270-8208");
    addCounty(Morrison,
        "800-269-1464",
        "help+dev@mnbenefits.org",
        "1255406286");
    addCounty(Mower, "507-437-9700");
    addCounty(Murray, "800-657-3811");
    addCounty(Nicollet, "507-934-8559");
    addCounty(Nobles, "507-295-5213");
    addCounty(Norman, "218-784-5400");
    addCounty(Olmsted,
        "507-328-6500",
        "help+dev@mnbenefits.org",
        "A000055800");
    addCounty(OtterTail,
        "218-998-8230",
        "help+dev@mnbenefits.org",
        "A000056600");
    addCounty(Pennington, "218-681-2880");
    addCounty(Pine, "320-591-1570");
    addCounty(Pipestone, "507-825-6720");
    addCounty(Polk, "877-281-3127");
    addCounty(Pope, "320-634-7758", //Grant and Pope share same phone number and NPI
        "help+dev@mnbenefits.org",
        "A000061200");
    addCounty(Ramsey, "651-266-4444");
    addCounty(RedLake, "877-294-0846");
    addCounty(Redwood, "888-234-1292");
    addCounty(Renville, "320-523-2202");
    addCounty(Rice, "507-332-6115");
    addCounty(Rock, "507-283-5070");
    addCounty(Roseau, "866-255-2932");
    addCounty(Scott, "952-496-8686");
    addCounty(Sherburne,
        "800-433-5239",
        "help+dev@mnbenefits.org",
        "1447381660");
    addCounty(Sibley, "507-237-4000");
    addCounty(Stearns, "800-450-3663");
    addCounty(Steele,
        "507-431-5600",
        "help+dev@mnbenefits.org",
        "A000074400");
    addCounty(Stevens, "800-950-4429");
    addCounty(StLouis,
        "800-450-9777 or 218-726-2101",
        "help+dev@mnbenefits.org",
        "A000069800");
    addCounty(Swift, "320-843-3160");
    addCounty(Todd, "888-838-4066");
    addCounty(Traverse, "855-735-8916");
    addCounty(Wabasha,
        "888-315-8815",
        "help+dev@mnbenefits.org",
        "A000079500");
    addCounty(Wadena,
        "888-662-2737",
        "help+dev@mnbenefits.org",
        "A000080900");
    addCounty(Waseca,
        "507-837-6600",
        "help+dev@mnbenefits.org",
        "A000081700");
    addCounty(Washington, "651-430-6455");
    addCounty(Watonwan, "888-299-5941");
    addCounty(Wilkin, "218-643-7161");
    addCounty(Winona, "507-457-6200");
    addCounty(Wright,
        "800-362-3667",
        "help+dev@mnbenefits.org",
        "1124197249");
    addCounty(YellowMedicine, "320-564-2211");
  }

  private void addCounty(County county, String phoneNumber, String email,
      String dhsProviderId, String mailingStreetAddress, String mailingCity,
      String mailingZipcode) {
    addCounty(county, CountyRoutingDestination.builder()
        .email(email)
        .phoneNumber(phoneNumber)
        .dhsProviderId(dhsProviderId)
        .postOfficeAddress(
            new Address(mailingStreetAddress, mailingCity, "MN", mailingZipcode, "",
                county.displayName())));
  }

  private void addCounty(County county, String phoneNumber, String email,
      String dhsProviderId) {
    addCounty(county, CountyRoutingDestination.builder().email(email)
        .phoneNumber(phoneNumber)
        .dhsProviderId(dhsProviderId));
  }

  private void addCounty(County county, String phoneNumber) {
    addCounty(county, CountyRoutingDestination.builder().phoneNumber(phoneNumber));
  }

  private void addCounty(County county,
      CountyRoutingDestinationBuilder builder) {
    countyMap.getCounties().put(county, builder.county(county).build());
  }

  private void updateCounty(County county, String email, String dhsProviderId) {
    CountyRoutingDestination countyInfo = countyMap.get(county);
    countyInfo.setEmail(email);
    countyInfo.setDhsProviderId(dhsProviderId);
  }
}
