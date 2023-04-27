package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.testutilities.TestUtils.getAbsoluteFilepathString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

public class LaterDocsMockMvcTest extends AbstractShibaMockMvcTest {

	private static final String UPLOADED_JPG_FILE_NAME = "shiba+file.jpg";

	@Autowired
	private RoutingDecisionService routingDecisionService;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private ServicingAgencyMap<CountyRoutingDestination> countyMap;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private ServicingAgencyMap<TribalNationRoutingDestination> tribalNationMap;

	@BeforeEach
	protected void setUp() throws Exception {
		super.setUp();
		applicationData.setFlow(FlowType.LATER_DOCS);
		new TestApplicationDataBuilder(applicationData);
		mockMvc.perform(get("/pages/identifyCountyOrTribalNation").session(session)); // start timer

	}

	@Test
	void routeLaterDocsToCorrectCountyOnly() throws Exception {
		postExpectingSuccess("identifyCountyOrTribalNation", "county", "Aitkin");

		postExpectingSuccess("matchInfo", Map.of(
				"firstName", List.of("Dwight"), 
				"lastName", List.of("Schrute"),
				"dateOfBirth", List.of("01", "12", "1928")));

		clickContinueOnInfoPage("howToAddDocuments", "Continue", "uploadDocuments");
		completeLaterDocsUploadFlow();
		clickContinueOnInfoPage("uploadDocuments", "Submit my documents", "documentSubmitConfirmation");
		assertThat(routingDecisionService.getRoutingDestinations(applicationData, CAF))
				.containsExactly(countyMap.get(County.getForName("Aitkin")));
	}

	@Test
	void routeLaterDocsToCorrectTribalNationOnly() throws Exception {
		postExpectingSuccess("identifyCountyOrTribalNation", "tribalNation", "Red Lake Nation");

		postExpectingSuccess("matchInfo", Map.of(
				"firstName", List.of("Dwight"), 
				"lastName", List.of("Schrute"),
				"dateOfBirth", List.of("01", "12", "1928")));

		clickContinueOnInfoPage("howToAddDocuments", "Continue", "uploadDocuments");
		completeLaterDocsUploadFlow();

		clickContinueOnInfoPage("uploadDocuments", "Submit my documents", "documentSubmitConfirmation");
		
		//No county was selected, so the blank county defaults to Hennepin
		var countyServicingAgency = County.getForName("Hennepin");
		var countyRoutingDestination = countyMap.get(countyServicingAgency);

		var servicingAgency = TribalNation.getFromName("Red Lake Nation");
		var tribalRoutingDestination = tribalNationMap.get(servicingAgency);
		List<RoutingDestination> routingDestinations = routingDecisionService.getRoutingDestinations(applicationData,
				CAF);

		assertThat(routingDestinations).containsExactly(countyRoutingDestination, tribalRoutingDestination);
	}

	@Test
	void routeLaterDocsToCorrectCountyAndTribalNation() throws Exception {
		postExpectingSuccess("identifyCountyOrTribalNation", Map.of(
						"tribalNation", List.of("Red Lake Nation"), 
						"county", List.of("Aitkin")));

		postExpectingSuccess("matchInfo", Map.of(
				"firstName", List.of("Dwight"), 
				"lastName", List.of("Schrute"),
				"dateOfBirth", List.of("01", "12", "1928")));

		clickContinueOnInfoPage("howToAddDocuments", "Continue", "uploadDocuments");
		completeLaterDocsUploadFlow();

		clickContinueOnInfoPage("uploadDocuments", "Submit my documents", "documentSubmitConfirmation");

		var countyServicingAgency = County.getForName("Aitkin");
		var countyRoutingDestination = countyMap.get(countyServicingAgency);

		var tribalServicingAgency = TribalNation.getFromName("Red Lake Nation");
		var tribalRoutingDestination = tribalNationMap.get(tribalServicingAgency);

		List<RoutingDestination> routingDestinations = routingDecisionService.getRoutingDestinations(applicationData,
				CAF);

		assertThat(routingDestinations).containsExactly(countyRoutingDestination, tribalRoutingDestination);
	}

	private void completeLaterDocsUploadFlow() throws Exception {
		String filePath = getAbsoluteFilepathString(UPLOADED_JPG_FILE_NAME);
		var jpgFile = new MockMultipartFile(UPLOADED_JPG_FILE_NAME, new FileInputStream(filePath));
		mockMvc.perform(multipart("/document-upload").file(jpgFile).session(session).with(csrf()));
	}

}
