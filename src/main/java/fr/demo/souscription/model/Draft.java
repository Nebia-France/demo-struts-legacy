package fr.demo.souscription.model;

import java.io.Serializable;
import java.util.UUID;

public final class Draft implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Product product;
    private Subscriber subscriber;
    private Simulation simulation;
    private String confirmationKey = UUID.randomUUID().toString();

    public Draft(Product product) { this.product = product; }
    public Product getProduct() { return product; }
    public Subscriber getSubscriber() { return subscriber; }
    public Simulation getSimulation() { return simulation; }
    public String getConfirmationKey() { return confirmationKey; }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
        this.confirmationKey = UUID.randomUUID().toString();
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.confirmationKey = UUID.randomUUID().toString();
    }
}
