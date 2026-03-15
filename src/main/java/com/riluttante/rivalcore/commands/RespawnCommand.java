package com.riluttante.rivalcore.commands;

import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class RespawnCommand implements CommandExecutor, TabCompleter {

    private final GameService gameService;
    private final MessageService messageService;

    public RespawnCommand(GameService gameService, MessageService messageService) {
        this.gameService = gameService;
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rivalcore.command.respawn")) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "no-permission");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cNon hai il permesso."));
            }
            return true;
        }

        if (!gameService.isGameRunning()) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "game-not-started");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cNessuna partita in corso."));
            }
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "respawn-no-target");
            } else {
                sender.sendMessage(ColorUtil.colorize("&cGiocatore non trovato o non online."));
            }
            return true;
        }

        if (!gameService.isEliminated(target.getUniqueId())) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "respawn-not-eliminated", "player", target.getName());
            } else {
                sender.sendMessage(ColorUtil.colorize("&e" + target.getName() + " non è stato eliminato."));
            }
            return true;
        }

        gameService.respawnPlayer(target);
        messageService.broadcastMessage("respawn-broadcast", "player", target.getName());
        return true;
    }

    private void sendUsage(CommandSender sender) {
        if (sender instanceof Player player) {
            messageService.sendMessage(player, "respawn-usage");
        } else {
            sender.sendMessage(ColorUtil.colorize("&eUso: /respawn <giocatore>"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest only online players who are currently eliminated
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> gameService.isEliminated(p.getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
