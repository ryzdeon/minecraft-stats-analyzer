package fr.ryzdeon.statsanalyzer;

import fr.ryzdeon.statsanalyzer.api.ApiServer;
import fr.ryzdeon.statsanalyzer.commands.*;
import fr.ryzdeon.statsanalyzer.database.DatabaseManager;
import fr.ryzdeon.statsanalyzer.listeners.PlayerStatsListener;
import fr.ryzdeon.statsanalyzer.services.EconomyService;
import fr.ryzdeon.statsanalyzer.services.PlayerStatsService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class StatsAnalyzerPlugin extends JavaPlugin {

    private static StatsAnalyzerPlugin instance;
    private DatabaseManager databaseManager;
    private PlayerStatsService playerStatsService;
    private EconomyService economyService;
    private ApiServer apiServer;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        getLogger().info("========================================");
        getLogger().info("  StatsAnalyzer v1.0.0 - Démarrage");
        getLogger().info("========================================");

        // Créer le dossier du plugin
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialiser la base de données
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            getLogger().info("✓ Base de données initialisée");
        } catch (Exception e) {
            getLogger().severe("✗ Erreur lors de l'initialisation de la base de données");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Configurer Vault Economy
        if (!setupEconomy()) {
            getLogger().warning("⚠ Vault/Economy non trouvé - l'économie sera désactivée");
        } else {
            getLogger().info("✓ Vault/Economy détecté");
            economyService = new EconomyService(this, economy);
        }

        // Initialiser les services
        playerStatsService = new PlayerStatsService(this, databaseManager);

        // Enregistrer les commandes
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("topmoney").setExecutor(new TopMoneyCommand(this));
        getCommand("topkills").setExecutor(new TopKillsCommand(this));
        getCommand("topplaytime").setExecutor(new TopPlaytimeCommand(this));
        getCommand("statsapi").setExecutor(new StatsApiCommand(this));

        // Enregistrer les listeners
        Bukkit.getPluginManager().registerEvents(new PlayerStatsListener(this), this);

        // Démarrer l'API REST
        if (getConfig().getBoolean("api.enabled", true)) {
            try {
                apiServer = new ApiServer(this, databaseManager, economy);
                apiServer.start();
                getLogger().info("✓ API REST démarrée sur le port " + getConfig().getInt("api.port", 8080));
            } catch (Exception e) {
                getLogger().severe("✗ Erreur au démarrage de l'API REST");
                e.printStackTrace();
            }
        }

        getLogger().info("========================================");
        getLogger().info("  StatsAnalyzer activé avec succès !");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("StatsAnalyzer désactivé.");
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (apiServer != null) {
            apiServer.stop();
        }
    }

    public static StatsAnalyzerPlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerStatsService getPlayerStatsService() {
        return playerStatsService;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public Economy getEconomy() {
        return economy;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }
}
