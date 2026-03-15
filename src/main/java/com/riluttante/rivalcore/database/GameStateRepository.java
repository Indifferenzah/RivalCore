package com.riluttante.rivalcore.database;

import com.riluttante.rivalcore.models.GameData;
import com.riluttante.rivalcore.models.GameState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameStateRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;

    public GameStateRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public GameData loadGameData() {
        String sql = "SELECT state, start_timestamp, teams_revealed, pvp_enabled FROM game_state WHERE id = 1";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                GameState state = GameState.valueOf(rs.getString("state"));
                long startTimestamp = rs.getLong("start_timestamp");
                boolean teamsRevealed = rs.getBoolean("teams_revealed");
                boolean pvpEnabled = rs.getBoolean("pvp_enabled");
                return new GameData(state, startTimestamp, teamsRevealed, pvpEnabled);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load game data!", e);
        }
        return new GameData(GameState.WAITING, 0L, false, true);
    }

    public void saveGameData(GameData data) {
        String sql = "MERGE INTO game_state (id, state, start_timestamp, teams_revealed, pvp_enabled) " +
                "KEY(id) VALUES(1, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data.getState().name());
            pstmt.setLong(2, data.getStartTimestamp());
            pstmt.setBoolean(3, data.isTeamsRevealed());
            pstmt.setBoolean(4, data.isPvpEnabled());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save game data!", e);
        }
    }

    public void resetGameData() {
        String sql = "MERGE INTO game_state (id, state, start_timestamp, teams_revealed, pvp_enabled) " +
                "KEY(id) VALUES(1, 'WAITING', 0, FALSE, TRUE)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to reset game data!", e);
        }
    }
}
