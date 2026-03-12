package com.riluttante.rivalcore.commands;

import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.services.PvpService;
import com.riluttante.rivalcore.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PvpCommand implements CommandExecutor, TabCompleter {

    private final PvpService pvpService;
    private final MessageService messageService;

    public PvpCommand(PvpService pvpService, MessageService messageService) {
        this.pvpService = pvpService;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&eUsage: /pvp <enable|disable>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "enable" -> {
                if (!sender.hasPermission("rivalcore.command.pvp.enable")) {
                    if (sender instanceof Player player) {
                        messageService.sendMessage(player, "no-permission");
                    } else {
                        sender.sendMessage(ColorUtil.colorize("&cNo permission."));
                    }
                    return true;
                }
                pvpService.enablePvP();
                messageService.broadcastMessage("pvp-enabled");
            }
            case "disable" -> {
                if (!sender.hasPermission("rivalcore.command.pvp.disable")) {
                    if (sender instanceof Player player) {
                        messageService.sendMessage(player, "no-permission");
                    } else {
                        sender.sendMessage(ColorUtil.colorize("&cNo permission."));
                    }
                    return true;
                }
                pvpService.disablePvP();
                messageService.broadcastMessage("pvp-disabled");
            }
            default -> sender.sendMessage(ColorUtil.colorize("&eUsage: /pvp <enable|disable>"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("enable", "disable");
        }
        return Collections.emptyList();
    }
}
