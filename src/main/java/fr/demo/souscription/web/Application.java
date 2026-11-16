package fr.demo.souscription.web;

import fr.demo.souscription.repository.SubscriptionRepository;
import fr.demo.souscription.service.CatalogService;
import fr.demo.souscription.service.InputValidator;
import fr.demo.souscription.service.SimulationService;
import fr.demo.souscription.service.SubscriptionService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.SQLException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.time.Clock;
import java.util.Enumeration;

public final class Application implements ServletContextListener {
    private CatalogService catalog;
    private InputValidator validator;
    private SimulationService simulations;
    private SubscriptionService subscriptions;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Class.forName("org.h2.Driver");
            Path database = Paths.get(System.getProperty("horizon.data", "data/souscriptions"))
                    .toAbsolutePath().normalize();
            Path directory = database.getParent();
            Files.createDirectories(directory);
            boolean posix = Files.getFileStore(directory).supportsFileAttributeView("posix");
            if (posix) { Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------")); }
            SubscriptionRepository repository = new SubscriptionRepository("jdbc:h2:file:" + database
                    + ";DB_CLOSE_ON_EXIT=FALSE;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0");
            repository.initialize();
            if (posix) {
                Files.setPosixFilePermissions(Paths.get(database + ".mv.db"),
                        PosixFilePermissions.fromString("rw-------"));
            }
            Clock clock = Clock.system(java.time.ZoneId.of("Europe/Paris"));
            catalog = new CatalogService();
            validator = new InputValidator(clock);
            simulations = new SimulationService();
            subscriptions = new SubscriptionService(repository, simulations, clock);
            event.getServletContext().setAttribute(Application.class.getName(), this);
        } catch (IOException | SQLException | ClassNotFoundException ex) {
            event.getServletContext().log("Initialisation de la base de données impossible ("
                    + ex.getClass().getSimpleName() + ").");
            throw new IllegalStateException("Initialisation de l'application impossible");
        }
    }

    public static Application get(ServletContext context) {
        Application application = (Application) context.getAttribute(Application.class.getName());
        if (application == null) { throw new IllegalStateException("Application non initialisée"); }
        return application;
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    event.getServletContext().log("Libération du pilote JDBC impossible ; SQLState="
                            + ex.getSQLState());
                }
            }
        }
    }

    public CatalogService getCatalog() { return catalog; }
    public InputValidator getValidator() { return validator; }
    public SimulationService getSimulations() { return simulations; }
    public SubscriptionService getSubscriptions() { return subscriptions; }
}
