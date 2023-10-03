package org.codeforamerica.shiba.mnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.codeforamerica.shiba.pages.rest.CommunicationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import com.google.gson.JsonObject;

@SpringBootTest
@ActiveProfiles("test")
public class CommHubServiceTest {
	
	@Autowired
	private CommunicationClient communicationClient;
    
	@MockBean
	private Clock clock;
    
    private MockRestServiceServer mockServer;
    @Value("${comm-hub.url}")
    private String commHubURL;
    
    @Value("${comm-hub.max-attempts}")
    private String maxAttempts;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.bindTo(communicationClient.getCommHubRestServiceTemplate()).build();
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }
    
    
	/*
     * This test verifies retry logic when the response from the comm-hub is an HTTP 200 (OK).
     * In this case the CommunicationClient will not retry.
     */
	@Test
	public void sendObjectToCommHubSuccess() {
    	// Create a mock web server to respond to comm-hub request(s) with an HTTP 200 OK.
    	// In this case we expect just one request.
        mockServer
        .expect(ExpectedCount.times(1), requestTo(commHubURL))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("<200 OK>"));
        
        // The request pay load doesn't really matter because we mocked the HTTP 200 response.
        JsonObject request = new JsonObject();
        try {
        	communicationClient.send(request);
        } catch(Exception e) {
        	// The assertion in this catch clause is here to verify that CommunicationClient did not throw
        	// an exception.  If it does we fail the test.
        	fail();
        }
        
        // This verifies that the mock server responded to just the one request.
        // Any more or any less will fail the verify.
        mockServer.verify(Duration.ofSeconds(10));
  	}
	
	/*
	 * This test verifies retry logic when the response from the comm-hub is an HTTP 503
	 * And then retires once to a HTTP 200
	 */
	@Test
	public void sendObjectToCommHubExpectedRetriesThenSuccess() {

		JsonObject appJsonObject = new JsonObject();
		appJsonObject.addProperty("whatever", "whatever");
		
		mockServer.expect(ExpectedCount.times(1), requestTo(commHubURL))
		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
				.contentType(MediaType.APPLICATION_JSON)
				.body("<503 SERVICE UNAVAILABLE>"));
		
		mockServer.expect(ExpectedCount.times(1), requestTo(commHubURL))
		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body("<200 OK>"));

		try {
			communicationClient.send(appJsonObject);
		} catch(Exception e) {
			fail();
		}

	}
    
	/*
     * This test verifies retry logic when the response from the comm-hub is an HTTP 503 (Service Unavailable).
     * In this case the CommunicationClient will retry (the max-attempts).
     * After exhausting retries we will catch the last exception and and verify that the expected number of
     * requests were made and that the exception thrown matches what we expect.
     */
	@Test
	public void sendObjectToCommHubExpectedRetriesThenFailure() {
		JsonObject appJsonObject = new JsonObject();
		appJsonObject.addProperty("whatever", "whatever");
		
    	// Create a mock web server to respond to comm-hub request(s) with an HTTP 503 Service Unavailable error.
    	// In this case we expect the max number of retry attempts/requests.
		mockServer.expect(ExpectedCount.times(Integer.parseInt(maxAttempts)), requestTo(commHubURL))
		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
				.contentType(MediaType.APPLICATION_JSON)
				.body("<503 SERVICE UNAVAILABLE>"));

		try {
			communicationClient.send(appJsonObject);
		} catch(Exception e) {
			// Verify that all three requests were made.
			mockServer.verify(Duration.ofSeconds(10));
			// Verify that the send method re-throws the HTTP 503.
			assertTrue(e.getMessage().contains("503"));
		}
	}
    
    
    /*
     * This test verifies correct retry logic when the response from the comm-hub is an HTTP 500 (Internal Server Error).
     * In this case there should not be any retires.
     */
    @Test                                                                                          
    public void sendObjectToCommHubErrorResponseHandling() {
    	// Create a mock web server to respond to comm-hub request(s) with an HTTP 500 Internal Server Error.
    	// In this case we expect just one request.
        mockServer
        .expect(ExpectedCount.times(1), requestTo(commHubURL))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body("<500 INTERNAL SERVER ERROR>"));
        
        // The request pay load doesn't really matter because we mocked the HTTP 500 response.
        JsonObject request = new JsonObject();
        try {
        	communicationClient.send(request);
        } catch(Exception e) {
        	// The assertion in this catch clause is here to verify that CommunicationClient did not throw
        	// an exception for the HTTP 500 error.  If it does we fail the test.
        	fail();
        }
        
        // This verifies that the mock server responded to just the one request.
        // Any more or any less will fail the verify.
        mockServer.verify(Duration.ofSeconds(10));
    }

}
