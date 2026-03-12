package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.services.SpawnService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final RivalCorePlugin plugin;
    private final GameService gameService;
    private final SpawnService spawnService;
    private final MessageService messageService;

    public PlayerRespawnListener(RivalCorePlugin plugin, GameService gameService,
                                  SpawnService spawnService, MessageService messageService) {
        this.plugin = plugin;
        this.gameService = gameService;
        this.spawnService = spawnService;
        this.messageService = messageService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Always redirect to configured spawn if it is set and the world exists.
        // This works both during a game and outside, so /setspawn can be tested freely.
        Location spawn = spawnService.getSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }

        // Game-specific logic below
        if (!gameService.isGameRunning()) return;

        if (gameService.isEliminated(player.getUniqueId())) {
            // Permanently dead: force SPECTATOR one tick after respawn to avoid race conditions
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.setGameMode(GameMode.SPECTATOR);
                    messageService.sendMessage(player, "player-died-spectator");
                }
            }, 1L);
        } else {
            // Still alive: ensure SURVIVAL mode (safeguard against gamemode changes)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && player.getGameMode() != GameMode.SURVIVAL) {
                    player.setGameMode(GameMode.SURVIVAL);
                }
            }, 1L);
        }
    }
}
