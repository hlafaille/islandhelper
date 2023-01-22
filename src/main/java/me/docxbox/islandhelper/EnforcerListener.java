package me.docxbox.islandhelper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

public class EnforcerListener implements Listener {
    private final IslandHelper plugin;

    public EnforcerListener(IslandHelper plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        BukkitTask task = new EnforcerTask(this.plugin, event.getPlayer()).runTaskTimer(this.plugin, 20L, 0L);
    }
}
