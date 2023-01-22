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
        // Run the world enforcer task for this player each Minecraft day (10 IRL minutes)
        BukkitTask task = new EnforcerTask(this.plugin, event.getPlayer()).runTaskTimer(this.plugin, 0L, 1200L);
    }
}
