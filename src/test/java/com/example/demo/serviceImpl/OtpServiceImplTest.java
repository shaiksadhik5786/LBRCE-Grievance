package com.example.demo.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.config.OtpProperties;
import com.example.demo.service.ExternalOtpService;
import com.example.demo.service.OtpPurpose;

@ExtendWith(OutputCaptureExtension.class)
class OtpServiceImplTest {
    private final ExternalOtpService external = mock(ExternalOtpService.class);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-08T00:00:00Z"));
    private final OtpProperties properties = new OtpProperties("https://otp.example", "secret", Duration.ofMinutes(10), Duration.ofSeconds(1), Duration.ofSeconds(1));
    private final OtpServiceImpl service = new OtpServiceImpl(external, properties, clock);

    @Test void normalizesEmailAndAcceptsOneMatchingPurpose() {
        String challenge = service.send(" Student@Example.com ", OtpPurpose.STUDENT_REGISTRATION);
        String token = service.verify("STUDENT@example.com", "123456", OtpPurpose.STUDENT_REGISTRATION, challenge);
        verify(external).send("student@example.com"); verify(external).verify("student@example.com", "123456");
        assertDoesNotThrow(() -> service.requireAndConsumeVerification("student@example.com", OtpPurpose.STUDENT_REGISTRATION, token));
        assertForbidden(() -> service.requireAndConsumeVerification("student@example.com", OtpPurpose.STUDENT_REGISTRATION, token));
    }

    @Test void verificationCannotBeAppliedToDifferentEmailOrPurpose() {
        String emailToken = verifiedToken("first@example.com", OtpPurpose.STUDENT_REGISTRATION, "123456");
        assertForbidden(() -> service.requireAndConsumeVerification("other@example.com", OtpPurpose.STUDENT_REGISTRATION, emailToken));
        String purposeToken = verifiedToken("first@example.com", OtpPurpose.STUDENT_REGISTRATION, "123456");
        assertForbidden(() -> service.requireAndConsumeVerification("first@example.com", OtpPurpose.ADMIN_CREATION, purposeToken));
    }

    @Test void sendingAnotherOtpInvalidatesEarlierVerification() {
        String token = verifiedToken("student@example.com", OtpPurpose.STUDENT_REGISTRATION, "123456");
        service.send("student@example.com", OtpPurpose.STUDENT_REGISTRATION);
        assertForbidden(() -> service.requireAndConsumeVerification("student@example.com", OtpPurpose.STUDENT_REGISTRATION, token));
    }

    @Test void expiredChallengeAndVerificationAreRejected() {
        String challenge = service.send("student@example.com", OtpPurpose.STUDENT_REGISTRATION); clock.advance(Duration.ofMinutes(11));
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.verify("student@example.com", "123456", OtpPurpose.STUDENT_REGISTRATION, challenge)).getStatusCode());
        String token = verifiedToken("student@example.com", OtpPurpose.STUDENT_REGISTRATION, "123456"); clock.advance(Duration.ofMinutes(11));
        assertForbidden(() -> service.requireAndConsumeVerification("student@example.com", OtpPurpose.STUDENT_REGISTRATION, token));
    }

    @Test void eachVerificationUsesAnUnpredictableFreshToken() {
        assertNotEquals(verifiedToken("student@example.com", OtpPurpose.STUDENT_REGISTRATION, "123456"),
                verifiedToken("student@example.com", OtpPurpose.STUDENT_REGISTRATION, "123456"));
    }

    @Test void supportsStudentStaffAndAdminCreationPurposes() {
        for (OtpPurpose purpose : java.util.List.of(OtpPurpose.STUDENT_REGISTRATION,
                OtpPurpose.STAFF_CREATION, OtpPurpose.ADMIN_CREATION)) {
            String email = purpose.name().toLowerCase() + "@example.com";
            String token = verifiedToken(email, purpose, "123456");
            assertDoesNotThrow(() -> service.requireAndConsumeVerification(email, purpose, token));
        }
    }

    @Test void verifiesLoginChallengeWithoutCreatingReusableRegistrationToken() {
        String challenge = service.send("admin@example.com", OtpPurpose.ADMIN_LOGIN);
        assertEquals("admin@example.com", service.verifyLogin("123456", OtpPurpose.ADMIN_LOGIN, challenge));
        assertThrows(ResponseStatusException.class,
                () -> service.verifyLogin("123456", OtpPurpose.ADMIN_LOGIN, challenge));
    }

    @Test void otpValueIsNotLogged(CapturedOutput output) {
        verifiedToken("student@example.com", OtpPurpose.STUDENT_REGISTRATION, "654321");
        assertFalse(output.getAll().contains("654321"));
    }

    private String verifiedToken(String email, OtpPurpose purpose, String otp) {
        return service.verify(email, otp, purpose, service.send(email, purpose));
    }
    private void assertForbidden(org.junit.jupiter.api.function.Executable action) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, action);
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }
    private static final class MutableClock extends Clock {
        private Instant instant; private MutableClock(Instant instant) { this.instant = instant; }
        void advance(Duration duration) { instant = instant.plus(duration); }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
