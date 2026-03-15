package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class MessageService {

    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;

    public MessageService(RivalCorePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    private String applyPlaceholders(String text, String... placeholders) {
        if (placeholders == null || placeholders.length == 0) return text;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return text;
    }

    public void sendMessage(Player player, String messageKey) {
        String raw = configManager.getMessage(messageKey);
        player.sendMessage(ColorUtil.colorize(raw));
    }

    public void sendMessage(Player player, String messageKey, String... placeholders) {
        String raw = configManager.getMessage(messageKey);
        raw = applyPlaceholders(raw, placeholders);
        player.sendMessage(ColorUtil.colorize(raw));
    }

    public void broadcastMessage(String messageKey) {
        String raw = configManager.getMessage(messageKey);
        Component component = ColorUtil.colorize(raw);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public void broadcastMessage(String messageKey, String... placeholders) {
        String raw = configManager.getMessage(messageKey);
        raw = applyPlaceholders(raw, placeholders);
        Component component = ColorUtil.colorize(raw);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public void broadcastRaw(String rawMessage) {
        Component component = ColorUtil.colorize(rawMessage);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public void sendActionBar(Player player, String text) {
        player.sendActionBar(ColorUtil.colorize(text));
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
        Title adventureTitle = Title.title(
                ColorUtil.colorize(title),
                ColorUtil.colorize(subtitle),
                times
        );
        player.showTitle(adventureTitle);
    }

    public void sendNoPermission(Player player) {
        sendMessage(player, "no-permission");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
