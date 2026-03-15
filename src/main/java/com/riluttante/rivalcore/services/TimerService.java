package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.models.GamePhase;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class TimerService {

    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;
    private final BossBarService bossBarService;
    private final Set<GamePhase> firedMilestones = new HashSet<>();
    private GameService gameService;
    private long startTimestamp = 0L;
    private BukkitTask task;

    public TimerService(RivalCorePlugin plugin, ConfigManager configManager, BossBarService bossBarService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.bossBarService = bossBarService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void start(long startTimestamp) {
        this.startTimestamp = startTimestamp;
        firedMilestones.clear();

        // Pre-populate milestones that have already passed (in case of restore)
        long elapsedMinutes = getElapsedSeconds() / 60;
        int total = configManager.getEffectiveTotalMatchMinutes();
        if (elapsedMinutes >= total / 4) firedMilestones.add(GamePhase.PHASE_TWO);
        if (elapsedMinutes >= total / 2) firedMilestones.add(GamePhase.PHASE_THREE);
        if (elapsedMinutes >= total * 3 / 4) firedMilestones.add(GamePhase.FINAL);
        if (elapsedMinutes >= total) firedMilestones.add(GamePhase.ENDED);

        int updateTicks = configManager.getBossbarUpdateTicks();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, updateTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        firedMilestones.clear();
    }

    private void tick() {
        long remaining = getRemainingSeconds();
        GamePhase phase = getCurrentPhase();

        bossBarService.updateBossBar(phase, remaining);

        long elapsedMinutes = getElapsedSeconds() / 60;
        int total = configManager.getEffectiveTotalMatchMinutes();

        if (elapsedMinutes >= total / 4 && !firedMilestones.contains(GamePhase.PHASE_TWO)) {
            firedMilestones.add(GamePhase.PHASE_TWO);
            if (gameService != null) gameService.onTimerMilestone(GamePhase.PHASE_TWO);
        }
        if (elapsedMinutes >= total / 2 && !firedMilestones.contains(GamePhase.PHASE_THREE)) {
            firedMilestones.add(GamePhase.PHASE_THREE);
            if (gameService != null) gameService.onTimerMilestone(GamePhase.PHASE_THREE);
        }
        if (elapsedMinutes >= total * 3 / 4 && !firedMilestones.contains(GamePhase.FINAL)) {
            firedMilestones.add(GamePhase.FINAL);
            if (gameService != null) gameService.onTimerMilestone(GamePhase.FINAL);
        }
        if (elapsedMinutes >= total && !firedMilestones.contains(GamePhase.ENDED)) {
            firedMilestones.add(GamePhase.ENDED);
            if (gameService != null) gameService.onTimerMilestone(GamePhase.ENDED);
        }
    }

    public long getRemainingSeconds() {
        long totalMillis = configManager.getEffectiveTotalMatchMinutes() * 60L * 1000L;
        long elapsed = System.currentTimeMillis() - startTimestamp;
        return Math.max(0L, (totalMillis - elapsed) / 1000L);
    }

    public long getElapsedSeconds() {
        if (startTimestamp == 0L) return 0L;
        return (System.currentTimeMillis() - startTimestamp) / 1000L;
    }

    public GamePhase getCurrentPhase() {
        long elapsedMinutes = getElapsedSeconds() / 60;
        int total = configManager.getEffectiveTotalMatchMinutes();
        if (elapsedMinutes < total / 4) return GamePhase.INITIAL;
        if (elapsedMinutes < total / 2) return GamePhase.PHASE_TWO;
        if (elapsedMinutes < total * 3 / 4) return GamePhase.PHASE_THREE;
        if (elapsedMinutes < total) return GamePhase.FINAL;
        return GamePhase.ENDED;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public boolean isRunning() {
        return task != null;
    }
}
