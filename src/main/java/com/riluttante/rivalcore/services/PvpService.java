package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.database.GameStateRepository;
import com.riluttante.rivalcore.models.GameData;

public class PvpService {

    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;
    private final GameStateRepository gameStateRepository;
    private boolean pvpEnabled;

    public PvpService(RivalCorePlugin plugin, ConfigManager configManager, GameStateRepository gameStateRepository) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameStateRepository = gameStateRepository;
        this.pvpEnabled = configManager.isDefaultPvpEnabled();
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean enabled) {
        pvpEnabled = enabled;
    }

    public void enablePvP() {
        pvpEnabled = true;
        persistState();
    }

    public void disablePvP() {
        pvpEnabled = false;
        persistState();
    }

    private void persistState() {
        GameData data = gameStateRepository.loadGameData();
        data.setPvpEnabled(pvpEnabled);
        gameStateRepository.saveGameData(data);
    }

    public void loadState() {
        GameData data = gameStateRepository.loadGameData();
        pvpEnabled = data.isPvpEnabled();
    }
}
