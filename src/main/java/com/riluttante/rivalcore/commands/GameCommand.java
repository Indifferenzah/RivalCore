package com.riluttante.rivalcore.commands;

import com.riluttante.rivalcore.models.GamePhase;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameCommand implements CommandExecutor, TabCompleter {

    private final GameService gameService;
    private final MessageService messageService;

    public GameCommand(GameService gameService, MessageService messageService) {
        this.gameService = gameService;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rivalcore.command.game")) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "no-permission");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cNo permission."));
            }
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&eUsage: /game <status|stop>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status" -> {
                if (!gameService.isGameRunning()) {
                    if (sender instanceof Player player) {
                        messageService.sendMessage(player, "game-status-not-running");
                    } else {
                        sender.sendMessage(ColorUtil.colorize("&cNo game running."));
                    }
                    return true;
                }
                GamePhase phase = gameService.getCurrentPhase();
                long remaining = gameService.getRemainingSeconds();
                String phaseStr = (phase != null) ? phase.name() : "UNKNOWN";
                String timeStr = ColorUtil.formatTime(Math.max(0, remaining));

                String raw = messageService.getConfigManager().getMessage("game-status-running");
                raw = raw.replace("{phase}", phaseStr).replace("{time}", timeStr);
                sender.sendMessage(ColorUtil.colorize(raw));
            }
            case "stop" -> {
                gameService.stopGame(sender);
            }
            default -> sender.sendMessage(ColorUtil.colorize("&eUsage: /game <status|stop>"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("status", "stop");
        }
        return Collections.emptyList();
    }
}
