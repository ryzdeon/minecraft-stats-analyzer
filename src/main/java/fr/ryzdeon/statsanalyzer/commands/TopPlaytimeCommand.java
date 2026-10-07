package fr.ryzdeon.statsanalyzer.commands;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class TopPlaytimeCommand implements CommandExecutor {
    private final StatsAnalyzerPlugin plugin;

    public TopPlaytimeCommand(StatsAnalyzerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Map<String, Object>> topList = plugin.getPlayerStatsService().getTopPlaytime(10);
        
        sender.sendMessage("\n§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("§6Top 10 Temps de Jeu");
        sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        int rank = 1;
        for (Map<String, Object> entry : topList) {
            String name = (String) entry.get("username");
            int seconds = (int) entry.get("playtime_seconds");
            
            String medal = switch(rank) {
                case 1 -> "§e⏱";
                case 2 -> "§7⏱";
                case 3 -> "§c⏱";
                default -> "§7" + rank + ".";
            };
            
            sender.sendMessage(medal + " §f" + name + " §7- " + formatPlaytime(seconds));
            rank++;
        }
        sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n");
        return true;
    }

    private String formatPlaytime(int seconds) {
        int days = seconds / 86400;
        int hours = (seconds % 86400) / 3600;
        int minutes = (seconds % 3600) / 60;
        
        if (days > 0) {
            return days + "j " + hours + "h " + minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }
}
