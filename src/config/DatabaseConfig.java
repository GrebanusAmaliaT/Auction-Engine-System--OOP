package src.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static Connection connection;

    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASS = System.getenv("DB_PASS");

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASS == null) {
            throw new SQLException("Database environment variables are not set. Please set DB_URL, DB_USER and DB_PASS.");
        }

        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver"); 
                connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL driver not found!", e);
            }
        }

        return connection;
    }
}