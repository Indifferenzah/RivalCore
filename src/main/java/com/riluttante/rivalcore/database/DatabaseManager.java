package com.riluttante.rivalcore.database;

import com.riluttante.rivalcore.RivalCorePlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {

    private final RivalCorePlugin plugin;
    private Connection connection;
    private final String jdbcUrl;

    public DatabaseManager(RivalCorePlugin plugin) {
        this.plugin = plugin;
        String dataFolderPath = plugin.getDataFolder().getAbsolutePath().replace("\\", "/");
        this.jdbcUrl = "jdbc:h2:file:" + dataFolderPath + "/rivalcore;AUTO_SERVER=FALSE;DB_CLOSE_ON_EXIT=FALSE";
    }

    public void init() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(jdbcUrl, "sa", "");
            createTables();
            insertDefaultGameState();
            plugin.getLogger().info("Database initialized successfully.");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "H2 Driver not found!", e);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS game_state (" +
                "id INTEGER NOT NULL," +
                "state VARCHAR(20) NOT NULL," +
                "start_timestamp BIGINT NOT NULL DEFAULT 0," +
                "teams_revealed BOOLEAN NOT NULL DEFAULT FALSE," +
                "pvp_enabled BOOLEAN NOT NULL DEFAULT TRUE," +
                "PRIMARY KEY (id))"
            );
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS player_teams (" +
                "uuid VARCHAR(36) NOT NULL," +
                "player_name VARCHAR(64) NOT NULL," +
                "team VARCHAR(10) NOT NULL," +
                "assigned_at BIGINT NOT NULL," +
                "PRIMARY KEY (uuid))"
            );
        }
    }

    private void insertDefaultGameState() throws SQLException {
        String sql = "MERGE INTO game_state (id, state, start_timestamp, teams_revealed, pvp_enabled) " +
                     "KEY(id) VALUES(1, 'WAITING', 0, FALSE, TRUE)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(jdbcUrl, "sa", "");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reconnect to database!", e);
        }
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing database connection.", e);
            }
        }
    }
}
