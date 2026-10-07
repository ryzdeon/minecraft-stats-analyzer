package fr.ryzdeon.statsanalyzer.commands;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;

public class StatsCommand implements CommandExecutor {
    private final StatsAnalyzerPlugin plugin;

    public StatsCommand(StatsAnalyzerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : (sender instanceof Player ? (Player) sender : null);

        if (target == null) {
            sender.sendMessage("§c✗ Joueur introuvable.");
            return true;
        }

        try {
            ResultSet rs = plugin.getPlayerStatsService().getPlayerStats(target.getUniqueId().toString());
            if (rs != null && rs.next()) {
                sender.sendMessage("\n§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                sender.sendMessage("§6Stats de §f" + target.getName());
                sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                sender.sendMessage("§7Kills: §f" + rs.getInt("kills"));
                sender.sendMessage("§7Morts: §f" + rs.getInt("deaths"));
                sender.sendMessage("§7Blocs cassés: §f" + rs.getInt("blocks_broken"));
                sender.sendMessage("§7Blocs placés: §f" + rs.getInt("blocks_placed"));
                sender.sendMessage("§7Distance: §f" + String.format("%.0f", rs.getDouble("distance_traveled")) + " blocs");
                sender.sendMessage("§7Temps de jeu: §f" + formatPlaytime(rs.getInt("playtime_seconds")));
                
                if (plugin.getEconomy() != null) {
                    double balance = plugin.getEconomy().getBalance(target);
                    sender.sendMessage("§7Argent: §a" + plugin.getConfig().getString("economy.currency", "$") + String.format("%.2f", balance));
                }
                sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n");
            } else {
                sender.sendMessage("§c✗ Aucune stats trouvée pour ce joueur.");
            }
        } catch (Exception e) {
            sender.sendMessage("§c✗ Erreur lors de la récupération des stats.");
            e.printStackTrace();
        }

        return true;
    }

    private String formatPlaytime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}
