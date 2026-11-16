package fr.demo.souscription.service;

import fr.demo.souscription.model.Product;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class CatalogService {
    private final List<Product> products = Collections.unmodifiableList(Arrays.asList(
        new Product("PRUDENT", "Horizon Prudent", "Prudent",
            "Une hypothèse de progression modérée pour découvrir le fonctionnement de l'épargne.", "0.02"),
        new Product("EQUILIBRE", "Horizon Équilibre", "Équilibré",
            "Une hypothèse intermédiaire pour construire un projet dans la durée.", "0.04"),
        new Product("DYNAMIQUE", "Horizon Dynamique", "Dynamique",
            "Une hypothèse de progression plus élevée, associée à un profil de risque supérieur.", "0.06")
    ));

    public List<Product> getProducts() { return products; }
    public Optional<Product> find(String code) {
        return products.stream().filter(product -> product.getCode().equals(code)).findFirst();
    }
}
