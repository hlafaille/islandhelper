package me.docxbox.islandhelper;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public final class IslandHelper extends JavaPlugin {

    // mainland world information
    private World mainland_world = Bukkit.getWorld("world");
    private Location mainland_location = new Location(
            mainland_world,
            mainland_world.getSpawnLocation().getX(),
            mainland_world.getSpawnLocation().getY(),
            mainland_world.getSpawnLocation().getZ()
    );

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // ensure that jdbcConnectionUrl is not null
        if (this.getConfig().getString("jdbcConnectionUrl") == null) {
            this.getLogger().severe("jdbcConnectionUrl in config is null! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            try {
                Connection connection = DriverManager.getConnection(this.getConfig().getString("jdbcConnectionUrl"));
            } catch (SQLException e) {
                e.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // if player wants to go to the mainland, teleport them to the world spawn
        if (command.getName().equalsIgnoreCase("mainland") && sender instanceof Player){
            Player p = (Player) sender;
            p.sendMessage(ChatColor.GREEN + "Going to mainland...");
            p.teleport(mainland_location);
            mainland_world.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
            mainland_world.playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }
        return true;
    }
}
