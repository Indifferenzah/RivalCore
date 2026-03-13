package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.config.ConfigManager;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class BorderService {

    private final ConfigManager configManager;

    public BorderService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private long phaseDuration() {
        return configManager.isDebugMode()
            ? 60L
            : configManager.getBorderPhaseDurationSeconds();
    }

    public void onGameStart(World world) {
        if (!configManager.isBorderEnabled()) return;
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(configManager.getBorderPhase1Start());
        border.setSize(configManager.getBorderPhase1End(), phaseDuration());
    }

    public void onPhaseTwo(World world) {
        if (!configManager.isBorderEnabled()) return;
        WorldBorder border = world.getWorldBorder();
        border.setSize(configManager.getBorderPhase2Start());
        border.setSize(configManager.getBorderPhase2End(), phaseDuration());
    }

    public void onPhaseThree(World world) {
        if (!configManager.isBorderEnabled()) return;
        WorldBorder border = world.getWorldBorder();
        border.setSize(configManager.getBorderPhase3Start());
        border.setSize(configManager.getBorderPhase3End(), phaseDuration());
    }

    public void onFinalPhase(World world) {
        if (!configManager.isBorderEnabled()) return;
        world.getWorldBorder().setSize(configManager.getBorderPhase3End());
    }

    public void resetBorder(World world) {
        world.getWorldBorder().setSize(59_999_968D); // Minecraft max border size
    }
}
