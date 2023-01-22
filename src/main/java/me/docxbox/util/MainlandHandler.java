package me.docxbox.util;

import me.docxbox.islandhelper.IslandHelper;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainlandHandler {
    // constructor variables
    private final IslandHelper plugin;
    private final Player player;

    // config variables
    private final String configMainlandWorld;
    private final long configMaximumMainlandTime;
    private final String configIslandCommand;

    // constructor
    public MainlandHandler(IslandHelper plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.configMainlandWorld = this.plugin.getConfig().getString("mainlandWorld");
        this.configMaximumMainlandTime = this.plugin.getConfig().getLong("maximumMainlandTime");
        this.configIslandCommand = this.plugin.getConfig().getString("islandCommand");
    }

    // returns the craftbukkit World object for the Mainland
    public World getMainlandWorld() {
        return plugin.getServer().getWorld(configMainlandWorld);
    }

    // returns if the player is in the mainland
    public boolean isPlayerInMainland() {
        return player.getWorld().getUID() == this.getMainlandWorld().getUID();
    }

    // returns the amount of ticks that have passed since this world was created
    public long getMainlandTimeSinceCreation() {
        return getMainlandWorld().getGameTime();
    }

    // returns the time that the player entered the mainland
    public long getPlayerMainlandBeginTime() throws SQLException {
        String getMainlandBeginTicks = "SELECT begin_ml_ticks FROM player_cooldowns WHERE player_uuid=?";
        PreparedStatement getMainlandBeginTicksStatement = plugin.connection.prepareStatement(getMainlandBeginTicks, Statement.RETURN_GENERATED_KEYS);
        getMainlandBeginTicksStatement.setString(1, player.getUniqueId().toString());
        ResultSet mainlandTicks = getMainlandBeginTicksStatement.executeQuery();
        return mainlandTicks.getLong("begin_ml_ticks");
    }

    // returns the time at which the player should leave the mainland
    public long getPlayerMainlandEndTime() throws SQLException {
        return getPlayerMainlandBeginTime() + configMaximumMainlandTime;
    }

    // returns if the player is over their allowed time in the mainland
    public boolean isPlayerOverAllowedMainlandTime() throws SQLException {
        return getPlayerMainlandBeginTime() + getPlayerMainlandEndTime() <= getMainlandTimeSinceCreation();
    }

    // records the players coordinates & calls the Skyblock home command
    public void removePlayerFromMainland() throws SQLException {
        recordMainlandCoordinates();
        player.performCommand(configIslandCommand);
    }

    // records the players current coordinates in the mainland
    private void recordMainlandCoordinates() throws SQLException {
        String recordMainlandCoords = """
                INSERT INTO player_locations (player_uuid, mainland_x, mainland_y, mainland_z) VALUES (?, ?, ?, ?)
                ON CONFLICT (player_uuid)
                DO UPDATE SET mainland_x=?,
                mainland_y=?,
                mainland_z=?
                """;
        PreparedStatement recordMainlandCoordsStatement = plugin.connection.prepareStatement(recordMainlandCoords, Statement.RETURN_GENERATED_KEYS);
        recordMainlandCoordsStatement.setString(1, player.getUniqueId().toString());
        recordMainlandCoordsStatement.setDouble(2, player.getLocation().getX());
        recordMainlandCoordsStatement.setDouble(3, player.getLocation().getY());
        recordMainlandCoordsStatement.setDouble(4, player.getLocation().getZ());
        recordMainlandCoordsStatement.setDouble(5, player.getLocation().getX());
        recordMainlandCoordsStatement.setDouble(6, player.getLocation().getY());
        recordMainlandCoordsStatement.setDouble(7, player.getLocation().getZ());
        recordMainlandCoordsStatement.executeUpdate();
    }

    // records the players entry to the mainland
    public void recordMainlandEntry() throws SQLException{
        String createCooldown = "INSERT INTO player_cooldowns (player_uuid, begin_ml_ticks) VALUES (?, ?)";
        PreparedStatement createCooldownStatement = plugin.connection.prepareStatement(createCooldown, Statement.RETURN_GENERATED_KEYS);
        createCooldownStatement.setString(1, player.getUniqueId().toString());
        createCooldownStatement.setLong(2, getMainlandTimeSinceCreation());
        createCooldownStatement.executeUpdate();

    }

}
