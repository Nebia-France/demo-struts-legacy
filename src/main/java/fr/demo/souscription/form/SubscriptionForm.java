package fr.demo.souscription.form;

import org.apache.struts.action.ActionForm;

public final class SubscriptionForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private String birthDate = "";
    private String initial = "1000,00";
    private String monthly = "100,00";
    private String years = "5";
    private boolean accepted;

    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { firstName = safe(value); }
    public String getLastName() { return lastName; }
    public void setLastName(String value) { lastName = safe(value); }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = safe(value); }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String value) { birthDate = safe(value); }
    public String getInitial() { return initial; }
    public void setInitial(String value) { initial = safe(value); }
    public String getMonthly() { return monthly; }
    public void setMonthly(String value) { monthly = safe(value); }
    public String getYears() { return years; }
    public void setYears(String value) { years = safe(value); }
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean value) { accepted = value; }
    private String safe(String value) { return value == null ? "" : value; }
}
