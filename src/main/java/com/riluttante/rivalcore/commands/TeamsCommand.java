package com.riluttante.rivalcore.commands;

import com.riluttante.rivalcore.models.GameTeam;
import com.riluttante.rivalcore.models.PlayerTeamData;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.services.TeamService;
import com.riluttante.rivalcore.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamsCommand implements CommandExecutor, TabCompleter {

    private final GameService gameService;
    private final TeamService teamService;
    private final MessageService messageService;

    public TeamsCommand(GameService gameService, TeamService teamService, MessageService messageService) {
        this.gameService = gameService;
        this.teamService = teamService;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rivalcore.command.teams.show")) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "no-permission");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cNo permission."));
            }
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&eUsage: /teams <show|list>"));
            return true;
        }

        if (!gameService.isGameRunning()) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "teams-game-not-started");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cNo active game."));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "show" -> {
                if (teamService.isTeamsRevealed()) {
                    if (sender instanceof Player player) {
                        messageService.sendMessage(player, "teams-already-revealed");
                    } else {
                        sender.sendMessage(ColorUtil.colorize("&eTeams already revealed."));
                    }
                    return true;
                }
                teamService.revealAllTeamsPublicly();
                if (sender instanceof Player player) {
                    messageService.sendMessage(player, "teams-public-revealed");
                } else {
                    sender.sendMessage(ColorUtil.colorize("&aTeams revealed publicly."));
                }
            }
            case "list" -> {
                Map<UUID, PlayerTeamData> allTeams = teamService.getAllTeams();
                if (allTeams.isEmpty()) {
                    sender.sendMessage(ColorUtil.colorize("&eNessun giocatore assegnato a un team."));
                    return true;
                }
                sender.sendMessage(ColorUtil.colorize("&8&m                  "));
                sender.sendMessage(ColorUtil.colorize("  &6&lTeam List"));
                sender.sendMessage(ColorUtil.colorize("&8&m                  "));
                for (PlayerTeamData data : allTeams.values()) {
                    // Skip permanently eliminated players
                    if (gameService.isEliminated(data.getUuid())) continue;
                    String color = (data.getTeam() == GameTeam.RED) ? "&c" : "&b";
                    String teamName = (data.getTeam() == GameTeam.RED) ? "ROSSO" : "BLU";
                    sender.sendMessage(ColorUtil.colorize("  " + color + data.getPlayerName() + " &8» " + color + teamName));
                }
            }
            default -> sender.sendMessage(ColorUtil.colorize("&eUsage: /teams <show|list>"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("show", "list");
        }
        return Collections.emptyList();
    }
}
