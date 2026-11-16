package fr.demo.souscription.repository;

import fr.demo.souscription.model.Product;
import fr.demo.souscription.model.Simulation;
import fr.demo.souscription.model.Subscriber;
import fr.demo.souscription.model.Subscription;
import fr.demo.souscription.service.CatalogService;
import fr.demo.souscription.service.SimulationService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionRepositoryTest {
    @TempDir Path directory;
    private final Product product = new CatalogService().getProducts().get(1);
    private final Subscriber subscriber = new Subscriber("Camille", "Martin", "camille@example.test",
            LocalDate.of(1980, 1, 1));
    private final Simulation simulation = new SimulationService().calculate(new BigDecimal("1000"),
            new BigDecimal("100"), 5, product.getAnnualRate());
    private final Instant now = Instant.parse("2026-11-07T10:00:00Z");

    private String url() { return "jdbc:h2:file:" + directory.resolve("subscriptions"); }

    @Test
    void survivesClosingAndReopeningAndPreservesTheProductSnapshot() throws Exception {
        SubscriptionRepository repository = new SubscriptionRepository(url());
        repository.initialize();
        String key = UUID.randomUUID().toString();
        Subscription saved = repository.save(key, product, subscriber, simulation, now);
        SubscriptionRepository reopened = new SubscriptionRepository(url());
        reopened.initialize();
        Subscription loaded = reopened.findByKey(key).orElseThrow(AssertionError::new);
        assertEquals(saved.getReference(), loaded.getReference());
        assertEquals(product.getName(), loaded.getProduct().getName());
        assertEquals(0, product.getAnnualRate().compareTo(loaded.getProduct().getAnnualRate()));
        assertEquals(simulation.getFinalCapital(), loaded.getSimulation().getFinalCapital());
        assertEquals(now, loaded.getCreatedAt());
        assertEquals(subscriber.getBirthDate(), loaded.getSubscriber().getBirthDate());
    }

    @Test
    void concurrentDuplicateConfirmationsCreateOnlyOneRow() throws Exception {
        SubscriptionRepository repository = new SubscriptionRepository(url());
        repository.initialize();
        String key = UUID.randomUUID().toString();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> jobs = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                jobs.add(() -> repository.save(key, product, subscriber, simulation, now).getReference());
            }
            List<Future<String>> results = executor.invokeAll(jobs);
            String reference = results.get(0).get();
            for (Future<String> result : results) { assertEquals(reference, result.get()); }
            try (java.sql.Connection connection = DriverManager.getConnection(url(), "sa", "");
                 java.sql.Statement statement = connection.createStatement();
                 java.sql.ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM subscriptions")) {
                assertTrue(rows.next());
                assertEquals(1, rows.getInt(1));
            }
        } finally { executor.shutdownNow(); }
    }

    @Test
    void failedInsertDoesNotProduceAConfirmation() throws Exception {
        SubscriptionRepository repository = new SubscriptionRepository(url());
        repository.initialize();
        String key = UUID.randomUUID().toString();
        Subscriber invalid = new Subscriber("Camille", "Martin", String.join("", java.util.Collections.nCopies(300, "x")),
                LocalDate.of(1980, 1, 1));
        assertThrows(SQLException.class, () -> repository.save(key, product, invalid, simulation, now));
        assertFalse(repository.findByKey(key).isPresent());
        assertNotNull(repository.save(key, product, subscriber, simulation, now));
    }
}
