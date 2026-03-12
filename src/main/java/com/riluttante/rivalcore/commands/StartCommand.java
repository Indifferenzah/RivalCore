package com.riluttante.rivalcore.commands;

import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class StartCommand implements CommandExecutor, TabCompleter {

    private final GameService gameService;
    private final MessageService messageService;

    public StartCommand(GameService gameService, MessageService messageService) {
        this.gameService = gameService;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rivalcore.command.start")) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "no-permission");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cNo permission."));
            }
            return true;
        }

        gameService.startGame(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
