package com.riluttante.rivalcore.database;

import com.riluttante.rivalcore.models.GameTeam;
import com.riluttante.rivalcore.models.PlayerTeamData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeamRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;

    public TeamRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public void savePlayerTeam(PlayerTeamData data) {
        String sql = "MERGE INTO player_teams (uuid, player_name, team, assigned_at) " +
                "KEY(uuid) VALUES(?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data.getUuid().toString());
            pstmt.setString(2, data.getPlayerName());
            pstmt.setString(3, data.getTeam().getId());
            pstmt.setLong(4, data.getAssignedAt());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save player team data!", e);
        }
    }

    public Map<UUID, PlayerTeamData> loadAllTeams() {
        Map<UUID, PlayerTeamData> result = new HashMap<>();
        String sql = "SELECT uuid, player_name, team, assigned_at FROM player_teams";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String playerName = rs.getString("player_name");
                GameTeam team = GameTeam.fromId(rs.getString("team"));
                long assignedAt = rs.getLong("assigned_at");
                if (team != null) {
                    result.put(uuid, new PlayerTeamData(uuid, playerName, team, assignedAt));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load all team data!", e);
        }
        return result;
    }

    public PlayerTeamData getPlayerTeam(UUID uuid) {
        String sql = "SELECT uuid, player_name, team, assigned_at FROM player_teams WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String playerName = rs.getString("player_name");
                    GameTeam team = GameTeam.fromId(rs.getString("team"));
                    long assignedAt = rs.getLong("assigned_at");
                    if (team != null) {
                        return new PlayerTeamData(uuid, playerName, team, assignedAt);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get player team data for UUID: " + uuid, e);
        }
        return null;
    }

    public void clearAllTeams() {
        String sql = "DELETE FROM player_teams";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to clear all team data!", e);
        }
    }
}
