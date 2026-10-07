package fr.ryzdeon.statsanalyzer.services;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import fr.ryzdeon.statsanalyzer.database.DatabaseManager;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlayerStatsService {
    private final StatsAnalyzerPlugin plugin;
    private final DatabaseManager db;

    public PlayerStatsService(StatsAnalyzerPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void registerPlayer(Player player) {
        try {
            String uuid = player.getUniqueId().toString();
            String username = player.getName();
            db.executeUpdate(
                    "INSERT OR IGNORE INTO players (uuid, username) VALUES (?, ?)",
                    uuid, username
            );
            db.executeUpdate(
                    "INSERT OR IGNORE INTO player_stats (uuid) VALUES (?)",
                    uuid
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'enregistrement du joueur");
            e.printStackTrace();
        }
    }

    public void updatePlayerStats(String uuid, String statName, int increment) {
        try {
            String query = "UPDATE player_stats SET " + statName + " = " + statName + " + ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
            db.executeUpdate(query, increment, uuid);
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la mise à jour des stats");
            e.printStackTrace();
        }
    }

    public Map<String, Object> getPlayerStats(String uuid) {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT * FROM player_stats WHERE uuid = ?",
                    uuid
            );
            if (rs.next()) {
                stats.put("uuid", uuid);
                stats.put("playtime_seconds", rs.getInt("playtime_seconds"));
                stats.put("kills", rs.getInt("kills"));
                stats.put("deaths", rs.getInt("deaths"));
                stats.put("blocks_broken", rs.getInt("blocks_broken"));
                stats.put("blocks_placed", rs.getInt("blocks_placed"));
                stats.put("distance_traveled", rs.getDouble("distance_traveled"));
                stats.put("items_crafted", rs.getInt("items_crafted"));
                stats.put("fish_caught", rs.getInt("fish_caught"));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération des stats");
            e.printStackTrace();
        }
        return stats;
    }

    public List<Map<String, Object>> getTopMoney(int limit) {
        List<Map<String, Object>> topList = new ArrayList<>();
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT uuid, username, last_seen_balance FROM players ORDER BY last_seen_balance DESC LIMIT ?",
                    limit
            );
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("rank", rank++);
                entry.put("uuid", rs.getString("uuid"));
                entry.put("username", rs.getString("username"));
                entry.put("balance", rs.getDouble("last_seen_balance"));
                topList.add(entry);
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération du top argent");
            e.printStackTrace();
        }
        return topList;
    }

    public List<Map<String, Object>> getTopKills(int limit) {
        List<Map<String, Object>> topList = new ArrayList<>();
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT p.uuid, p.username, ps.kills FROM players p " +
                    "JOIN player_stats ps ON p.uuid = ps.uuid " +
                    "ORDER BY ps.kills DESC LIMIT ?",
                    limit
            );
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("rank", rank++);
                entry.put("uuid", rs.getString("uuid"));
                entry.put("username", rs.getString("username"));
                entry.put("kills", rs.getInt("kills"));
                topList.add(entry);
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération du top kills");
            e.printStackTrace();
        }
        return topList;
    }

    public List<Map<String, Object>> getTopPlaytime(int limit) {
        List<Map<String, Object>> topList = new ArrayList<>();
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT p.uuid, p.username, ps.playtime_seconds FROM players p " +
                    "JOIN player_stats ps ON p.uuid = ps.uuid " +
                    "ORDER BY ps.playtime_seconds DESC LIMIT ?",
                    limit
            );
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("rank", rank++);
                entry.put("uuid", rs.getString("uuid"));
                entry.put("username", rs.getString("username"));
                entry.put("playtime_seconds", rs.getInt("playtime_seconds"));
                topList.add(entry);
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération du top playtime");
            e.printStackTrace();
        }
        return topList;
    }
}
