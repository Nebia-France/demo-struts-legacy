package fr.demo.souscription.service;

import fr.demo.souscription.model.Draft;
import fr.demo.souscription.model.Simulation;
import fr.demo.souscription.model.Subscription;
import fr.demo.souscription.repository.SubscriptionRepository;
import java.sql.SQLException;
import java.time.Clock;

public final class SubscriptionService {
    private final SubscriptionRepository repository;
    private final SimulationService simulations;
    private final Clock clock;

    public SubscriptionService(SubscriptionRepository repository, SimulationService simulations, Clock clock) {
        this.repository = repository;
        this.simulations = simulations;
        this.clock = clock;
    }

    public Subscription confirm(Draft draft) throws SQLException {
        if (draft.getSubscriber() == null || draft.getSimulation() == null) {
            throw new IllegalStateException("Parcours incomplet");
        }
        Simulation source = draft.getSimulation();
        Simulation recalculated = simulations.calculate(source.getInitial(), source.getMonthly(),
                source.getYears(), draft.getProduct().getAnnualRate());
        return repository.save(draft.getConfirmationKey(), draft.getProduct(),
                draft.getSubscriber(), recalculated, clock.instant());
    }
}
