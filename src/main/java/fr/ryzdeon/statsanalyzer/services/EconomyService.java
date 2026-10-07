package fr.ryzdeon.statsanalyzer.services;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import fr.ryzdeon.statsanalyzer.database.DatabaseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;

public class EconomyService {
    private final StatsAnalyzerPlugin plugin;
    private final Economy economy;
    private final DatabaseManager db;

    public EconomyService(StatsAnalyzerPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.db = plugin.getDatabaseManager();
    }

    public void logTransaction(OfflinePlayer player, double amount, String type, String description) {
        try {
            double balance = economy.getBalance(player);
            db.executeUpdate(
                    "INSERT INTO economy_history (uuid, username, amount, transaction_type, description, balance_after) VALUES (?, ?, ?, ?, ?, ?)",
                    player.getUniqueId().toString(),
                    player.getName(),
                    amount,
                    type,
                    description,
                    balance
            );
            // Mise à jour du solde dans la table players
            db.executeUpdate(
                    "UPDATE players SET last_seen_balance = ? WHERE uuid = ?",
                    balance,
                    player.getUniqueId().toString()
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'enregistrement de la transaction");
            e.printStackTrace();
        }
    }

    public double getBalance(OfflinePlayer player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }
}
