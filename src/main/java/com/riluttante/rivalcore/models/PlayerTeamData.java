package com.riluttante.rivalcore.models;

import java.util.UUID;

public class PlayerTeamData {
    private UUID uuid;
    private String playerName;
    private GameTeam team;
    private long assignedAt;

    public PlayerTeamData(UUID uuid, String playerName, GameTeam team, long assignedAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.team = team;
        this.assignedAt = assignedAt;
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public GameTeam getTeam() { return team; }
    public long getAssignedAt() { return assignedAt; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
