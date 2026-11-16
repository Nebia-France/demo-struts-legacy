package fr.demo.souscription.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Subscription implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String reference;
    private final Product product;
    private final Subscriber subscriber;
    private final Simulation simulation;
    private final Instant createdAt;

    public Subscription(String reference, Product product, Subscriber subscriber,
                        Simulation simulation, Instant createdAt) {
        this.reference = reference;
        this.product = product;
        this.subscriber = subscriber;
        this.simulation = simulation;
        this.createdAt = createdAt;
    }

    public String getReference() { return reference; }
    public Product getProduct() { return product; }
    public Subscriber getSubscriber() { return subscriber; }
    public Simulation getSimulation() { return simulation; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedAtLabel() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm")
                .withZone(ZoneId.of("Europe/Paris")).format(createdAt);
    }
}
