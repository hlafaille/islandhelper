package me.docxbox.islandhelper;
import me.docxbox.util.PostgreSQL;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public final class IslandHelper extends JavaPlugin {

    // mainland world information
    private final World mainland_world = Bukkit.getWorld("world");
    private final Location mainland_location = new Location(
            mainland_world,
            mainland_world.getSpawnLocation().getX(),
            mainland_world.getSpawnLocation().getY(),
            mainland_world.getSpawnLocation().getZ()
    );

    // database objects
    public final PostgreSQL db = new PostgreSQL();
    public Connection connection;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        connection = db.connect(this);
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
        // if player wants to go to the mainland, teleport them to the world spawn
        if (command.getName().equalsIgnoreCase("mainland") && sender instanceof Player){
            Player p = (Player) sender;
            p.sendMessage(ChatColor.GREEN + "Going to mainland...");

            // get players last main world X,Y,Z from the database
            String getCoordinates = "SELECT mainland_x, mainland_y, mainland_z FROM player_locations WHERE player_uuid=?";

            // pre-define the previous location variable, it may be set to world spawn if the player doesn't have a stored mainland coordinate
            Location prev_location = null;

            try {
                PreparedStatement statement = connection.prepareStatement(getCoordinates, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, p.getUniqueId().toString());
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    // if there's no rows, set prev_location to world spawn. else, set prev_location to the coordinates from the database
                    if (rs.getRow() <= 0){
                        prev_location = mainland_location;
                    } else {
                        double mainland_x = rs.getDouble("mainland_x");
                        double mainland_y = rs.getDouble("mainland_y");
                        double mainland_z = rs.getDouble("mainland_z");
                        prev_location = new Location(mainland_world, mainland_x, mainland_y, mainland_z);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            p.teleport(mainland_location);
            mainland_world.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
            mainland_world.playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }
        return true;

        /*
        if (command.getName().equalsIgnoreCase("return") && sender instanceof Player){

        }
         */
    }
}
