package fr.ryzdeon.statsanalyzer.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import fr.ryzdeon.statsanalyzer.database.DatabaseManager;
import net.milkbowl.vault.economy.Economy;
import spark.Spark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ApiServer {
    private final StatsAnalyzerPlugin plugin;
    private final DatabaseManager db;
    private final Economy economy;
    private final Gson gson = new Gson();

    public ApiServer(StatsAnalyzerPlugin plugin, DatabaseManager db, Economy economy) {
        this.plugin = plugin;
        this.db = db;
        this.economy = economy;
    }

    public void start() {
        int port = plugin.getConfig().getInt("api.port", 8080);
        String host = plugin.getConfig().getString("api.host", "0.0.0.0");
        String token = plugin.getConfig().getString("api.token", "");

        Spark.port(port);
        Spark.ipAddress(host);

        // Middleware pour le token
        if (!token.isEmpty()) {
            Spark.before((req, res) -> {
                String auth = req.headers("Authorization");
                if (!token.equals(auth)) {
                    Spark.halt(401, "{\"error\":\"Unauthorized\"}");
                }
            });
        }

        Spark.get("/api/health", (req, res) -> {
            res.type("application/json");
            JsonObject json = new JsonObject();
            json.addProperty("status", "ok");
            json.addProperty("version", "1.0.0");
            return gson.toJson(json);
        });

        Spark.get("/api/stats/:player", (req, res) -> {
            res.type("application/json");
            return getPlayerStatsJson(req.params(":player"));
        });

        Spark.get("/api/top-money", (req, res) -> {
            res.type("application/json");
            return getTopMoneyJson();
        });

        Spark.get("/api/top-kills", (req, res) -> {
            res.type("application/json");
            return getTopKillsJson();
        });

        Spark.get("/api/top-playtime", (req, res) -> {
            res.type("application/json");
            return getTopPlaytimeJson();
        });

        Spark.get("/api/server-stats", (req, res) -> {
            res.type("application/json");
            return getServerStatsJson();
        });

        plugin.getLogger().info("✓ API REST démarrée sur http://" + host + ":" + port);
    }

    public void stop() {
        Spark.stop();
    }

    private String getPlayerStatsJson(String playerName) {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT p.*, s.* FROM players p LEFT JOIN player_stats s ON p.uuid = s.uuid WHERE p.username = ? LIMIT 1"
            );
            ps.setString(1, playerName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JsonObject json = new JsonObject();
                json.addProperty("username", rs.getString("username"));
                json.addProperty("uuid", rs.getString("uuid"));
                json.addProperty("kills", rs.getInt("kills"));
                json.addProperty("deaths", rs.getInt("deaths"));
                json.addProperty("blocks_broken", rs.getInt("blocks_broken"));
                json.addProperty("blocks_placed", rs.getInt("blocks_placed"));
                json.addProperty("distance_traveled", rs.getDouble("distance_traveled"));
                json.addProperty("items_crafted", rs.getInt("items_crafted"));
                json.addProperty("fish_caught", rs.getInt("fish_caught"));
                json.addProperty("playtime_seconds", rs.getInt("playtime_seconds"));
                
                if (economy != null) {
                    double balance = economy.getBalance(plugin.getServer().getOfflinePlayer(playerName));
                    json.addProperty("balance", balance);
                }
                
                return gson.toJson(json);
            }
            
            JsonObject error = new JsonObject();
            error.addProperty("error", "Player not found");
            return gson.toJson(error);
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            return gson.toJson(error);
        }
    }

    private String getTopMoneyJson() {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT username, last_seen_balance FROM players ORDER BY last_seen_balance DESC LIMIT 10"
            );
            ResultSet rs = ps.executeQuery();

            List<JsonObject> list = new ArrayList<>();
            int rank = 1;
            while (rs.next()) {
                JsonObject json = new JsonObject();
                json.addProperty("rank", rank++);
                json.addProperty("username", rs.getString("username"));
                json.addProperty("balance", rs.getDouble("last_seen_balance"));
                list.add(json);
            }
            
            return gson.toJson(list);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getTopKillsJson() {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT p.username, s.kills FROM players p JOIN player_stats s ON p.uuid = s.uuid ORDER BY s.kills DESC LIMIT 10"
            );
            ResultSet rs = ps.executeQuery();

            List<JsonObject> list = new ArrayList<>();
            int rank = 1;
            while (rs.next()) {
                JsonObject json = new JsonObject();
                json.addProperty("rank", rank++);
                json.addProperty("username", rs.getString("username"));
                json.addProperty("kills", rs.getInt("kills"));
                list.add(json);
            }
            
            return gson.toJson(list);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getTopPlaytimeJson() {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT p.username, s.playtime_seconds FROM players p JOIN player_stats s ON p.uuid = s.uuid ORDER BY s.playtime_seconds DESC LIMIT 10"
            );
            ResultSet rs = ps.executeQuery();

            List<JsonObject> list = new ArrayList<>();
            int rank = 1;
            while (rs.next()) {
                JsonObject json = new JsonObject();
                json.addProperty("rank", rank++);
                json.addProperty("username", rs.getString("username"));
                json.addProperty("playtime_seconds", rs.getInt("playtime_seconds"));
                list.add(json);
            }
            
            return gson.toJson(list);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getServerStatsJson() {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT COUNT(*) as total_players FROM players"
            );
            ResultSet rs = ps.executeQuery();
            
            JsonObject json = new JsonObject();
            if (rs.next()) {
                json.addProperty("total_players", rs.getInt("total_players"));
            }
            json.addProperty("online_players", plugin.getServer().getOnlinePlayers().size());
            json.addProperty("max_players", plugin.getServer().getMaxPlayers());
            
            return gson.toJson(json);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
