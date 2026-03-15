package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.models.GamePhase;
import com.riluttante.rivalcore.utils.ColorUtil;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BossBarService {

    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;
    private BossBar bossBar;

    public BossBarService(RivalCorePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void createBossBar() {
        String initialText = configManager.getBossbarPhaseInitial().replace("{time}", "00:00");
        bossBar = BossBar.bossBar(
                ColorUtil.colorize(initialText),
                1.0f,
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS
        );
    }

    public void updateBossBar(GamePhase phase, long remainingSeconds) {
        if (bossBar == null) return;

        long totalSeconds = configManager.getEffectiveTotalMatchMinutes() * 60L;
        float progress = (totalSeconds > 0)
                ? Math.max(0f, Math.min(1f, (float) remainingSeconds / (float) totalSeconds))
                : 0f;

        String timeFormatted = ColorUtil.formatTime(Math.max(0, remainingSeconds));
        String rawText;
        BossBar.Color color;

        switch (phase) {
            case INITIAL -> {
                rawText = configManager.getBossbarPhaseInitial().replace("{time}", timeFormatted);
                color = BossBar.Color.GREEN;
            }
            case PHASE_TWO -> {
                rawText = configManager.getBossbarPhaseTwo().replace("{time}", timeFormatted);
                color = BossBar.Color.YELLOW;
            }
            case PHASE_THREE -> {
                rawText = configManager.getBossbarPhaseThree().replace("{time}", timeFormatted);
                color = BossBar.Color.RED;
            }
            case FINAL -> {
                rawText = configManager.getBossbarPhaseFinal().replace("{time}", timeFormatted);
                color = BossBar.Color.PINK;
            }
            default -> {
                rawText = configManager.getBossbarEnded();
                color = BossBar.Color.WHITE;
                progress = 0f;
            }
        }

        bossBar.name(ColorUtil.colorize(rawText));
        bossBar.color(color);
        bossBar.progress(progress);
    }

    public void addPlayer(Player player) {
        if (bossBar != null) {
            player.showBossBar(bossBar);
        }
    }

    public void removePlayer(Player player) {
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    public void addAllOnlinePlayers() {
        if (bossBar == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }

    public void removeAllPlayers() {
        if (bossBar == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bossBar);
        }
    }

    public void destroyBossBar() {
        if (bossBar != null) {
            removeAllPlayers();
            bossBar = null;
        }
    }

    public boolean isActive() {
        return bossBar != null;
    }
}
