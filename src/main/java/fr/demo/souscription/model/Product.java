package fr.demo.souscription.model;

import java.io.Serializable;
import java.math.BigDecimal;

public final class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String code;
    private final String name;
    private final String profile;
    private final String description;
    private final BigDecimal annualRate;

    public Product(String code, String name, String profile, String description, String rate) {
        this.code = code;
        this.name = name;
        this.profile = profile;
        this.description = description;
        this.annualRate = new BigDecimal(rate);
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getProfile() { return profile; }
    public String getDescription() { return description; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public BigDecimal getRatePercent() { return annualRate.multiply(new BigDecimal("100")); }
}
