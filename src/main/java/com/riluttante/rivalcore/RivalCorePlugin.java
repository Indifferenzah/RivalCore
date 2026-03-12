package com.riluttante.rivalcore;

import com.riluttante.rivalcore.commands.GameCommand;
import com.riluttante.rivalcore.commands.PvpCommand;
import com.riluttante.rivalcore.commands.RespawnCommand;
import com.riluttante.rivalcore.commands.RivalCoreCommand;
import com.riluttante.rivalcore.commands.SetSpawnCommand;
import com.riluttante.rivalcore.commands.StartCommand;
import com.riluttante.rivalcore.commands.TeamsCommand;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.database.DatabaseManager;
import com.riluttante.rivalcore.database.GameStateRepository;
import com.riluttante.rivalcore.database.TeamRepository;
import com.riluttante.rivalcore.listeners.PlayerChatListener;
import com.riluttante.rivalcore.listeners.PlayerDeathListener;
import com.riluttante.rivalcore.listeners.PlayerJoinListener;
import com.riluttante.rivalcore.listeners.PlayerQuitListener;
import com.riluttante.rivalcore.listeners.PlayerRespawnListener;
import com.riluttante.rivalcore.listeners.PvpControlListener;
import com.riluttante.rivalcore.services.BossBarService;
import com.riluttante.rivalcore.services.GameService;
import com.riluttante.rivalcore.services.MessageService;
import com.riluttante.rivalcore.services.PvpService;
import com.riluttante.rivalcore.services.SpawnService;
import com.riluttante.rivalcore.services.TeamService;
import com.riluttante.rivalcore.services.TimerService;
import org.bukkit.plugin.java.JavaPlugin;

public class RivalCorePlugin extends JavaPlugin {

    // Config
    private ConfigManager configManager;

    // Database
    private DatabaseManager databaseManager;
    private GameStateRepository gameStateRepository;
    private TeamRepository teamRepository;

    // Services
    private MessageService messageService;
    private SpawnService spawnService;
    private PvpService pvpService;
    private BossBarService bossBarService;
    private TeamService teamService;
    private TimerService timerService;
    private GameService gameService;

    @Override
    public void onEnable() {
        // 1. Config
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // 2. Database
        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        // 3. Repositories
        gameStateRepository = new GameStateRepository(databaseManager, getLogger());
        teamRepository = new TeamRepository(databaseManager, getLogger());

        // 4. Services (dependency order)
        messageService = new MessageService(this, configManager);
        spawnService = new SpawnService(this, configManager);
        pvpService = new PvpService(this, configManager, gameStateRepository);
        bossBarService = new BossBarService(this, configManager);
        teamService = new TeamService(this, configManager, teamRepository, messageService);

        // TimerService needs GameService - create without it first, set via setter
        timerService = new TimerService(this, configManager, bossBarService);

        gameService = new GameService(
            this,
            configManager,
            gameStateRepository,
            teamRepository,
            messageService,
            spawnService,
            pvpService,
            bossBarService,
            teamService,
            timerService
        );

        // Wire the circular reference
        timerService.setGameService(gameService);

        // 5. Register commands
        registerCommands();

        // 6. Register listeners
        registerListeners();

        // 7. Load state from database (restore running game if any)
        gameService.loadStateFromDatabase();

        if (configManager.isDebugMode()) {
            getLogger().warning("==============================================");
            getLogger().warning("  DEBUG MODE ATTIVO: ogni fase dura 1 minuto!");
            getLogger().warning("  Disabilita 'settings.debug: false' in prod.");
            getLogger().warning("==============================================");
        }
        getLogger().info("RivalCore enabled successfully!");
    }

    @Override
    public void onDisable() {
        // 1. Stop timer
        if (timerService != null) {
            timerService.stop();
        }

        // 2. Remove bossbar from all players
        if (bossBarService != null) {
            bossBarService.destroyBossBar();
        }

        // 3. Persist in-memory state
        if (gameService != null && gameService.getGameData() != null) {
            gameStateRepository.saveGameData(gameService.getGameData());
        }

        // 4. Close database
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("RivalCore disabled.");
    }

    private void registerCommands() {
        RivalCoreCommand rivalCoreCommand = new RivalCoreCommand(configManager, messageService);
        getCommand("rivalcore").setExecutor(rivalCoreCommand);
        getCommand("rivalcore").setTabCompleter(rivalCoreCommand);

        StartCommand startCommand = new StartCommand(gameService, messageService);
        getCommand("start").setExecutor(startCommand);
        getCommand("start").setTabCompleter(startCommand);

        TeamsCommand teamsCommand = new TeamsCommand(gameService, teamService, messageService);
        getCommand("teams").setExecutor(teamsCommand);
        getCommand("teams").setTabCompleter(teamsCommand);

        PvpCommand pvpCommand = new PvpCommand(pvpService, messageService);
        getCommand("pvp").setExecutor(pvpCommand);
        getCommand("pvp").setTabCompleter(pvpCommand);

        SetSpawnCommand setSpawnCommand = new SetSpawnCommand(spawnService, messageService);
        getCommand("setspawn").setExecutor(setSpawnCommand);
        getCommand("setspawn").setTabCompleter(setSpawnCommand);

        GameCommand gameCommand = new GameCommand(gameService, messageService);
        getCommand("game").setExecutor(gameCommand);
        getCommand("game").setTabCompleter(gameCommand);

        RespawnCommand respawnCommand = new RespawnCommand(gameService, messageService);
        getCommand("respawn").setExecutor(respawnCommand);
        getCommand("respawn").setTabCompleter(respawnCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
            new PlayerDeathListener(this, gameService, messageService, configManager), this);
        getServer().getPluginManager().registerEvents(
            new PlayerRespawnListener(this, gameService, spawnService, messageService), this);
        getServer().getPluginManager().registerEvents(
            new PlayerJoinListener(this, gameService, bossBarService, teamService), this);
        getServer().getPluginManager().registerEvents(
            new PlayerQuitListener(this, gameService, bossBarService), this);
        getServer().getPluginManager().registerEvents(
            new PvpControlListener(this, pvpService, messageService), this);
        getServer().getPluginManager().registerEvents(
            new PlayerChatListener(gameService, teamService, configManager), this);
    }

    // Getters for services (useful for commands/listeners if needed)
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public GameStateRepository getGameStateRepository() { return gameStateRepository; }
    public TeamRepository getTeamRepository() { return teamRepository; }
    public MessageService getMessageService() { return messageService; }
    public SpawnService getSpawnService() { return spawnService; }
    public PvpService getPvpService() { return pvpService; }
    public BossBarService getBossBarService() { return bossBarService; }
    public TeamService getTeamService() { return teamService; }
    public TimerService getTimerService() { return timerService; }
    public GameService getGameService() { return gameService; }
}
