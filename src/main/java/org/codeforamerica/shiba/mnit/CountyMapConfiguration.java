package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.County.Aitkin;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Becker;
import static org.codeforamerica.shiba.County.Beltrami;
import static org.codeforamerica.shiba.County.Benton;
import static org.codeforamerica.shiba.County.BigStone;
import static org.codeforamerica.shiba.County.BlueEarth;
import static org.codeforamerica.shiba.County.Brown;
import static org.codeforamerica.shiba.County.Carlton;
import static org.codeforamerica.shiba.County.Carver;
import static org.codeforamerica.shiba.County.Cass;
import static org.codeforamerica.shiba.County.Chippewa;
import static org.codeforamerica.shiba.County.Chisago;
import static org.codeforamerica.shiba.County.Clay;
import static org.codeforamerica.shiba.County.Clearwater;
import static org.codeforamerica.shiba.County.Cook;
import static org.codeforamerica.shiba.County.Cottonwood;
import static org.codeforamerica.shiba.County.CrowWing;
import static org.codeforamerica.shiba.County.Dakota;
import static org.codeforamerica.shiba.County.Dodge;
import static org.codeforamerica.shiba.County.Douglas;
import static org.codeforamerica.shiba.County.Faribault;
import static org.codeforamerica.shiba.County.Fillmore;
import static org.codeforamerica.shiba.County.Freeborn;
import static org.codeforamerica.shiba.County.Goodhue;
import static org.codeforamerica.shiba.County.Grant;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Houston;
import static org.codeforamerica.shiba.County.Hubbard;
import static org.codeforamerica.shiba.County.Isanti;
import static org.codeforamerica.shiba.County.Itasca;
import static org.codeforamerica.shiba.County.Jackson;
import static org.codeforamerica.shiba.County.Kanabec;
import static org.codeforamerica.shiba.County.Kandiyohi;
import static org.codeforamerica.shiba.County.Kittson;
import static org.codeforamerica.shiba.County.Koochiching;
import static org.codeforamerica.shiba.County.LacQuiParle;
import static org.codeforamerica.shiba.County.Lake;
import static org.codeforamerica.shiba.County.LakeOfTheWoods;
import static org.codeforamerica.shiba.County.LeSueur;
import static org.codeforamerica.shiba.County.Lincoln;
import static org.codeforamerica.shiba.County.Lyon;
import static org.codeforamerica.shiba.County.Mahnomen;
import static org.codeforamerica.shiba.County.Marshall;
import static org.codeforamerica.shiba.County.Martin;
import static org.codeforamerica.shiba.County.McLeod;
import static org.codeforamerica.shiba.County.Meeker;
import static org.codeforamerica.shiba.County.MilleLacs;
import static org.codeforamerica.shiba.County.Morrison;
import static org.codeforamerica.shiba.County.Mower;
import static org.codeforamerica.shiba.County.Murray;
import static org.codeforamerica.shiba.County.Nicollet;
import static org.codeforamerica.shiba.County.Nobles;
import static org.codeforamerica.shiba.County.Norman;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.County.OtterTail;
import static org.codeforamerica.shiba.County.Pennington;
import static org.codeforamerica.shiba.County.Pine;
import static org.codeforamerica.shiba.County.Pipestone;
import static org.codeforamerica.shiba.County.Polk;
import static org.codeforamerica.shiba.County.Pope;
import static org.codeforamerica.shiba.County.Ramsey;
import static org.codeforamerica.shiba.County.RedLake;
import static org.codeforamerica.shiba.County.Redwood;
import static org.codeforamerica.shiba.County.Renville;
import static org.codeforamerica.shiba.County.Rice;
import static org.codeforamerica.shiba.County.Rock;
import static org.codeforamerica.shiba.County.Roseau;
import static org.codeforamerica.shiba.County.Scott;
import static org.codeforamerica.shiba.County.Sherburne;
import static org.codeforamerica.shiba.County.Sibley;
import static org.codeforamerica.shiba.County.StLouis;
import static org.codeforamerica.shiba.County.Stearns;
import static org.codeforamerica.shiba.County.Steele;
import static org.codeforamerica.shiba.County.Stevens;
import static org.codeforamerica.shiba.County.Swift;
import static org.codeforamerica.shiba.County.Todd;
import static org.codeforamerica.shiba.County.Traverse;
import static org.codeforamerica.shiba.County.Wabasha;
import static org.codeforamerica.shiba.County.Wadena;
import static org.codeforamerica.shiba.County.Waseca;
import static org.codeforamerica.shiba.County.Washington;
import static org.codeforamerica.shiba.County.Watonwan;
import static org.codeforamerica.shiba.County.Wilkin;
import static org.codeforamerica.shiba.County.Winona;
import static org.codeforamerica.shiba.County.Wright;
import static org.codeforamerica.shiba.County.YellowMedicine;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SuppressWarnings("DuplicatedCode")
@Configuration
public class CountyMapConfiguration {

  private ServicingAgencyMap<CountyRoutingDestination> countyMap;
  private final String DEV_EMAIL = "mnbenefits@state.mn.us";

  @Bean
  @Profile({"default", "test", "dev", "atst"})
  ServicingAgencyMap<CountyRoutingDestination> nonProdMapping() {
    initializeDefaultCountyMap();
    return countyMap;
  }

  @Bean
  @Profile("production")
  ServicingAgencyMap<CountyRoutingDestination> productionMapping() {
    initializeDefaultCountyMap();
    updateCounty(Aitkin, "achhs@co.aitkin.mn.us", "A000001900");
    updateCounty(Anoka, "EADocs@co.anoka.mn.us", "1023250115");
    updateCounty(Becker, "hsosu2@co.becker.mn.us", "A000003500");
    updateCounty(Beltrami, "BeltramiCoPubAssntOffice-DO-NOT-REPLY@co.beltrami.mn.us", "A000004300");
    updateCounty(Benton, "financial@co.benton.mn.us", "1174697148");
    updateCounty(BigStone, "familyservicecenter@bigstonecounty.gov", "A000006000");
    updateCounty(BlueEarth, "DeAnn.Boney@blueearthcountymn.gov", "A000007800");
    updateCounty(Brown, "help123@co.brown.mn.us", "A000008600");
    updateCounty(Carlton, "IMOSS@co.carlton.mn.us", "1003921875");
    updateCounty(Carver, "cssfinancial@co.carver.mn.us", "A000010800");
    updateCounty(Cass, "cass.socserv@casscountymn.gov", "1760489769");
    updateCounty(Chippewa, "Tracy.kittelson@chippewa.mn", "A000012400");
    updateCounty(Chisago, "hhsim@chisagocounty.us", "1659408904");
    updateCounty(Clay, "social.services@co.clay.mn.us", "A000014100");
    updateCounty(Clearwater, "samantha.coyle@co.clearwater.mn.us", "A000015900");
    updateCounty(Cook, "economic.assistance@co.cook.mn.us", "A000016700");
    updateCounty(Cottonwood, "financial@dvhhs.org", "A000017500");
    updateCounty(CrowWing, "cwcss@crowwing.us", "A000018300");
    updateCounty(Dakota, "EEARIGAppResearch@CO.DAKOTA.MN.US", "1427127620");
    updateCounty(Dodge, "OSS@MNPrairie.org", "A000020500");
    updateCounty(Douglas, "dcss@co.douglas.mn.us", "A000021300");
    updateCounty(Faribault, "lea.silverthorn@fmchs.com ", "A000022100");
    updateCounty(Fillmore, "SS-FAX@co.fillmore.mn.us", "1437228236");
    updateCounty(Freeborn, "im.dhs@co.freeborn.mn.us", "A000024800");
    updateCounty(Goodhue, "hhs.imu@co.goodhue.mn.us", "A000025600");
    // Grant and Pope merged into Western Prairie, using Pope NPI and Western Prairie email
    updateCounty(Grant, "front@westernprairiemn.us", "A000061200");
    updateCounty(Hennepin, "hhsews@hennepin.us", "A000027200");
    updateCounty(Houston, "dhsinfo@co.houston.mn.us", "A000028100");
    updateCounty(Hubbard, "beth.vredenburg@co.hubbard.mn.us", "A000029900");
    updateCounty(Isanti, "Kelly.Borchardt@co.isanti.mn.us", "A000030200");
    updateCounty(Itasca, "FAUsupport@co.itasca.mn.us", "A000031100");
    updateCounty(Jackson, "financial@dvhhs.org", "A000032900");
    updateCounty(Kanabec, "family.services@co.kanabec.mn.us", "1396819108");
    updateCounty(Kandiyohi, "hs-financial@kcmn.us", "A000034500");
    updateCounty(Kittson, "bseed@co.kittson.mn.us", "A000035300");
    updateCounty(Koochiching, "Kccs.intake@co.koochiching.mn.us", "A000036100");
    updateCounty(LacQuiParle, "familyservices@co.lac-qui-parle.mn.us", "A000037000");
    updateCounty(Lake, "financial.assistance@co.lake.mn.us", "A000038800");
    updateCounty(LakeOfTheWoods, "mnbenefits@co.lotw.mn.us", "A000039600");
    updateCounty(LeSueur, "IMDocs@co.le-sueur.mn.us", "A000040000");
    updateCounty(Lincoln, "ssintake@swmhhs.com", "A000041800");
    updateCounty(Lyon, "ssintake@swmhhs.com", "A000042600");
    updateCounty(McLeod, "mcleod.fw@co.mcleod.mn.us", "A000043400");
    updateCounty(Mahnomen, "info@co.mahnomen.mn.us", "A000044200");
    updateCounty(Marshall, "sarah.noble@co.marshall.mn.us", "A000045100");
    updateCounty(Martin, "emily.hanson@fmchs.com", "A000046900");
    updateCounty(Meeker, "socserv.info@co.meeker.mn.us", "A000047700");
    updateCounty(MilleLacs, "Beth.Sumner@millelacs.mn.gov", "A000048500");
    updateCounty(Morrison, "callcenter@co.morrison.mn.us", "1255406286");
    updateCounty(Mower, "dhsrecep@co.mower.mn.us", "M000050700");
    updateCounty(Murray, "ssintake@swmhhs.com", "M000051500");
    updateCounty(Nicollet, "hhsinfo@co.nicollet.mn.us", "1083845127");
    updateCounty(Nobles, "CommunityServices@co.nobles.mn.us", "M000053100");
    updateCounty(Norman, "supportstaff@co.norman.mn.us", "A000054000");
    updateCounty(Olmsted, "PAQ@olmstedcounty.gov", "A000055800");
    updateCounty(OtterTail, "imques@co.ottertail.mn.us", "A000056600");
    updateCounty(Pennington, "Afcasebank@co.pennington.mn.us", "A000057400");
    updateCounty(Pine, "income.proof@co.pine.mn.us", "A000058200");
    updateCounty(Pipestone, "pipestone.frontdesk@swmhhs.com", "A000059100");
    updateCounty(Polk, "pcss.info@co.polk.mn.us", "A000060400");
    // Grant and Pope merged into Western Prairie, using Pope NPI and Western Prairie email
    updateCounty(Pope, "gfront@westernprairiemn.us", "A000061200");
    updateCounty(Ramsey, "FAS.Forms@co.ramsey.mn.us", "1811055957");
    updateCounty(RedLake, "reception@co.red-lake.mn.us", "A000063900");
    updateCounty(Redwood, "ssintake@swmhhs.com", "A000064700");
    updateCounty(Renville, "hs@renvillecountymn.com", "M000065500");
    updateCounty(Rice, "Fin.Asst@ricecountymn.gov", "M000066300");
    updateCounty(Rock, "ssintake@swmhhs.com", "M000067100");
    updateCounty(Roseau, "case.bank@co.roseau.mn.us", "A000068000");
    updateCounty(Scott, "scottcountyincomemaintenance@co.scott.mn.us", "A000070100");
    updateCounty(Sherburne, "PADocs@co.sherburne.mn.us", "1447381660");
    updateCounty(Sibley, "ContactPHHS@co.sibley.mn.us", "A000072800");
    updateCounty(Stearns, "HSGatewayOSIII@co.stearns.mn.us", "A000073600");
    updateCounty(Steele, "OSS@MNPrairie.org", "A000074400");
    updateCounty(Stevens, "imdocs@co.stevens.mn.us", "A000075200");
    updateCounty(StLouis, "ESS@stlouiscountymn.gov", "A000069800");
    updateCounty(Swift, "julie.jahn@co.swift.mn.us", "A000076100");
    updateCounty(Todd, "Ricoh@co.todd.mn.us", "1336372465");
    updateCounty(Traverse, "stacey.hennen@co.traverse.mn.us", "A000078700");
    updateCounty(Wabasha, "imuinterview@co.wabasha.mn.us", "A000079500");
    updateCounty(Wadena, "wchs.benefits@wcmn.us", "A000080900");
    updateCounty(Waseca, "OSS@MNPrairie.org", "A000081700");
    updateCounty(Washington, "stephanie.schlageter@co.washington.mn.us", "1700969334");
    updateCounty(Watonwan, "randee.nelson@co.watonwan.mn.us", "1942539846");
    updateCounty(Wilkin, "intake@co.wilkin.mn.us", "1962567529");
    updateCounty(Winona, "dhs@co.winona.mn.us", "A000085000");
    updateCounty(Wright, "HSFSPrograms@co.wright.mn.us", "1124197249");
    updateCounty(YellowMedicine, "robin.schoep@co.ym.mn.gov", "A000087600");
    countyMap.setDefaultValue(countyMap.get(Hennepin));
    return countyMap;
  }

  private void initializeDefaultCountyMap() {
    countyMap = new ServicingAgencyMap<>();
    addCountyDefaults(Aitkin, "A000001900", "800-328-3744");
    addCountyDefaults(Anoka, "1023250115", "763-422-7200");
    addCountyDefaults(Becker, "A000003500", "218-847-5628");
    addCountyDefaults(Beltrami, "A000004300", "218-333-8300");
    addCountyDefaults(Benton, "1174697148", "800-530-6254");
    addCountyDefaults(BigStone, "A000006000", "320-839-2555");
    addCountyDefaults(BlueEarth, "A000007800", "507-304-4335");
    addCountyDefaults(Brown, "A000008600", "507-359-6500");
    addCountyDefaults(Carlton, "1003921875", "800-642-9082");
    addCountyDefaults(Carver, "A000010800", "952-361-1600");
    addCountyDefaults(Cass, "1760489769", "218-547-1340");
    addCountyDefaults(Chippewa, "A000012400", "320-269-6401");
    addCountyDefaults(Chisago, "1659408904", "888-234-1246");
    addCountyDefaults(Clay, "A000014100", "800-757-3880");
    addCountyDefaults(Clearwater, "A000015900", "800-245-6064");
    addCountyDefaults(Cook, "A000016700", "218-387-3620");
    addCountyDefaults(Cottonwood, "A000017500", "507-831-1891");
    addCountyDefaults(CrowWing, "A000018300", "888-772-8212");
    addCountyDefaults(Dakota, "1427127620", "651-554-5611");
    addCountyDefaults(Dodge, "A000020500", "507-923-2900");
    addCountyDefaults(Douglas, "A000021300", "320-762-2302");
    addCountyDefaults(Faribault, "A000022100", "507-526-3265");
    addCountyDefaults(Fillmore, "1437228236", "507-765-2175");
    addCountyDefaults(Freeborn, "A000024800", "507-377-5400");
    addCountyDefaults(Goodhue, "A000025600", "651-385-3200");
    addCountyDefaults(Grant, "A000061200", "218-685-8200");
    addCountyDefaults(Hennepin, "A000027200", "612-596-1300", new Address(
        "100 S 1st St", "Minneapolis", "MN", "55401", "",
        Hennepin.toString()
    ));
    addCountyDefaults(Houston, "A000028100", "507-725-5811");
    addCountyDefaults(Hubbard, "A000029900", "877-450-1451");
    addCountyDefaults(Isanti, "A000030200", "763-689-1711");
    addCountyDefaults(Itasca, "A000031100", "800-422-0312");
    addCountyDefaults(Jackson, "A000032900", "507-847-4000");
    addCountyDefaults(Kanabec, "1396819108", "320-679-6350");
    addCountyDefaults(Kandiyohi, "A000034500", "877-464-7800");
    addCountyDefaults(Kittson, "A000035300", "800-672-8026");
    addCountyDefaults(Koochiching, "A000036100", "800-950-4630");
    addCountyDefaults(LacQuiParle, "A000037000", "320-598-7594");
    addCountyDefaults(Lake, "A000038800", "218-834-8400");
    addCountyDefaults(LakeOfTheWoods, "A000039600", "218-634-2642");
    addCountyDefaults(LeSueur, "A000040000", "507-357-8288");
    addCountyDefaults(Lincoln, "A000041800", "800-657-3781");
    addCountyDefaults(Lyon, "A000042600", "800-657-3760");
    addCountyDefaults(McLeod, "A000043400", "800-247-1756");
    addCountyDefaults(Mahnomen, "A000044200", "218-935-2568");
    addCountyDefaults(Marshall, "A000045100", "800-642-5444");
    addCountyDefaults(Martin, "A000046900", "507-238-4757");
    addCountyDefaults(Meeker, "A000047700", "877-915-5300");
    addCountyDefaults(MilleLacs, "A000048500", "888-270-8208");
    addCountyDefaults(Morrison, "1255406286", "800-269-1464");
    addCountyDefaults(Mower, "M000050700", "507-437-9700");
    addCountyDefaults(Murray, "M000051500", "800-657-3811");
    addCountyDefaults(Nicollet, "M000052300", "507-934-8559");
    addCountyDefaults(Nobles, "M000053100", "507-295-5213");
    addCountyDefaults(Norman, "A000054000", "218-784-5400");
    addCountyDefaults(Olmsted, "A000055800", "507-328-6500");
    addCountyDefaults(OtterTail, "A000056600", "218-998-8230");
    addCountyDefaults(Pennington, "A000057400", "218-681-2880");
    addCountyDefaults(Pine, "A000058200", "320-591-1570");
    addCountyDefaults(Pipestone, "A000059100", "507-825-6720");
    addCountyDefaults(Polk, "A000060400", "877-281-3127");
    addCountyDefaults(Pope, "A000061200", "320-634-7755");
    addCountyDefaults(Ramsey, "1811055957", "651-266-4444");
    addCountyDefaults(RedLake, "A000063900", "877-294-0846");
    addCountyDefaults(Redwood, "A000064700", "888-234-1292");
    addCountyDefaults(Renville, "M000065500", "320-523-2202");
    addCountyDefaults(Rice, "M000066300", "507-332-5995");
    addCountyDefaults(Rock, "M000067100", "507-283-5070");
    addCountyDefaults(Roseau, "A000068000", "866-255-2932");
    addCountyDefaults(Scott, "A000070100", "952-445-7751");
    addCountyDefaults(Sherburne, "1447381660", "800-433-5239");
    addCountyDefaults(Sibley, "A000072800", "507-237-4000");
    addCountyDefaults(Stearns, "A000073600", "800-450-3663");
    addCountyDefaults(Steele, "A000074400", "507-431-5600");
    addCountyDefaults(Stevens, "A000075200", "800-950-4429");
    addCountyDefaults(StLouis, "A000069800", "800-450-9777");
    addCountyDefaults(Swift, "A000076100", "320-843-3160");
    addCountyDefaults(Todd, "1336372465", "888-838-4066");
    addCountyDefaults(Traverse, "A000078700", "855-735-8916");
    addCountyDefaults(Wabasha, "A000079500", "888-315-8815");
    addCountyDefaults(Wadena, "A000080900", "888-662-2737");
    addCountyDefaults(Waseca, "A000081700", "507-837-6600");
    addCountyDefaults(Washington, "1700969334", "651-430-6455");
    addCountyDefaults(Watonwan, "1942539846", "888-299-5941");
    addCountyDefaults(Wilkin, "1962567529", "218-643-7161");
    addCountyDefaults(Winona, "A000085000", "507-457-6200");
    addCountyDefaults(Wright, "1124197249", "800-362-3667");
    addCountyDefaults(YellowMedicine, "A000087600", "320-564-2211");
    countyMap.setDefaultValue(countyMap.get(Hennepin));
  }

  private void addCountyDefaults(County county, String dhsProviderId, String phoneNumber,
      Address address) {
    countyMap.getAgencies().put(county,
        new CountyRoutingDestination(county, dhsProviderId, DEV_EMAIL, phoneNumber,
            address));
  }

  private void addCountyDefaults(County county, String dhsProviderId, String phoneNumber) {
    countyMap.getAgencies().put(county,
        new CountyRoutingDestination(county, dhsProviderId, DEV_EMAIL,
            phoneNumber));
  }

  private void updateCounty(County county, String email, String dhsProviderId) {
    CountyRoutingDestination countyInfo = countyMap.get(county);
    countyInfo.setEmail(email);
    countyInfo.setDhsProviderId(dhsProviderId);
  }
}
