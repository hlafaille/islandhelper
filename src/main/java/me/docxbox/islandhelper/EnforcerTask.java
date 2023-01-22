package me.docxbox.islandhelper;

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

    public EnforcerTask(IslandHelper plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        // if the player is in the mainland world, enforce their state
        if (player.getWorld().getName().equals(plugin.getConfig().getString("mainlandWorld"))){
            String getCooldowns = "SELECT mainland_days_remaining FROM player_cooldowns WHERE uuid=?";

            try {
                PreparedStatement getCooldownStatement = plugin.connection.prepareStatement(getCooldowns, Statement.RETURN_GENERATED_KEYS);
                getCooldownStatement.setString(1, player.getUniqueId().toString());
                ResultSet cooldowns = getCooldownStatement.executeQuery();

                // if there's no record of this player having cooldowns then create one
                if (cooldowns.getRow() <= 0){
                    String createCooldown = "INSERT INTO player_cooldowns (player_uuid, mainland_days_remaining, cooldown_days_remaining) VALUES (?, ?, ?)";
                    PreparedStatement createCooldownStatement = plugin.connection.prepareStatement(createCooldown, Statement.RETURN_GENERATED_KEYS);
                    createCooldownStatement.setString(1, player.getUniqueId().toString());
                    createCooldownStatement.setInt(2, 4);
                    createCooldownStatement.setInt(3, 10);
                    createCooldownStatement.executeQuery();
                } else {
                    // update the player cooldown to subtract one
                    String updateCooldown = "UPDATE player_cooldowns SET mainland_days=? WHERE player_uuid=?";
                    PreparedStatement updateCooldownStatement = plugin.connection.prepareStatement(updateCooldown, Statement.RETURN_GENERATED_KEYS);

                    while (cooldowns.next()) {
                        updateCooldownStatement.setInt(1, cooldowns.getInt("mainland_days_remaining") - 1);
                        updateCooldownStatement.setString(2, player.getUniqueId().toString());
                    }
                    updateCooldownStatement.executeUpdate();
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }
}
