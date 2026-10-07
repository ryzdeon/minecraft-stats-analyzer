package fr.ryzdeon.statsanalyzer.commands;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StatsApiCommand implements CommandExecutor {
    private final StatsAnalyzerPlugin plugin;

    public StatsApiCommand(StatsAnalyzerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("api.enabled", true)) {
            sender.sendMessage("§c✗ L'API est désactivée.");
            return true;
        }

        int port = plugin.getConfig().getInt("api.port", 8080);
        String host = plugin.getConfig().getString("api.host", "localhost");
        
        sender.sendMessage("\n§6API StatsAnalyzer");
        sender.sendMessage("§7Adresse: §fhttp://" + host + ":" + port);
        sender.sendMessage("§7Endpoints disponibles:");
        sender.sendMessage("  §7- /api/health");
        sender.sendMessage("  §7- /api/stats/:player");
        sender.sendMessage("  §7- /api/top-money");
        sender.sendMessage("  §7- /api/top-kills");
        sender.sendMessage("  §7- /api/top-playtime");
        sender.sendMessage("  §7- /api/server-stats\n");
        return true;
    }
}
