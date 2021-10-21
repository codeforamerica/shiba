package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test that uses a real FileNet client implementation (not mock). 
 *
 */
@Disabled
@SpringBootTest
@ActiveProfiles("test")
public class FileNetLiveTest {

	String fileContent = "fileContent";
	String fileName = "fileName";

	@MockBean
	private Clock clock;

//	@Autowired
//	private MnitCmisFilenetClient fileNetClient;
	
	@Autowired
	private MnitFilenetWebServiceClient fileNetClient;

	@MockBean
	private ApplicationRepository applicationRepository;

	private RoutingDestination olmsted;
	private RoutingDestination hennepin;

	@BeforeEach
	void setUp() {
		when(clock.instant()).thenReturn(Instant.now());
		when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
		olmsted = new CountyRoutingDestination();
		olmsted.setDhsProviderId("A000055800");
		olmsted.setFolderId("6875aa2f-8852-426f-a618-d394b9a32be5");

		hennepin = new CountyRoutingDestination();
		hennepin.setDhsProviderId("A000027200");
		hennepin.setFolderId("5195b061-9bdc-4d31-9840-90a99902d329");
	}

	@Test
	void testFileNet() {
		fileNetClient.send(new ApplicationFile(fileContent.getBytes(), fileName), hennepin, "someId", Document.CAF,
				FlowType.FULL);

		verify(applicationRepository).updateStatus("someId", Document.CAF, DELIVERED);
	}

}
