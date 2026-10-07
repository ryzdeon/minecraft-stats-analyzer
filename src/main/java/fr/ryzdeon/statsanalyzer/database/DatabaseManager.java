package fr.ryzdeon.statsanalyzer.database;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private final StatsAnalyzerPlugin plugin;
    private Connection connection;

    public DatabaseManager(StatsAnalyzerPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException, ClassNotFoundException {
        String type = plugin.getConfig().getString("database.type", "sqlite");

        if (type.equalsIgnoreCase("sqlite")) {
            initializeSQLite();
        } else if (type.equalsIgnoreCase("mysql")) {
            initializeMySQL();
        }

        createTables();
    }

    private void initializeSQLite() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String path = plugin.getConfig().getString("database.sqlite.file", "plugins/StatsAnalyzer/stats.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "minecraft_stats");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Table des joueurs
            stmt.execute(""
                    + "CREATE TABLE IF NOT EXISTS players ("
                    + "uuid TEXT PRIMARY KEY,"
                    + "username TEXT NOT NULL,"
                    + "first_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "last_seen_balance REAL DEFAULT 0"
                    + ")");

            // Table des stats de joueurs
            stmt.execute(""
                    + "CREATE TABLE IF NOT EXISTS player_stats ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "uuid TEXT NOT NULL,"
                    + "playtime_seconds INTEGER DEFAULT 0,"
                    + "kills INTEGER DEFAULT 0,"
                    + "deaths INTEGER DEFAULT 0,"
                    + "blocks_broken INTEGER DEFAULT 0,"
                    + "blocks_placed INTEGER DEFAULT 0,"
                    + "distance_traveled REAL DEFAULT 0,"
                    + "items_crafted INTEGER DEFAULT 0,"
                    + "fish_caught INTEGER DEFAULT 0,"
                    + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "UNIQUE(uuid)"
                    + ")");

            // Table de l'historique économique
            stmt.execute(""
                    + "CREATE TABLE IF NOT EXISTS economy_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "uuid TEXT NOT NULL,"
                    + "username TEXT NOT NULL,"
                    + "amount REAL NOT NULL,"
                    + "transaction_type TEXT NOT NULL,"
                    + "description TEXT,"
                    + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "balance_after REAL DEFAULT 0"
                    + ")");

            // Index pour optimiser les recherches
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuid ON players(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_username ON players(username)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_economy_uuid ON economy_history(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_economy_timestamp ON economy_history(timestamp)");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la fermeture de la base de données");
            e.printStackTrace();
        }
    }

    public void executeUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        }
    }

    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }
}
