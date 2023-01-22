package me.docxbox.islandhelper;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EnforcerTask extends BukkitRunnable {

    private final IslandHelper plugin;
    private final Player player;

    public EnforcerTask(IslandHelper plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        // todo: put code that enforces users are in the correct world here
        return;
    }
}
