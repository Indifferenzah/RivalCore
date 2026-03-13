package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.models.GameTeam;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.services.PvpService;
import com.riluttante.rivalcore.services.TeamService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PvpControlListener implements Listener {

    private final RivalCorePlugin plugin;
    private final PvpService pvpService;
    private final MessageService messageService;
    private final TeamService teamService;
    private final GameService gameService;

    public PvpControlListener(RivalCorePlugin plugin, PvpService pvpService,
                               MessageService messageService,
                               TeamService teamService, GameService gameService) {
        this.plugin = plugin;
        this.pvpService = pvpService;
        this.messageService = messageService;
        this.teamService = teamService;
        this.gameService = gameService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;

        // Global PvP disabled
        if (!pvpService.isPvpEnabled()) {
            event.setCancelled(true);
            messageService.sendMessage(attacker, "pvp-blocked");
            return;
        }

        // Friendly fire: block same-team hits after teams are revealed
        if (gameService.isGameRunning() && teamService.isTeamsRevealed()) {
            GameTeam attackerTeam = teamService.getTeam(attacker.getUniqueId());
            GameTeam damagedTeam  = teamService.getTeam(damaged.getUniqueId());
            if (attackerTeam != null && attackerTeam == damagedTeam) {
                event.setCancelled(true);
                messageService.sendMessage(attacker, "friendly-fire-blocked");
            }
        }
    }

    /** Returns the Player responsible for the damage, handling projectiles. */
    private static Player resolveAttacker(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player p) return p;
        }
        return null;
    }
}
