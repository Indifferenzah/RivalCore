package com.riluttante.rivalcore.commands;

import com.riluttante.rivalcore.config.ConfigManager;
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

public class RivalCoreCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;
    private final MessageService messageService;

    public RivalCoreCommand(ConfigManager configManager, MessageService messageService) {
        this.configManager = configManager;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&7Made with \u2764 by &cRiluttante&7."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(ColorUtil.colorize(
                configManager.getPrefix() + "&cUso: /rivalcore [help|reload]"));
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&8&m                    "));
        sender.sendMessage(ColorUtil.colorize("  &c&lRivalCore &8- &7Comandi disponibili"));
        sender.sendMessage(ColorUtil.colorize("&8&m                    "));
        sender.sendMessage(ColorUtil.colorize("  &e/start &8» &7Avvia la partita"));
        sender.sendMessage(ColorUtil.colorize("  &e/game status &8» &7Stato partita e fase attuale"));
        sender.sendMessage(ColorUtil.colorize("  &e/game stop &8» &7Ferma la partita"));
        sender.sendMessage(ColorUtil.colorize("  &e/teams show &8» &7Rivela i team pubblicamente"));
        sender.sendMessage(ColorUtil.colorize("  &e/teams list &8» &7Lista giocatori per team"));
        sender.sendMessage(ColorUtil.colorize("  &e/teams warn &8» &7Mostra il team in actionbar per 15s"));
        sender.sendMessage(ColorUtil.colorize("  &e/pvp enable|disable &8» &7Abilita/disabilita PvP"));
        sender.sendMessage(ColorUtil.colorize("  &e/setspawn &8» &7Imposta lo spawn della partita"));
        sender.sendMessage(ColorUtil.colorize("  &e/respawn <giocatore> &8» &7Riporta un eliminato in partita"));
        sender.sendMessage(ColorUtil.colorize("  &e/rivalcore reload &8» &7Ricarica config.yml"));
        sender.sendMessage(ColorUtil.colorize("&8&m                    "));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("rivalcore.command.reload")) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "no-permission");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cNon hai il permesso."));
            }
            return;
        }

        configManager.reload();

        if (sender instanceof Player player) {
            messageService.sendMessage(player, "config-reloaded");
        } else {
            sender.sendMessage(ColorUtil.colorize(
                configManager.getPrefix() + "&aConfigurazione ricaricata con successo."));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(new String[]{"help", "reload"})
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .toList();
        }
        return Collections.emptyList();
    }
}
