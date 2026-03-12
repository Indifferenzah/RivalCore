package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.models.GamePhase;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final RivalCorePlugin plugin;
    private final GameService gameService;
    private final MessageService messageService;
    private final ConfigManager configManager;

    public PlayerDeathListener(RivalCorePlugin plugin, GameService gameService,
                                MessageService messageService, ConfigManager configManager) {
        this.plugin = plugin;
        this.gameService = gameService;
        this.messageService = messageService;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameService.isGameRunning()) return;

        Player player = event.getEntity();

        // Play lightning sound to all online players if configured
        if (configManager.isPlayLightningSoundOnDeath()) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
            }
        }

        GamePhase phase = gameService.getCurrentPhase();
        if (phase == null) return;

        // If phase is PHASE_TWO or later, this is a permanent death
        if (phase != GamePhase.INITIAL && phase != GamePhase.ENDED) {
            gameService.markEliminated(player.getUniqueId());
            messageService.broadcastMessage("death-broadcast", "player", player.getName());
        }
    }
}
