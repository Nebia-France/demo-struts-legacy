package fr.demo.souscription.service;

import fr.demo.souscription.form.SubscriptionForm;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {
    private final InputValidator validator = new InputValidator(
            Clock.fixed(Instant.parse("2026-11-07T10:00:00Z"), ZoneOffset.UTC));

    private SubscriptionForm subscriber() {
        SubscriptionForm form = new SubscriptionForm();
        form.setFirstName("Camille <Démo>");
        form.setLastName("O'Martin");
        form.setEmail("camille@example.test");
        form.setBirthDate("07/11/2008");
        return form;
    }

    @Test
    void acceptsExactlyEighteenAndPreservesEscapableText() {
        SubscriptionForm form = subscriber();
        assertTrue(validator.subscriberErrors(form).isEmpty());
        assertEquals("Camille <Démo>", validator.subscriber(form).getFirstName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"08/11/2008", "01/01/2099", "31/02/1980", "29/02/2001", "", "1980-01-01", "1/1/1980"})
    void refusesMinorFutureAndMalformedBirthDates(String date) {
        SubscriptionForm form = subscriber();
        form.setBirthDate(date);
        assertTrue(validator.subscriberErrors(form).containsKey("birthDate"));
    }

    @Test
    void acceptsLeapDayAndRejectsControlCharactersAndBadEmails() {
        SubscriptionForm form = subscriber();
        form.setBirthDate("29/02/2000");
        assertTrue(validator.subscriberErrors(form).isEmpty());
        form.setFirstName("Jean\nDémo");
        form.setEmail("x<script>@example.test");
        assertTrue(validator.subscriberErrors(form).containsKey("firstName"));
        assertTrue(validator.subscriberErrors(form).containsKey("email"));
    }

    @Test
    void parsesFrenchAndDotDecimalsAndAcceptsInclusiveBounds() {
        assertEquals(new BigDecimal("100.01"), validator.amount("100,01"));
        assertEquals(new BigDecimal("100.01"), validator.amount("100.01"));
        SubscriptionForm form = new SubscriptionForm();
        form.setInitial("100000,00");
        form.setMonthly("2000,00");
        form.setYears("30");
        assertTrue(validator.simulationErrors(form).isEmpty());
        form.setInitial("100");
        form.setMonthly("0");
        form.setYears("1");
        assertTrue(validator.simulationErrors(form).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "1e3", "100,001", "1 000,00", "1.000,00", "NaN", "", "100.", "100,", "1000000", " 100"})
    void refusesAmbiguousMoney(String value) { assertNull(validator.amount(value)); }

    @Test
    void enforcesFinancialLimitsAndIntegerYears() {
        SubscriptionForm form = new SubscriptionForm();
        form.setInitial("99,99");
        form.setMonthly("2000,01");
        form.setYears("1.5");
        assertEquals(3, validator.simulationErrors(form).size());
    }
}
