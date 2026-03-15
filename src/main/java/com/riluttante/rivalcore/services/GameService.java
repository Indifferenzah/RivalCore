package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.database.GameStateRepository;
import com.riluttante.rivalcore.database.TeamRepository;
import com.riluttante.rivalcore.models.GameData;
import com.riluttante.rivalcore.models.GamePhase;
import com.riluttante.rivalcore.models.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GameService {

    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;
    private final GameStateRepository gameStateRepository;
    private final TeamRepository teamRepository;
    private final MessageService messageService;
    private final SpawnService spawnService;
    private final PvpService pvpService;
    private final BossBarService bossBarService;
    private final TeamService teamService;
    private final TimerService timerService;
    private final Set<UUID> eliminatedPlayers = new HashSet<>();
    private GameData currentGame;
    // Optional services wired after construction
    private KillTrackerService killTrackerService;
    private BorderService borderService;
    private SidebarService sidebarService;

    public GameService(RivalCorePlugin plugin,
                       ConfigManager configManager,
                       GameStateRepository gameStateRepository,
                       TeamRepository teamRepository,
                       MessageService messageService,
                       SpawnService spawnService,
                       PvpService pvpService,
                       BossBarService bossBarService,
                       TeamService teamService,
                       TimerService timerService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameStateRepository = gameStateRepository;
        this.teamRepository = teamRepository;
        this.messageService = messageService;
        this.spawnService = spawnService;
        this.pvpService = pvpService;
        this.bossBarService = bossBarService;
        this.teamService = teamService;
        this.timerService = timerService;
    }

    public void setKillTrackerService(KillTrackerService s) {
        this.killTrackerService = s;
    }

    public void setBorderService(BorderService s) {
        this.borderService = s;
    }

    public void setSidebarService(SidebarService s) {
        this.sidebarService = s;
    }

    public void startGame(CommandSender sender) {
        if (isGameRunning()) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "game-already-started");
            } else {
                sender.sendMessage("Game already started.");
            }
            return;
        }

        long startTimestamp = System.currentTimeMillis();

        // Clear old teams and assign new ones
        teamService.clearTeams();
        eliminatedPlayers.clear();

        ArrayList<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (!onlinePlayers.isEmpty()) {
            teamService.assignTeams(onlinePlayers);
        }

        // Update game data
        boolean defaultPvp = configManager.isDefaultPvpEnabled();
        currentGame = new GameData(GameState.RUNNING, startTimestamp, false, defaultPvp);
        gameStateRepository.saveGameData(currentGame);

        pvpService.setPvpEnabled(defaultPvp);

        // Start BossBar
        bossBarService.createBossBar();
        bossBarService.addAllOnlinePlayers();

        // Start timer
        timerService.start(startTimestamp);

        // Start world border phase 1
        if (borderService != null) {
            World world = Bukkit.getWorld(configManager.getSpawnWorld());
            if (world != null) borderService.onGameStart(world);
        }

        // Reveal team to each player via actionbar
        for (Player player : onlinePlayers) {
            teamService.revealTeamToPlayer(player);
        }

        // Broadcast start message
        messageService.broadcastMessage("game-started");

        plugin.getLogger().info("Game started by: " + sender.getName());
    }

    public void stopGame(CommandSender sender) {
        if (!isGameRunning()) {
            if (sender instanceof Player player) {
                messageService.sendMessage(player, "game-not-started");
            } else {
                sender.sendMessage("No game running.");
            }
            return;
        }

        timerService.stop();
        bossBarService.destroyBossBar();
        if (borderService != null) {
            World world = Bukkit.getWorld(configManager.getSpawnWorld());
            if (world != null) borderService.resetBorder(world);
        }

        // Teleport everyone to spawn and disable PvP before clearing state
        spawnService.teleportAllToSpawn();
        pvpService.disablePvP();

        eliminatedPlayers.clear();
        currentGame = new GameData(GameState.WAITING, 0L, false, configManager.isDefaultPvpEnabled());
        gameStateRepository.resetGameData();

        messageService.broadcastMessage("game-stopped");
        plugin.getLogger().info("Game stopped by: " + sender.getName());
    }

    public boolean isGameRunning() {
        return currentGame != null && currentGame.getState() == GameState.RUNNING;
    }

    public boolean isRespawnAllowed() {
        if (!isGameRunning()) return true;
        GamePhase phase = timerService.getCurrentPhase();
        return phase == GamePhase.INITIAL;
    }

    public void onTimerMilestone(GamePhase phase) {
        switch (phase) {
            case PHASE_TWO -> {
                messageService.broadcastMessage("after-30");
                if (borderService != null) {
                    World world = Bukkit.getWorld(configManager.getSpawnWorld());
                    if (world != null) borderService.onPhaseTwo(world);
                }
            }
            case PHASE_THREE -> {
                messageService.broadcastMessage("after-60");
                if (borderService != null) {
                    World world = Bukkit.getWorld(configManager.getSpawnWorld());
                    if (world != null) borderService.onPhaseThree(world);
                }
            }
            case FINAL -> {
                messageService.broadcastMessage("after-90");
                if (configManager.isAutoRevealAt90() && !teamService.isTeamsRevealed()) {
                    teamService.revealAllTeamsPublicly();
                    if (currentGame != null) {
                        currentGame.setTeamsRevealed(true);
                        gameStateRepository.saveGameData(currentGame);
                    }
                }
                if (borderService != null) {
                    World world = Bukkit.getWorld(configManager.getSpawnWorld());
                    if (world != null) borderService.onFinalPhase(world);
                }
            }
            case ENDED -> {
                onGameEnd();
            }
            default -> {
            }
        }
    }

    public void onGameEnd() {
        messageService.broadcastMessage("timer-ended");
        pvpService.disablePvP();
        spawnService.teleportAllToSpawn();
        if (borderService != null) {
            World world = Bukkit.getWorld(configManager.getSpawnWorld());
            if (world != null) borderService.resetBorder(world);
        }

        if (currentGame != null) {
            currentGame.setState(GameState.ENDED);
            currentGame.setPvpEnabled(false);
            gameStateRepository.saveGameData(currentGame);
        }

        bossBarService.updateBossBar(GamePhase.ENDED, 0L);
    }

    public void loadStateFromDatabase() {
        GameData data = gameStateRepository.loadGameData();
        currentGame = data;

        if (data.getState() == GameState.RUNNING) {
            plugin.getLogger().info("Restoring running game from database...");

            // Reload teams
            teamService.reloadFromDatabase();
            eliminatedPlayers.clear();

            // Restore PvP state
            pvpService.setPvpEnabled(data.isPvpEnabled());

            // Restore bossbar
            bossBarService.createBossBar();
            bossBarService.addAllOnlinePlayers();

            // Restore timer
            timerService.start(data.getStartTimestamp());

            // Restore world border to current phase
            if (borderService != null) {
                World world = Bukkit.getWorld(configManager.getSpawnWorld());
                if (world != null) {
                    switch (timerService.getCurrentPhase()) {
                        case INITIAL -> borderService.onGameStart(world);
                        case PHASE_TWO -> borderService.onPhaseTwo(world);
                        case PHASE_THREE -> borderService.onPhaseThree(world);
                        case FINAL -> borderService.onFinalPhase(world);
                        default -> {
                        }
                    }
                }
            }

            // Re-apply scoreboard if teams were revealed
            if (data.isTeamsRevealed()) {
                teamService.setTeamsRevealed(true);
                // Re-apply scoreboard entries for all known players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    teamService.applyScoreboardToPlayer(player);
                }
            }

            plugin.getLogger().info("Game state restored successfully.");
        } else {
            pvpService.setPvpEnabled(configManager.isDefaultPvpEnabled());
        }
    }

    public void markEliminated(UUID uuid) {
        eliminatedPlayers.add(uuid);
        // Remove name tag prefix so eliminated players look like spectators to others
        teamService.removeFromScoreboard(uuid);
    }

    public boolean isEliminated(UUID uuid) {
        return eliminatedPlayers.contains(uuid);
    }

    public java.util.Set<UUID> getEliminatedPlayers() {
        return java.util.Collections.unmodifiableSet(eliminatedPlayers);
    }

    /**
     * Restores an eliminated player back into the game.
     * Removes from elimination set, teleports to spawn, sets SURVIVAL, restores name tag.
     */
    public void respawnPlayer(Player player) {
        eliminatedPlayers.remove(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
        spawnService.teleportToSpawn(player);
        teamService.addToScoreboard(player.getUniqueId());
        messageService.sendMessage(player, "respawn-restored");
    }

    public GameData getGameData() {
        return currentGame;
    }

    public GamePhase getCurrentPhase() {
        if (!isGameRunning()) return null;
        return timerService.getCurrentPhase();
    }

    public long getRemainingSeconds() {
        return timerService.getRemainingSeconds();
    }
}
