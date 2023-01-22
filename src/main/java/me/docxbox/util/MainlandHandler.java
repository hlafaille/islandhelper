package me.docxbox.util;

import me.docxbox.islandhelper.IslandHelper;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainlandHandler {
    // plugin & player
    private final IslandHelper plugin;
    private final Player player;

    // variables from config.yml
    private final String configMainlandWorld;
    private final long configMaximumMainlandTime;
    private final long configMainlandCooldownTime;
    private final String configIslandCommand;

    // constructor
    public MainlandHandler(IslandHelper plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.configMainlandWorld = this.plugin.getConfig().getString("mainlandWorld");
        this.configMaximumMainlandTime = this.plugin.getConfig().getLong("maximumMainlandTime");
        this.configMainlandCooldownTime = this.plugin.getConfig().getLong("mainlandCooldownTime");
        this.configIslandCommand = this.plugin.getConfig().getString("islandCommand");
    }

    // returns the craftbukkit World object for the Mainland
    public World getMainlandWorld() {
        return plugin.getServer().getWorld(configMainlandWorld);
    }

    // returns the spawn point of the mainland
    public Location getMainlandSpawnLocation() {
        return getMainlandWorld().getSpawnLocation();
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

        long beginMainlandTicks = 0;
        while (mainlandTicks.next()){
            beginMainlandTicks = mainlandTicks.getLong("begin_ml_ticks");
        }
        return beginMainlandTicks;
    }

    // returns the time at which the player should leave the mainland
    public long getPlayerMainlandEndTime() throws SQLException {
        return getPlayerMainlandBeginTime() + configMaximumMainlandTime;
    }

    // returns the players remaining mainland time
    public long getPlayerMainlandRemainingTime() throws SQLException {
         return getPlayerMainlandEndTime() - getMainlandTimeSinceCreation();
    }

    // returns if the player is over their allowed time in the mainland
    public boolean isPlayerOverAllowedMainlandTime() throws SQLException {
        return getPlayerMainlandBeginTime() + configMaximumMainlandTime <= getMainlandTimeSinceCreation();
    }

    // returns if the player is within their cooldown
    public boolean isPlayerWithinMainlandCooldown() throws SQLException {
        // check if this method is being called while the player still has active time in the mainland
        if (! isPlayerOverAllowedMainlandTime()){
            return false;
        }
        // if the current time is greater than the players mainland end time + the cooldown
        if (getMainlandTimeSinceCreation() >= getPlayerMainlandEndTime() + configMainlandCooldownTime){
            return false;
        } else {
            return true;
        }
    }

    // returns the remaining players mainland cooldown in ticks
    public long getRemainingMainlandCooldownTime() throws SQLException {
        return (getPlayerMainlandEndTime() + configMainlandCooldownTime) - getMainlandTimeSinceCreation();
    }

    // records the players coordinates & calls the Skyblock home command
    public void removePlayerFromMainland() throws SQLException {
        recordMainlandCoordinates();
        getMainlandWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
        getMainlandWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
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
    public void recordMainlandEntry() throws SQLException {
        String createCooldown = """
                INSERT INTO player_cooldowns (player_uuid, begin_ml_ticks) VALUES (?, ?)
                ON CONFLICT (player_uuid)
                DO UPDATE SET begin_ml_ticks=?
                """;
        PreparedStatement createCooldownStatement = plugin.connection.prepareStatement(createCooldown, Statement.RETURN_GENERATED_KEYS);
        createCooldownStatement.setString(1, player.getUniqueId().toString());
        createCooldownStatement.setLong(2, getMainlandTimeSinceCreation());
        createCooldownStatement.setLong(3, getMainlandTimeSinceCreation());
        createCooldownStatement.executeUpdate();
    }

    // returns the players last known coordinates from the mainland
    public Location getRecordedPlayerMainlandLocation() throws SQLException {
        Location previousLocation = null;

        // get players last main world X,Y,Z from the database
        String getCoordinates = "SELECT mainland_x, mainland_y, mainland_z FROM player_locations WHERE player_uuid=?";
        PreparedStatement getCoordinatesStatement = plugin.connection.prepareStatement(getCoordinates, Statement.RETURN_GENERATED_KEYS);
        getCoordinatesStatement.setString(1, player.getUniqueId().toString());
        ResultSet rs = getCoordinatesStatement.executeQuery();

        // if there's no player coordinates in the mainland, get the mainland spawn
        if (rs.getRow() <= 0){
            previousLocation = getMainlandSpawnLocation();
        }

        while (rs.next()){
                double mainland_x = rs.getDouble("mainland_x");
                double mainland_y = rs.getDouble("mainland_y");
                double mainland_z = rs.getDouble("mainland_z");
                previousLocation = new Location(getMainlandWorld(), mainland_x, mainland_y, mainland_z);
        }
        return previousLocation;
    }


}
