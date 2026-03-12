package com.riluttante.rivalcore.models;

public class GameData {
    private GameState state;
    private long startTimestamp; // epoch millis
    private boolean teamsRevealed;
    private boolean pvpEnabled;

    public GameData(GameState state, long startTimestamp, boolean teamsRevealed, boolean pvpEnabled) {
        this.state = state;
        this.startTimestamp = startTimestamp;
        this.teamsRevealed = teamsRevealed;
        this.pvpEnabled = pvpEnabled;
    }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public long getStartTimestamp() { return startTimestamp; }
    public void setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; }
    public boolean isTeamsRevealed() { return teamsRevealed; }
    public void setTeamsRevealed(boolean teamsRevealed) { this.teamsRevealed = teamsRevealed; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }
}
