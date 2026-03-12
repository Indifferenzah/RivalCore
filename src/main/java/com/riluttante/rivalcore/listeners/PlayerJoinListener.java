package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.services.BossBarService;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.TeamService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final RivalCorePlugin plugin;
    private final GameService gameService;
    private final BossBarService bossBarService;
    private final TeamService teamService;

    public PlayerJoinListener(RivalCorePlugin plugin, GameService gameService,
                               BossBarService bossBarService, TeamService teamService) {
        this.plugin = plugin;
        this.gameService = gameService;
        this.bossBarService = bossBarService;
        this.teamService = teamService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!gameService.isGameRunning()) return;

        // Add to bossbar
        bossBarService.addPlayer(player);

        boolean alreadyAssigned = teamService.getTeam(player.getUniqueId()) != null;

        if (alreadyAssigned) {
            // Player was already in a team - restore state
            if (teamService.isTeamsRevealed()) {
                teamService.applyScoreboardToPlayer(player);
            }
            // If they are eliminated, set spectator
            if (gameService.isEliminated(player.getUniqueId())) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                    }
                }, 1L);
            }
        } else {
            // Late joiner - try to assign team
            teamService.assignLateJoiner(player);
        }
    }
}
