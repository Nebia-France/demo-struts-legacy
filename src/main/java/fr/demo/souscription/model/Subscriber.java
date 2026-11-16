package fr.demo.souscription.model;

import java.io.Serializable;
import java.time.LocalDate;

public final class Subscriber implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final LocalDate birthDate;

    public Subscriber(String firstName, String lastName, String email, LocalDate birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getBirthDateLabel() {
        return birthDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
