package fr.demo.souscription.service;

import fr.demo.souscription.form.SubscriptionForm;
import fr.demo.souscription.model.Subscriber;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InputValidator {
    private final Clock clock;

    public InputValidator(Clock clock) { this.clock = clock; }

    public Map<String, String> subscriberErrors(SubscriptionForm form) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateName(form.getFirstName(), "firstName", "prénom", errors);
        validateName(form.getLastName(), "lastName", "nom", errors);
        String email = form.getEmail().trim();
        if (email.length() > 254 || !email.matches("[^\\s@<>]+@[^\\s@<>]+\\.[^\\s@<>]+")) {
            errors.put("email", "Saisissez une adresse électronique valide (254 caractères maximum).");
        }
        LocalDate birth = birthDate(form.getBirthDate());
        LocalDate today = LocalDate.now(clock);
        if (birth == null) {
            errors.put("birthDate", "Saisissez une date valide au format JJ/MM/AAAA.");
        } else if (birth.isAfter(today)) {
            errors.put("birthDate", "La date de naissance ne peut pas être dans le futur.");
        } else if (birth.isAfter(today.minusYears(18))) {
            errors.put("birthDate", "La souscription est réservée aux personnes majeures.");
        }
        return errors;
    }

    private void validateName(String value, String field, String label, Map<String, String> errors) {
        if (value.trim().isEmpty() || value.trim().length() > 80
                || value.codePoints().anyMatch(Character::isISOControl)) {
            errors.put(field, "Saisissez votre " + label + " (1 à 80 caractères, sans caractère de contrôle).");
        }
    }

    public Subscriber subscriber(SubscriptionForm form) {
        if (!subscriberErrors(form).isEmpty()) {
            throw new IllegalArgumentException("Souscripteur invalide");
        }
        return new Subscriber(form.getFirstName().trim(), form.getLastName().trim(),
                form.getEmail().trim(), birthDate(form.getBirthDate()));
    }

    public Map<String, String> simulationErrors(SubscriptionForm form) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateAmount(form.getInitial(), "initial", "100", "100000",
                "Le versement initial doit être compris entre 100,00 et 100 000,00 €.", errors);
        validateAmount(form.getMonthly(), "monthly", "0", "2000",
                "La mensualité doit être comprise entre 0,00 et 2 000,00 €.", errors);
        if (!form.getYears().matches("(?:[1-9]|[12][0-9]|30)")) {
            errors.put("years", "La durée doit être un nombre entier de 1 à 30 ans.");
        }
        return errors;
    }

    private void validateAmount(String raw, String field, String min, String max,
                                String message, Map<String, String> errors) {
        BigDecimal value = amount(raw);
        if (value == null || value.compareTo(new BigDecimal(min)) < 0
                || value.compareTo(new BigDecimal(max)) > 0) {
            errors.put(field, message + " Utilisez au maximum deux décimales, sans séparateur de milliers.");
        }
    }

    public BigDecimal amount(String raw) {
        if (raw == null || !raw.matches("[0-9]{1,6}(?:[.,][0-9]{1,2})?")) {
            return null;
        }
        return new BigDecimal(raw.replace(',', '.'));
    }

    private LocalDate birthDate(String raw) {
        if (!raw.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) { return null; }
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
