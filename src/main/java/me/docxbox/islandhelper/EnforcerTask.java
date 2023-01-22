package me.docxbox.islandhelper;

import com.sun.tools.javac.Main;
import me.docxbox.util.MainlandHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.plaf.nimbus.State;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EnforcerTask extends BukkitRunnable {

    private final IslandHelper plugin;
    private final Player player;
    private final Integer gameTime = null;
    private final MainlandHandler handler;

    public EnforcerTask(IslandHelper plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.handler = new MainlandHandler(plugin, player);
    }

    // enforces the players ability to be in the mainland
    /*public void enforceMainlandPresence(long beginMainlandTicks) {
        // point at which the player should be kicked out
        long kickoutTime = beginMainlandTicks + plugin.getConfig().getLong("maximumMainlandTime");

        // if the player is over their maximum allowed time in the mainland
        if (kickoutTime <= getMainlandGameTime()) {
            removeFromMainland();
        } else {
            double remainingTime = ((double) kickoutTime - (double) getMainlandGameTime()) / 1200;
            player.sendMessage(ChatColor.YELLOW + "You have " + String.format("%.2f", remainingTime) + " minutes left in the mainland.");
        }
    }*/

    @Override
    public void run() {
        // if the player is in the mainland world, enforce their state
        if (handler.isPlayerInMainland()) {
            // if the player is over their allowed mainland time
            try {
                if (handler.isPlayerOverAllowedMainlandTime()){
                    player.sendMessage(ChatColor.RED + "Time's up buddy, time to go home!");
                    handler.removePlayerFromMainland();
                } else {
                    double remainingTimeInMinutes = (double) handler.getPlayerMainlandRemainingTime() / 1200;
                    player.sendMessage(ChatColor.YELLOW + "You have " + String.format("%.2f", remainingTimeInMinutes) + " minutes remaining in the mainland!");
                }
            } catch (SQLException e) {
                plugin.getServer().broadcastMessage("An error occurred in Mainland/EnforcerTask.java");
                e.printStackTrace();
            }
        }
    }
}
