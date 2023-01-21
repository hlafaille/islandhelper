package me.docxbox.islandhelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class IslandHelper extends JavaPlugin {

    // mainland information
    private World mainland_world = Bukkit.getWorld("world");
    private Location mainland = new Location(Bukkit.getWorld("world"), 0, 0, 0);

    @Override
    public void onEnable() {
        this.getLogger().info("HELLO WORLD");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // if player wants to go to the mainland
        if (command.getName().equalsIgnoreCase("mainland") && sender instanceof Player){
            Player p = (Player) sender;
            p.teleport
        }


        return true;
    }
}
