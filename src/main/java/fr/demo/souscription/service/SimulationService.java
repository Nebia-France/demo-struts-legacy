package fr.demo.souscription.service;

import fr.demo.souscription.model.Simulation;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class SimulationService {
    private static final MathContext PRECISION = MathContext.DECIMAL128;

    public Simulation calculate(BigDecimal initial, BigDecimal monthly, int years, BigDecimal annualRate) {
        if (initial == null || monthly == null || annualRate == null
                || initial.scale() > 2 || monthly.scale() > 2
                || initial.compareTo(new BigDecimal("100")) < 0
                || initial.compareTo(new BigDecimal("100000")) > 0
                || monthly.signum() < 0 || monthly.compareTo(new BigDecimal("2000")) > 0
                || years < 1 || years > 30 || annualRate.signum() < 0
                || annualRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Paramètres de simulation invalides");
        }
        BigDecimal factor = BigDecimal.ONE.add(annualRate.divide(new BigDecimal("12"), PRECISION));
        BigDecimal capital = initial;
        for (int month = 0; month < years * 12; month++) {
            capital = capital.multiply(factor, PRECISION).add(monthly, PRECISION);
        }
        BigDecimal contributed = initial.add(monthly.multiply(BigDecimal.valueOf(years * 12L)));
        return new Simulation(initial.setScale(2), monthly.setScale(2), years,
                contributed.setScale(2), capital.setScale(2, RoundingMode.HALF_UP));
    }
}
