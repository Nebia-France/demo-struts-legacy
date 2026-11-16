package fr.demo.souscription.service;

import fr.demo.souscription.model.Draft;
import fr.demo.souscription.model.Simulation;
import fr.demo.souscription.model.Subscriber;
import fr.demo.souscription.model.Subscription;
import fr.demo.souscription.repository.SubscriptionRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceTest {
    @TempDir Path directory;

    @Test
    void recomputesAuthoritativeResultAndRetainsValuesWhenEditingIdentity() throws Exception {
        SubscriptionRepository repository = new SubscriptionRepository("jdbc:h2:file:" + directory.resolve("demo"));
        repository.initialize();
        SubscriptionService service = new SubscriptionService(repository, new SimulationService(),
                Clock.fixed(Instant.parse("2026-11-07T10:00:00Z"), ZoneOffset.UTC));
        Draft draft = new Draft(new CatalogService().getProducts().get(0));
        draft.setSubscriber(new Subscriber("Camille", "Martin", "camille@example.test", LocalDate.of(1980, 1, 1)));
        draft.setSimulation(new Simulation(new BigDecimal("1000.00"), new BigDecimal("0.00"), 1,
                BigDecimal.ZERO, new BigDecimal("999999.00")));
        String oldKey = draft.getConfirmationKey();
        draft.setSubscriber(new Subscriber("Camille", "Durand", "camille@example.test", LocalDate.of(1980, 1, 1)));
        assertNotEquals(oldKey, draft.getConfirmationKey());
        assertNotNull(draft.getSimulation());
        Subscription saved = service.confirm(draft);
        assertEquals(new BigDecimal("1000.00"), saved.getSimulation().getContributed());
        assertNotEquals(new BigDecimal("999999.00"), saved.getSimulation().getFinalCapital());
        assertEquals("Durand", saved.getSubscriber().getLastName());
        assertEquals(saved.getReference(), service.confirm(draft).getReference());
    }

    @Test
    void refusesIncompleteJourneysBeforeWriting() {
        SubscriptionRepository repository = new SubscriptionRepository("jdbc:h2:file:" + directory.resolve("unused"));
        SubscriptionService service = new SubscriptionService(repository, new SimulationService(), Clock.systemUTC());
        assertThrows(IllegalStateException.class, () -> service.confirm(new Draft(new CatalogService().getProducts().get(0))));
    }
}
