import dsa.SkillIndexer;
import server.SimpleHttpServer;
import storage.DataStore;

/**
 * Main Application Entry Point
 * Résumé–Job Matching & Talent-Marketplace Engine
 *
 * Course: Data Structures and Algorithms - 3 (25CS2103E)
 * Team: 14 | Section: 10
 * Institution: KL University, Hyderabad
 * Students: Vakkalanka Sai Bhaskara Sujit (2520090085), Mahankali Sai Tarun (2520090020)
 * Guide: K. Chandusha, Asst. Prof., CS&IT
 */
public class Main {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            System.out.println("Initializing Skill Inverted Index (HashMap / HashSet)...");
            SkillIndexer skillIndexer = new SkillIndexer();

            System.out.println("Loading DataStore & Seeding Realistic Technical Profiles...");
            DataStore dataStore = new DataStore(skillIndexer);

            System.out.println("Starting Java Standard Library HttpServer on port " + port + "...");
            SimpleHttpServer server = new SimpleHttpServer(port, dataStore, skillIndexer);
            server.start();

            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down Talent Engine HTTP Server...");
                server.stop();
                System.out.println("Server stopped.");
            }));

        } catch (Exception e) {
            System.err.println("Fatal error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
