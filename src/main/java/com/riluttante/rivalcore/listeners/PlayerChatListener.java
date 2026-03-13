package com.riluttante.rivalcore.listeners;

import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.models.GameTeam;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.TeamService;
import com.riluttante.rivalcore.utils.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
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
        Player player = event.getPlayer();

        boolean showTeam = gameService.isGameRunning()
            && teamService.isTeamsRevealed()
            && !gameService.isEliminated(player.getUniqueId());

        final String format;
        final String teamPrefix;

        if (showTeam) {
            GameTeam team = teamService.getTeam(player.getUniqueId());
            if (team != null) {
                format = configManager.getChatFormatTeam();
                teamPrefix = team == GameTeam.RED
                    ? configManager.getChatPrefixRed()
                    : configManager.getChatPrefixBlue();
            } else {
                format = configManager.getChatFormatDefault();
                teamPrefix = "";
            }
        } else {
            format = configManager.getChatFormatDefault();
            teamPrefix = "";
        }

        event.renderer((source, sourceDisplayName, message, viewer) ->
            buildMessage(format, player.getName(), teamPrefix, message)
        );
    }

    /**
     * Builds the chat component by splitting the format on {message},
     * resolving {team} and {player} in the surrounding text, then
     * injecting the Adventure message component in the middle.
     */
    private Component buildMessage(String format, String playerName,
                                   String teamPrefix, Component message) {
        String resolved = format
            .replace("{team}", teamPrefix)
            .replace("{player}", playerName);

        int msgIdx = resolved.indexOf("{message}");
        if (msgIdx == -1) {
            return ColorUtil.colorize(resolved);
        }

        String left  = resolved.substring(0, msgIdx);
        String right = resolved.substring(msgIdx + "{message}".length());

        Component result = ColorUtil.colorize(left).append(message);
        if (!right.isEmpty()) {
            result = result.append(ColorUtil.colorize(right));
        }
        return result;
    }
}
