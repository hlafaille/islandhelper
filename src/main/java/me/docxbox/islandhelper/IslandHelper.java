package me.docxbox.islandhelper;
import com.sun.tools.javac.Main;
import me.docxbox.util.MainlandHandler;
import me.docxbox.util.PostgreSQL;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.logging.Handler;

public final class IslandHelper extends JavaPlugin {

    // database objects
    public final PostgreSQL db = new PostgreSQL();
    public Connection connection;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        connection = db.connect(this);

        // create the world enforcer event listener
        new EnforcerListener(this);
    }

    @Override
    public void onDisable() {
        try {
            connection.close();
        } catch (SQLException e){
            this.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // if player wants to go to the mainland, teleport them to their last known coordinates
        if (command.getName().equalsIgnoreCase("mainland") && sender instanceof Player) {
            Player p = (Player) sender;
            MainlandHandler handler = new MainlandHandler(this, p);

            // if the player is already in the mainland, give an error
            if (p.getWorld() == handler.getMainlandWorld()) {
                p.sendMessage(ChatColor.RED + "You are already in the mainland!");
                return true;
            }

            // get the last known player coordinates
            Location lastPlayerCoordinates;
            try {
                lastPlayerCoordinates = handler.getRecordedPlayerMainlandLocation();
            } catch (SQLException e) {
                p.sendMessage(ChatColor.RED + "An error occurred while fetching your last mainland coordinates. Errors are in the logs.");
                e.printStackTrace();
                return false;
            }
            
            // check if the player is within their cooldown
            boolean withinCooldown = false;
            try {
                withinCooldown = handler.isPlayerWithinMainlandCooldown();
            } catch (SQLException e) {
                p.sendMessage(ChatColor.RED + "An error occurred while checking if you're in cooldown. Errors are in the logs.");
                e.printStackTrace();
                return false;
            }
            
            if (withinCooldown) {
                // get the remaining cooldown
                long remainingCooldown = 0;
                try {
                    remainingCooldown = handler.getRemainingMainlandCooldownTime();
                } catch (SQLException e) {
                    p.sendMessage(ChatColor.RED + "An error occurred while checking if you're in cooldown. Errors are in the logs.");
                    e.printStackTrace();
                    return false;
                }

                handler.getMainlandWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 10, 1);
                p.sendMessage(ChatColor.RED + "You still have " + remainingCooldown / 1200 + " minutes left on cooldown.");

            }

            // begin player cooldown
            try {
                handler.recordMainlandEntry();
            } catch (SQLException e) {
                p.sendMessage(ChatColor.RED + "An error occurred while recording your mainland entry timestamp. Errors are in the logs.");
                e.printStackTrace();
                return false;
            }

            // teleport, play some fancy effects
            p.sendMessage(ChatColor.GREEN + "Going to the mainland!");
            p.teleport(lastPlayerCoordinates);
            handler.getMainlandWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
            handler.getMainlandWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }

        // if the player requested their remaining time in the mainland
        if (command.getName().equals("remaining") && sender instanceof Player) {
            Player p = (Player) sender;
            MainlandHandler handler = new MainlandHandler(this, p);

            double remainingTimeInMinutes = 0;
            try {
                remainingTimeInMinutes = (double) handler.getPlayerMainlandRemainingTime() / 1200;
            } catch (SQLException e) {
                p.sendMessage(ChatColor.RED + "An error occurred while fetching your remaining mainland time. Errors are in the logs.");
                e.printStackTrace();
                return false;
            }

            p.sendMessage(ChatColor.YELLOW + "You have " + String.format("%.2f", remainingTimeInMinutes) + " minutes remaining in the mainland!");
        }
        return true;
    }
}
