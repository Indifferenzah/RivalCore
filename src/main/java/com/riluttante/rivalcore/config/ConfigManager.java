package com.riluttante.rivalcore.config;

import com.riluttante.rivalcore.RivalCorePlugin;
import org.bukkit.Location;

import java.util.List;

public class ConfigManager {

    private final RivalCorePlugin plugin;

    public ConfigManager(RivalCorePlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public String getString(String path) {
        return plugin.getConfig().getString(path, "");
    }

    public String getRawString(String path) {
        return plugin.getConfig().getString(path, "");
    }

    public int getInt(String path) {
        return plugin.getConfig().getInt(path);
    }

    public boolean getBoolean(String path) {
        return plugin.getConfig().getBoolean(path);
    }

    public double getDouble(String path) {
        return plugin.getConfig().getDouble(path);
    }

    public long getLong(String path) {
        return plugin.getConfig().getLong(path);
    }

    public String getMessage(String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&cRivalCore&8] ");
        String message = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        return prefix + message;
    }

    public String getRawMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
    }

    public String getPrefix() {
        return plugin.getConfig().getString("messages.prefix", "&8[&cRivalCore&8] ");
    }

    // Spawn accessors
    public String getSpawnWorld() {
        return plugin.getConfig().getString("spawn.world", "world");
    }

    public double getSpawnX() {
        return plugin.getConfig().getDouble("spawn.x", 0.0);
    }

    public double getSpawnY() {
        return plugin.getConfig().getDouble("spawn.y", 64.0);
    }

    public double getSpawnZ() {
        return plugin.getConfig().getDouble("spawn.z", 0.0);
    }

    public float getSpawnYaw() {
        return (float) plugin.getConfig().getDouble("spawn.yaw", 0.0);
    }

    public float getSpawnPitch() {
        return (float) plugin.getConfig().getDouble("spawn.pitch", 0.0);
    }

    public void saveSpawn(Location loc) {
        plugin.getConfig().set("spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("spawn.x", loc.getX());
        plugin.getConfig().set("spawn.y", loc.getY());
        plugin.getConfig().set("spawn.z", loc.getZ());
        plugin.getConfig().set("spawn.yaw", (double) loc.getYaw());
        plugin.getConfig().set("spawn.pitch", (double) loc.getPitch());
        plugin.saveConfig();
    }

    // Settings accessors
    public boolean isAutoRevealAt90() {
        return plugin.getConfig().getBoolean("settings.auto-reveal-teams-at-90min", true);
    }

    public int getInitialActionbarSeconds() {
        return plugin.getConfig().getInt("settings.initial-team-actionbar-seconds", 15);
    }

    public boolean isPlayLightningSoundOnDeath() {
        return plugin.getConfig().getBoolean("settings.play-lightning-sound-on-death", true);
    }

    public int getRespawnLockMinutes() {
        return plugin.getConfig().getInt("settings.respawn-lock-minutes", 30);
    }

    public int getTotalMatchMinutes() {
        return plugin.getConfig().getInt("settings.total-match-minutes", 120);
    }

    public boolean isAssignLateJoiners() {
        return plugin.getConfig().getBoolean("settings.assign-late-joiners-to-smaller-team", true);
    }

    public int getBossbarUpdateTicks() {
        return plugin.getConfig().getInt("settings.bossbar-update-ticks", 20);
    }

    public boolean isDefaultPvpEnabled() {
        return plugin.getConfig().getBoolean("settings.default-pvp-enabled", true);
    }

    public String getChatPrefixRed() {
        return plugin.getConfig().getString("messages.chat-prefix-red", "&c[ROSSO]");
    }

    public String getChatPrefixBlue() {
        return plugin.getConfig().getString("messages.chat-prefix-blue", "&b[BLU]");
    }

    public String getChatFormatDefault() {
        return plugin.getConfig().getString("messages.chat-format-default", "{player}&7: &f{message}");
    }

    public String getChatFormatTeam() {
        return plugin.getConfig().getString("messages.chat-format-team", "{team} {player}&7: &f{message}");
    }

    public boolean isDebugMode() {
        return plugin.getConfig().getBoolean("settings.debug", false);
    }

    /**
     * Returns the effective total match duration in minutes.
     * In debug mode each phase lasts 1 minute (4 phases = 4 minutes total).
     * In normal mode returns the configured total-match-minutes (default 120).
     */
    public int getEffectiveTotalMatchMinutes() {
        return isDebugMode() ? 4 : getTotalMatchMinutes();
    }

    // Sidebar scoreboard
    public boolean isSidebarEnabled() {
        return plugin.getConfig().getBoolean("scoreboard.enabled", true);
    }

    public String getSidebarTitle() {
        return plugin.getConfig().getString("scoreboard.title", "&6&lTop Kills");
    }

    public int getSidebarUpdateTicks() {
        return plugin.getConfig().getInt("scoreboard.update-ticks", 20);
    }

    public List<String> getSidebarLines() {
        return plugin.getConfig().getStringList("scoreboard.lines");
    }

    // Tab HP
    public boolean isTabHpEnabled() {
        return plugin.getConfig().getBoolean("tab.hp-enabled", true);
    }

    public String getTabHpFormat() {
        return plugin.getConfig().getString("tab.hp-format", "%name%     &cHP: &4%hp%");
    }

    public List<String> getTabHeader() {
        return plugin.getConfig().getStringList("tab.header");
    }

    public List<String> getTabFooter() {
        return plugin.getConfig().getStringList("tab.footer");
    }

    // World border
    public boolean isBorderEnabled() {
        return plugin.getConfig().getBoolean("border.enabled", true);
    }

    public double getBorderPhase1Start() {
        return plugin.getConfig().getDouble("border.phase-1-start", 1200.0);
    }

    public double getBorderPhase1End() {
        return plugin.getConfig().getDouble("border.phase-1-end", 800.0);
    }

    public double getBorderPhase2Start() {
        return plugin.getConfig().getDouble("border.phase-2-start", 800.0);
    }

    public double getBorderPhase2End() {
        return plugin.getConfig().getDouble("border.phase-2-end", 600.0);
    }

    public double getBorderPhase3Start() {
        return plugin.getConfig().getDouble("border.phase-3-start", 600.0);
    }

    public double getBorderPhase3End() {
        return plugin.getConfig().getDouble("border.phase-3-end", 200.0);
    }

    public long getBorderPhaseDurationSeconds() {
        return plugin.getConfig().getLong("border.phase-duration-seconds", 1800L);
    }

    // BossBar messages
    public String getBossbarPhaseInitial() {
        return plugin.getConfig().getString("messages.bossbar.phase-initial", "&aRespawn libero: {time}");
    }

    public String getBossbarPhaseTwo() {
        return plugin.getConfig().getString("messages.bossbar.phase-two", "&eReveal team tra: {time}");
    }

    public String getBossbarPhaseThree() {
        return plugin.getConfig().getString("messages.bossbar.phase-three", "&cDeathmatch tra: {time}");
    }

    public String getBossbarPhaseFinal() {
        return plugin.getConfig().getString("messages.bossbar.phase-final", "&4Fine partita tra: {time}");
    }

    public String getBossbarEnded() {
        return plugin.getConfig().getString("messages.bossbar.ended", "&cPartita terminata");
    }
}
