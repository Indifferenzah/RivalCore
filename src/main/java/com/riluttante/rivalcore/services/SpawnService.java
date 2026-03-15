package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpawnService {

    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;

    public SpawnService(RivalCorePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public Location getSpawn() {
        String worldName = configManager.getSpawnWorld();
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(
                world,
                configManager.getSpawnX(),
                configManager.getSpawnY(),
                configManager.getSpawnZ(),
                configManager.getSpawnYaw(),
                configManager.getSpawnPitch()
        );
    }

    public void setSpawn(Location loc) {
        configManager.saveSpawn(loc);
    }

    public boolean hasValidSpawn() {
        String worldName = configManager.getSpawnWorld();
        return Bukkit.getWorld(worldName) != null;
    }

    public void teleportToSpawn(Player player) {
        Location spawn = getSpawn();
        if (spawn != null) {
            player.teleport(spawn);
        }
    }

    public void teleportAllToSpawn() {
        Location spawn = getSpawn();
        if (spawn == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawn);
        }
    }
}
