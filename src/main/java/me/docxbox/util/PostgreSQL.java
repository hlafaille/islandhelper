package me.docxbox.util;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.docxbox.islandhelper.IslandHelper;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class PostgreSQL {
    public Connection connect(@Nonnull IslandHelper plugin) {
        plugin.getLogger().log(Level.INFO, "Connecting to PostgreSQL DB...");

        String connectionUrl = plugin.getConfig().getString("jdbcConnectionUrl");

        Connection conn = null;

        HikariConfig config = new HikariConfig();
        //config.setJdbcUrl(connectionUrl + "/" );
        config.setJdbcUrl(plugin.getConfig().getString("jdbcConnectionUrl"));
        config.setDriverClassName(org.postgresql.Driver.class.getName());

        HikariDataSource ds = new HikariDataSource(config);

        try {
            conn = ds.getConnection();
        } catch (SQLException err) {
            plugin.getLogger().log(Level.WARNING, err.getMessage());
            ds.close();
        }

        plugin.getLogger().log(Level.INFO, "Connected to PostgreSQL DB!");

        return conn;
    }
}
