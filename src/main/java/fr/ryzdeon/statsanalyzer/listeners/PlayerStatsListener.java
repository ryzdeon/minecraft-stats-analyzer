package fr.ryzdeon.statsanalyzer.listeners;

import fr.ryzdeon.statsanalyzer.StatsAnalyzerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerStatsListener implements Listener {
    private final StatsAnalyzerPlugin plugin;

    public PlayerStatsListener(StatsAnalyzerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerStatsService().registerPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Enregistrer le temps de jeu
        Player player = event.getPlayer();
        long playtime = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 20;
        plugin.getPlayerStatsService().setPlaytime(player.getUniqueId().toString(), (int) playtime);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        plugin.getPlayerStatsService().increment(player.getUniqueId().toString(), "blocks_broken", 1);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        plugin.getPlayerStatsService().increment(player.getUniqueId().toString(), "blocks_placed", 1);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getPlayerStatsService().increment(player.getUniqueId().toString(), "deaths", 1);
        
        if (player.getKiller() != null) {
            Player killer = player.getKiller();
            plugin.getPlayerStatsService().increment(killer.getUniqueId().toString(), "kills", 1);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        int amount = event.getCurrentItem().getAmount();
        plugin.getPlayerStatsService().increment(player.getUniqueId().toString(), "items_crafted", amount);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            plugin.getPlayerStatsService().increment(player.getUniqueId().toString(), "fish_caught", 1);
        }
    }
}
