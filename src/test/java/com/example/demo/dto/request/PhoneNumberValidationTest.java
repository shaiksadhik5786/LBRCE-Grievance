package com.example.demo.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class PhoneNumberValidationTest {
    private static final String PHONE_ERROR = "Phone number must contain exactly 10 digits.";
    private static ValidatorFactory factory;
    private static Validator validator;
    private static final List<Function<String, Object>> OPTIONAL_PHONE_FORMS = List.of(
            value -> { StudentRequest form = new StudentRequest(); form.setPhone(value); return form; },
            value -> { StaffRequest form = new StaffRequest(); form.setPhone(value); return form; },
            value -> { AdminRequest form = new AdminRequest(); form.setPhone(value); return form; });

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() { factory.close(); }

    @Test
    void acceptsOnlyTenAsciiDigitsWhenPhoneIsProvidedForOptionalContactFields() {
        for (Function<String, Object> form : OPTIONAL_PHONE_FORMS) {
            assertPhoneValid(form.apply("9876543210"));
            assertPhoneValid(form.apply(""));
            assertPhoneValid(form.apply(null));
            for (String invalid : List.of("987654321", "98765432101", "98765abcde", "+919876543210", "98765-43210", "98765 43210")) {
                assertEquals(PHONE_ERROR, phoneError(form.apply(invalid)), invalid);
            }
        }
    }

    @Test
    void registrationRequiresExactlyTenAsciiDigits() {
        for (String valid : List.of("9876543210")) assertPhoneValid(registrationForm(valid));
        for (String invalid : List.of("", "987654321", "98765432101", "98765abcde", "+919876543210", "98765-43210", "98765 43210")) {
            assertEquals(PHONE_ERROR, phoneError(registrationForm(invalid)), invalid);
        }
        assertEquals(PHONE_ERROR, phoneError(registrationForm(null)));
    }

    private static StudentRegistrationRequestDto registrationForm(String value) {
        StudentRegistrationRequestDto form = new StudentRegistrationRequestDto();
        form.setPhone(value);
        return form;
    }

    private static String phoneError(Object form) {
        return validator.validate(form).stream()
                .filter(error -> error.getPropertyPath().toString().equals("phone"))
                .map(error -> error.getMessage()).findFirst().orElse(null);
    }

    private static void assertPhoneValid(Object form) {
        assertTrue(validator.validate(form).stream().noneMatch(error -> error.getPropertyPath().toString().equals("phone")));
    }
}
