package com.example.demo.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.config.OtpProperties;

class BrevoOtpServiceTest {
    private static final String BASE_URL = "https://otp.example";
    private static final String API_KEY = "server-secret-api-key";

    @Test
    void sendsExpectedServerSideAuthenticatedRequest() {
        Fixture fixture = fixture(API_KEY);
        fixture.server.expect(requestTo(BASE_URL + "/api/otp/send"))
                .andExpect(header("X-API-Key", API_KEY))
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));
        fixture.service.send("student@example.com");
        fixture.server.verify();
    }

    @Test
    void invalidOtpIsRejectedSafely() {
        Fixture fixture = fixture(API_KEY);
        fixture.server.expect(requestTo(BASE_URL + "/api/otp/verify"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> fixture.service.verify("student@example.com", "123456"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertFalse(error.getReason().contains("123456"));
    }

    @Test
    void externalFailureAndTimeoutReturnSafeUnavailableMessage() {
        Fixture serverFailure = fixture(API_KEY);
        serverFailure.server.expect(requestTo(BASE_URL + "/api/otp/send"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        assertSafeUnavailable(assertThrows(ResponseStatusException.class,
                () -> serverFailure.service.send("student@example.com")));

        Fixture timeout = fixture(API_KEY);
        timeout.server.expect(requestTo(BASE_URL + "/api/otp/send"))
                .andRespond(withException(new SocketTimeoutException("timed out contacting internal host")));
        assertSafeUnavailable(assertThrows(ResponseStatusException.class,
                () -> timeout.service.send("student@example.com")));
    }

    @Test
    void missingApiKeyFailsSafelyWithoutExposingConfiguration() {
        Fixture fixture = fixture("");
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> fixture.service.send("student@example.com"));
        assertSafeUnavailable(error);
        assertFalse(error.getReason().contains(API_KEY));
        assertFalse(error.getReason().contains(BASE_URL));
    }

    @Test
    void authenticationFailureNeverReturnsApiKey() {
        Fixture fixture = fixture(API_KEY);
        fixture.server.expect(requestTo(BASE_URL + "/api/otp/send"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> fixture.service.send("student@example.com"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
        assertFalse(error.getReason().contains(API_KEY));
    }

    private void assertSafeUnavailable(ResponseStatusException error) {
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
        assertFalse(error.getReason().contains(BASE_URL));
        assertFalse(error.getReason().toLowerCase().contains("exception"));
    }

    private Fixture fixture(String apiKey) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OtpProperties properties = new OtpProperties(BASE_URL, apiKey, Duration.ofMinutes(10),
                Duration.ofSeconds(1), Duration.ofSeconds(1));
        return new Fixture(new BrevoOtpService(builder.build(), properties), server);
    }

    private record Fixture(BrevoOtpService service, MockRestServiceServer server) {}
}
