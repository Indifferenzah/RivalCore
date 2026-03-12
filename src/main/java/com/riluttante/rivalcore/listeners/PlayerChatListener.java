package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.models.GameTeam;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.TeamService;
import com.riluttante.rivalcore.utils.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerChatListener implements Listener {

    private final GameService gameService;
    private final TeamService teamService;
    private final ConfigManager configManager;

    public PlayerChatListener(GameService gameService, TeamService teamService,
                               ConfigManager configManager) {
        this.gameService = gameService;
        this.teamService = teamService;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!gameService.isGameRunning()) return;

        // Show team prefix only after teams have been publicly revealed
        if (!teamService.isTeamsRevealed()) return;

        Player player = event.getPlayer();

        // Eliminated players (spectators) chat without team prefix
        if (gameService.isEliminated(player.getUniqueId())) return;

        GameTeam team = teamService.getTeam(player.getUniqueId());
        if (team == null) return;

        boolean isRed = team == GameTeam.RED;
        // Use dedicated methods with hardcoded fallbacks — safe even if the server's
        // config.yml was created before these keys were added.
        String rawPrefix = isRed ? configManager.getChatPrefixRed() : configManager.getChatPrefixBlue();
        TextColor nameColor = isRed ? NamedTextColor.RED : NamedTextColor.AQUA;

        // Format: [ROSSO] PlayerName: message
        Component prefix = ColorUtil.colorize(rawPrefix);
        Component space = Component.text(" ");
        Component name = Component.text(player.getName(), nameColor);
        Component separator = Component.text(": ", NamedTextColor.GRAY);

        event.renderer((source, sourceDisplayName, message, viewer) ->
            prefix.append(space).append(name).append(separator).append(message)
        );
    }
}
