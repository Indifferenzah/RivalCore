package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.database.TeamRepository;
import com.riluttante.rivalcore.models.GameTeam;
import com.riluttante.rivalcore.models.PlayerTeamData;
import com.riluttante.rivalcore.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamService {

    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;
    private final TeamRepository teamRepository;
    private final MessageService messageService;

    private final Map<UUID, PlayerTeamData> teamCache = new HashMap<>();
    private boolean teamsRevealed = false;

    public TeamService(RivalCorePlugin plugin, ConfigManager configManager,
                       TeamRepository teamRepository, MessageService messageService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.teamRepository = teamRepository;
        this.messageService = messageService;
    }

    public void assignTeams(List<Player> players) {
        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        int half = shuffled.size() / 2;

        for (int i = 0; i < shuffled.size(); i++) {
            Player player = shuffled.get(i);
            GameTeam team = (i < half) ? GameTeam.RED : GameTeam.BLUE;
            long now = System.currentTimeMillis();
            PlayerTeamData data = new PlayerTeamData(player.getUniqueId(), player.getName(), team, now);
            teamCache.put(player.getUniqueId(), data);
            teamRepository.savePlayerTeam(data);
        }
    }

    public GameTeam getTeam(UUID uuid) {
        PlayerTeamData data = teamCache.get(uuid);
        return (data != null) ? data.getTeam() : null;
    }

    public PlayerTeamData getPlayerTeamData(UUID uuid) {
        return teamCache.get(uuid);
    }

    public Map<UUID, PlayerTeamData> getAllTeams() {
        return Collections.unmodifiableMap(teamCache);
    }

    public void revealTeamToPlayer(Player player) {
        PlayerTeamData data = teamCache.get(player.getUniqueId());
        if (data == null) return;

        String teamMessage = (data.getTeam() == GameTeam.RED)
            ? configManager.getRawMessage("team-red")
            : configManager.getRawMessage("team-blue");

        int actionbarSeconds = configManager.getInitialActionbarSeconds();
        int totalTicks = actionbarSeconds * 20;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= totalTicks || !player.isOnline()) {
                    cancel();
                    return;
                }
                player.sendActionBar(ColorUtil.colorize(teamMessage));
                ticks += 40;
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    public void revealAllTeamsPublicly() {
        if (teamsRevealed) return;
        teamsRevealed = true;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Set up RED scoreboard team
        Team redTeam = scoreboard.getTeam("rivalcore_red");
        if (redTeam == null) redTeam = scoreboard.registerNewTeam("rivalcore_red");
        redTeam.color(NamedTextColor.RED);
        redTeam.prefix(ColorUtil.colorize("&c[RED] "));

        // Set up BLUE scoreboard team
        Team blueTeam = scoreboard.getTeam("rivalcore_blue");
        if (blueTeam == null) blueTeam = scoreboard.registerNewTeam("rivalcore_blue");
        blueTeam.color(NamedTextColor.AQUA);
        blueTeam.prefix(ColorUtil.colorize("&b[BLU] "));

        // Assign players to their scoreboard teams
        for (PlayerTeamData data : teamCache.values()) {
            if (data.getTeam() == GameTeam.RED) {
                redTeam.addEntry(data.getPlayerName());
            } else {
                blueTeam.addEntry(data.getPlayerName());
            }
        }

        // Send chat summary
        Component header = ColorUtil.colorize("&6--- Team Reveal ---");
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(header));
        Bukkit.getConsoleSender().sendMessage(header);

        for (PlayerTeamData data : teamCache.values()) {
            String color = (data.getTeam() == GameTeam.RED) ? "&c" : "&b";
            String teamName = (data.getTeam() == GameTeam.RED) ? "ROSSO" : "BLU";
            Component line = ColorUtil.colorize(color + data.getPlayerName() + " &7-> " + color + teamName);
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(line));
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }

    public void applyScoreboardToPlayer(Player player) {
        PlayerTeamData data = teamCache.get(player.getUniqueId());
        if (data == null) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (data.getTeam() == GameTeam.RED) {
            Team redTeam = scoreboard.getTeam("rivalcore_red");
            if (redTeam != null) redTeam.addEntry(player.getName());
        } else {
            Team blueTeam = scoreboard.getTeam("rivalcore_blue");
            if (blueTeam != null) blueTeam.addEntry(player.getName());
        }
    }

    /**
     * Removes the player's name tag prefix by unregistering them from their scoreboard team.
     * Called when a player is permanently eliminated.
     */
    public void removeFromScoreboard(UUID uuid) {
        // If teams are not yet publicly revealed there are no scoreboard teams to update.
        if (!teamsRevealed) return;

        PlayerTeamData data = teamCache.get(uuid);
        if (data == null) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        // Only touch the player's actual team — touching the other team sends a spurious
        // TEAM_MODIFY packet to all clients which causes the nametag color to flicker/reset
        // for every other player still in the game.
        String teamId = data.getTeam() == GameTeam.RED ? "rivalcore_red" : "rivalcore_blue";
        Team team = scoreboard.getTeam(teamId);
        if (team != null) team.removeEntry(data.getPlayerName());
    }

    /**
     * Re-adds the player to their scoreboard team after an admin respawn.
     */
    public void addToScoreboard(UUID uuid) {
        PlayerTeamData data = teamCache.get(uuid);
        if (data == null || !teamsRevealed) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (data.getTeam() == GameTeam.RED) {
            Team redTeam = scoreboard.getTeam("rivalcore_red");
            if (redTeam != null) redTeam.addEntry(data.getPlayerName());
        } else {
            Team blueTeam = scoreboard.getTeam("rivalcore_blue");
            if (blueTeam != null) blueTeam.addEntry(data.getPlayerName());
        }
    }

    public boolean isTeamsRevealed() {
        return teamsRevealed;
    }

    public void setTeamsRevealed(boolean teamsRevealed) {
        this.teamsRevealed = teamsRevealed;
    }

    public void assignLateJoiner(Player player) {
        if (!configManager.isAssignLateJoiners()) return;
        if (teamCache.containsKey(player.getUniqueId())) return;

        long redCount = teamCache.values().stream().filter(d -> d.getTeam() == GameTeam.RED).count();
        long blueCount = teamCache.values().stream().filter(d -> d.getTeam() == GameTeam.BLUE).count();
        GameTeam assigned = (redCount <= blueCount) ? GameTeam.RED : GameTeam.BLUE;

        long now = System.currentTimeMillis();
        PlayerTeamData data = new PlayerTeamData(player.getUniqueId(), player.getName(), assigned, now);
        teamCache.put(player.getUniqueId(), data);
        teamRepository.savePlayerTeam(data);

        String teamName = (assigned == GameTeam.RED) ? "ROSSO" : "BLU";
        messageService.sendMessage(player, "late-joiner-assigned", "team", teamName);
    }

    public void clearTeams() {
        teamCache.clear();
        teamRepository.clearAllTeams();
        teamsRevealed = false;
        cleanupScoreboardTeams();
    }

    public void reloadFromDatabase() {
        teamCache.clear();
        teamCache.putAll(teamRepository.loadAllTeams());
    }

    private void cleanupScoreboardTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team redTeam = scoreboard.getTeam("rivalcore_red");
        if (redTeam != null) redTeam.unregister();
        Team blueTeam = scoreboard.getTeam("rivalcore_blue");
        if (blueTeam != null) blueTeam.unregister();
    }

    public int getTeamCount(GameTeam team) {
        return (int) teamCache.values().stream().filter(d -> d.getTeam() == team).count();
    }
}
