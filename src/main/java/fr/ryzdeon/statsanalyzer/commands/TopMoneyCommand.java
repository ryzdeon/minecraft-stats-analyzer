package fr.ryzdeon.statsanalyzer.commands;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class TopMoneyCommand implements CommandExecutor {
    private final StatsAnalyzerPlugin plugin;

    public TopMoneyCommand(StatsAnalyzerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.getEconomy() == null) {
            sender.sendMessage("§c✗ L'économie n'est pas activée.");
            return true;
        }

        List<Map<String, Object>> topList = plugin.getPlayerStatsService().getTopMoney(10);
        
        sender.sendMessage("\n§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("§6Top 10 Richesse");
        sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        int rank = 1;
        for (Map<String, Object> entry : topList) {
            String name = (String) entry.get("username");
            double balance = (double) entry.get("balance");
            String currency = plugin.getConfig().getString("economy.currency", "$");
            
            String medal = switch(rank) {
                case 1 -> "§6🥇";
                case 2 -> "§7🥈";
                case 3 -> "§c🥉";
                default -> "§7" + rank + ".";
            };
            
            sender.sendMessage(medal + " §f" + name + " §7- " + currency + String.format("%.2f", balance));
            rank++;
        }
        sender.sendMessage("§6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n");
        return true;
    }
}
