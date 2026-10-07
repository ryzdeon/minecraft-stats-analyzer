package fr.ryzdeon.statsanalyzer.commands;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class TopKillsCommand implements CommandExecutor {
    private final StatsAnalyzerPlugin plugin;

    public TopKillsCommand(StatsAnalyzerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Map<String, Object>> topList = plugin.getPlayerStatsService().getTopKills(10);
        
        sender.sendMessage("\n§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("§6Top 10 Kills");
        sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        int rank = 1;
        for (Map<String, Object> entry : topList) {
            String name = (String) entry.get("username");
            int kills = (int) entry.get("kills");
            
            String medal = switch(rank) {
                case 1 -> "§c💀";
                case 2 -> "§7💀";
                case 3 -> "§e💀";
                default -> "§7" + rank + ".";
            };
            
            sender.sendMessage(medal + " §f" + name + " §7- " + kills + " kills");
            rank++;
        }
        sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n");
        return true;
    }
}
