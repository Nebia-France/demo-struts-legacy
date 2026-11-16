package fr.demo.souscription.model;

import java.io.Serializable;
import java.math.BigDecimal;

public final class Simulation implements Serializable {
    private static final long serialVersionUID = 1L;
    private final BigDecimal initial;
    private final BigDecimal monthly;
    private final int years;
    private final BigDecimal contributed;
    private final BigDecimal finalCapital;

    public Simulation(BigDecimal initial, BigDecimal monthly, int years,
                      BigDecimal contributed, BigDecimal finalCapital) {
        this.initial = initial;
        this.monthly = monthly;
        this.years = years;
        this.contributed = contributed;
        this.finalCapital = finalCapital;
    }

    public BigDecimal getInitial() { return initial; }
    public BigDecimal getMonthly() { return monthly; }
    public int getYears() { return years; }
    public BigDecimal getContributed() { return contributed; }
    public BigDecimal getFinalCapital() { return finalCapital; }
    public BigDecimal getGains() { return finalCapital.subtract(contributed); }
}
