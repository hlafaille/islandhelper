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

    // returns the current game time
    private long getMainlandGameTime(){
            return plugin.mainland_world.getGameTime();
    }

    // creates a cooldown record for this player stating when they entered the mainland
    private void createCooldownRecord() {
        player.sendMessage(ChatColor.GREEN + "Welcome to the mainland!");
        String createCooldown = "INSERT INTO player_cooldowns (player_uuid, begin_ml_ticks) VALUES (?, ?)";
        try {
            PreparedStatement createCooldownStatement = plugin.connection.prepareStatement(createCooldown, Statement.RETURN_GENERATED_KEYS);
            createCooldownStatement.setString(1, player.getUniqueId().toString());
            createCooldownStatement.setLong(2, getMainlandGameTime());
            createCooldownStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // records the players current coordinates
    public void recordCurrentCoordinates(){
        String recordMainlandCoords = """
                INSERT INTO player_locations (player_uuid, mainland_x, mainland_y, mainland_z) VALUES (?, ?, ?, ?)
                ON CONFLICT (player_uuid)
                DO UPDATE SET mainland_x=?,
                mainland_y=?,
                mainland_z=?
                """;

        try {
            PreparedStatement recordMainlandCoordsStatement = plugin.connection.prepareStatement(recordMainlandCoords, Statement.RETURN_GENERATED_KEYS);
            recordMainlandCoordsStatement.setString(1, player.getUniqueId().toString());
            recordMainlandCoordsStatement.setDouble(2, player.getLocation().getX());
            recordMainlandCoordsStatement.setDouble(3, player.getLocation().getY());
            recordMainlandCoordsStatement.setDouble(4, player.getLocation().getZ());
            recordMainlandCoordsStatement.setDouble(5, player.getLocation().getX());
            recordMainlandCoordsStatement.setDouble(6, player.getLocation().getY());
            recordMainlandCoordsStatement.setDouble(7, player.getLocation().getZ());
            recordMainlandCoordsStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    // removes the player from the mainland
    public void removeFromMainland(){
        recordCurrentCoordinates();
        player.sendMessage(ChatColor.RED + "Time's up buddy, time to go home!");
        player.performCommand(plugin.getConfig().getString("islandCommand"));
    }

    // enforces the players ability to be in the mainland
    public void enforceMainlandPresence(long beginMainlandTicks) {
        // point at which the player should be kicked out
        long kickoutTime = beginMainlandTicks + plugin.getConfig().getLong("maximumMainlandTime");

        // if the player is over their maximum allowed time in the mainland
        if (kickoutTime <= getMainlandGameTime()) {
            removeFromMainland();
        } else {
            double remainingTime = ((double) kickoutTime - (double) getMainlandGameTime()) / 1200;
            player.sendMessage(ChatColor.YELLOW + "You have " + String.format("%.2f", remainingTime) + " minutes left in the mainland.");
        }
    }

    @Override
    public void run() {
        // if the player is in the mainland world, enforce their state
        if (handler.isPlayerInMainland()) {
            // if the player is over their allowed mainland time
            try {
                if (handler.isPlayerOverAllowedMainlandTime()){

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
