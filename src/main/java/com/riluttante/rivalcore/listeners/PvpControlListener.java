package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.services.PvpService;
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

    public PvpControlListener(RivalCorePlugin plugin, PvpService pvpService, MessageService messageService) {
        this.plugin = plugin;
        this.pvpService = pvpService;
        this.messageService = messageService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (pvpService.isPvpEnabled()) return;

        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        if (!(damaged instanceof Player damagedPlayer)) return;

        // Direct player attack
        if (damager instanceof Player attackerPlayer) {
            event.setCancelled(true);
            messageService.sendMessage(attackerPlayer, "pvp-blocked");
            return;
        }

        // Projectile shot by player
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player attackerPlayer) {
                event.setCancelled(true);
                messageService.sendMessage(attackerPlayer, "pvp-blocked");
            }
        }
    }
}
