package me.docxbox.islandhelper;

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

    public EnforcerTask(IslandHelper plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
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
    }

    // removes the player from the mainland
    public void removeFromMainland(){
        player.sendMessage(ChatColor.RED + "Time's up buddy, time to go home!");
    }

    // enforces the players ability to be in the mainland
    public void enforceMainlandPresence(long beginMainlandTicks) {
        // if the player is over their maximum allowed time in the mainland
        if (beginMainlandTicks + plugin.getConfig().getInt("maximumMainlandTime") <= getMainlandGameTime()) {
            recordCurrentCoordinates();
            removeFromMainland();
        } else {
            long remainingTicks = (beginMainlandTicks + plugin.getConfig().getInt("maximumMainlandTime")) / 20;
            player.sendMessage(ChatColor.YELLOW + "You have " + remainingTicks + " in game days left in the mainland");
        }
    }

    @Override
    public void run() {
        // if the player is in the mainland world, enforce their state
        if (player.getWorld().getName().equals(plugin.mainland_world.getName())){
            // get the game time from when this player entered the mainland
            String getMainlandBeginTicks = "SELECT begin_ml_ticks FROM player_cooldowns WHERE player_uuid=?";
            try {
                PreparedStatement getMainlandBeginTicksStatement = plugin.connection.prepareStatement(getMainlandBeginTicks, Statement.RETURN_GENERATED_KEYS);
                getMainlandBeginTicksStatement.setString(1, player.getUniqueId().toString());
                ResultSet mainlandTicks = getMainlandBeginTicksStatement.executeQuery();

                // if there's no record for when this player entered the mainland, create it
                while (mainlandTicks.next()) {
                    if (mainlandTicks.getRow() <= 0) {
                        createCooldownRecord();
                    } else {
                        enforceMainlandPresence(mainlandTicks.getLong("begin_ml_ticks"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
