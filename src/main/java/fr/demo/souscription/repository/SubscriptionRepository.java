package fr.demo.souscription.repository;

import fr.demo.souscription.model.Product;
import fr.demo.souscription.model.Simulation;
import fr.demo.souscription.model.Subscriber;
import fr.demo.souscription.model.Subscription;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class SubscriptionRepository {
    private final String url;

    public SubscriptionRepository(String url) { this.url = url; }

    public void initialize() throws SQLException, IOException {
        StringBuilder schema = new StringBuilder();
        try (InputStream stream = SubscriptionRepository.class.getResourceAsStream("/db/schema.sql")) {
            if (stream == null) { throw new IOException("Schéma H2 absent"); }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                char[] buffer = new char[2048];
                int count;
                while ((count = reader.read(buffer)) != -1) { schema.append(buffer, 0, count); }
            }
        }
        try (Connection connection = open(); Statement statement = connection.createStatement()) {
            statement.execute(schema.toString());
        }
    }

    public Subscription save(String key, Product product, Subscriber subscriber,
                             Simulation simulation, Instant now) throws SQLException {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                Optional<Subscription> existing = find(connection, key);
                if (existing.isPresent()) {
                    connection.commit();
                    return existing.get();
                }
                String reference = UUID.randomUUID().toString();
                String sql = "INSERT INTO subscriptions (reference, confirmation_key, product_code, "
                        + "product_name, product_profile, annual_rate, first_name, last_name, email, "
                        + "birth_date, initial_amount, monthly_amount, duration_years, contributed, "
                        + "final_capital, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, reference);
                    statement.setString(2, key);
                    statement.setString(3, product.getCode());
                    statement.setString(4, product.getName());
                    statement.setString(5, product.getProfile());
                    statement.setBigDecimal(6, product.getAnnualRate());
                    statement.setString(7, subscriber.getFirstName());
                    statement.setString(8, subscriber.getLastName());
                    statement.setString(9, subscriber.getEmail());
                    statement.setDate(10, Date.valueOf(subscriber.getBirthDate()));
                    statement.setBigDecimal(11, simulation.getInitial());
                    statement.setBigDecimal(12, simulation.getMonthly());
                    statement.setInt(13, simulation.getYears());
                    statement.setBigDecimal(14, simulation.getContributed());
                    statement.setBigDecimal(15, simulation.getFinalCapital());
                    statement.setTimestamp(16, Timestamp.from(now));
                    statement.executeUpdate();
                }
                connection.commit();
                return new Subscription(reference, product, subscriber, simulation, now);
            } catch (SQLException ex) {
                connection.rollback();
                if ("23505".equals(ex.getSQLState())) {
                    Optional<Subscription> existing = find(connection, key);
                    if (existing.isPresent()) { return existing.get(); }
                }
                throw ex;
            }
        }
    }

    public Optional<Subscription> findByKey(String key) throws SQLException {
        try (Connection connection = open()) { return find(connection, key); }
    }

    private Optional<Subscription> find(Connection connection, String key) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM subscriptions WHERE confirmation_key = ?")) {
            statement.setString(1, key);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) { return Optional.empty(); }
                Product product = new Product(row.getString("product_code"), row.getString("product_name"),
                        row.getString("product_profile"), "", row.getBigDecimal("annual_rate").toPlainString());
                Subscriber subscriber = new Subscriber(row.getString("first_name"), row.getString("last_name"),
                        row.getString("email"), row.getDate("birth_date").toLocalDate());
                Simulation simulation = new Simulation(row.getBigDecimal("initial_amount"),
                        row.getBigDecimal("monthly_amount"), row.getInt("duration_years"),
                        row.getBigDecimal("contributed"), row.getBigDecimal("final_capital"));
                return Optional.of(new Subscription(row.getString("reference"), product, subscriber,
                        simulation, row.getTimestamp("created_at").toInstant()));
            }
        }
    }

    private Connection open() throws SQLException { return DriverManager.getConnection(url, "sa", ""); }
}
