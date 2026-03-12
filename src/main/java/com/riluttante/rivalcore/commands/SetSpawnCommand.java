package com.riluttante.rivalcore.commands;

import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.services.SpawnService;
import com.riluttante.rivalcore.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetSpawnCommand implements CommandExecutor, TabCompleter {

    private final SpawnService spawnService;
    private final MessageService messageService;

    public SetSpawnCommand(SpawnService spawnService, MessageService messageService) {
        this.spawnService = spawnService;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.colorize("&cThis command can only be used by players."));
            return true;
        }

        if (!player.hasPermission("rivalcore.command.setspawn")) {
            messageService.sendMessage(player, "no-permission");
            return true;
        }

        spawnService.setSpawn(player.getLocation());
        messageService.sendMessage(player, "spawn-set");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
