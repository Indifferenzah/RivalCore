package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.services.BossBarService;
import com.riluttante.rivalcore.services.GameService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final RivalCorePlugin plugin;
    private final GameService gameService;
    private final BossBarService bossBarService;

    public PlayerQuitListener(RivalCorePlugin plugin, GameService gameService, BossBarService bossBarService) {
        this.plugin = plugin;
        this.gameService = gameService;
        this.bossBarService = bossBarService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!gameService.isGameRunning()) return;

        Player player = event.getPlayer();
        bossBarService.removePlayer(player);
    }
}
