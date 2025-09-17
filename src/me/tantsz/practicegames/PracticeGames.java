package me.tantsz.practicegames;

import me.tantsz.practicegames.commands.*;
import me.tantsz.practicegames.database.DatabaseManager;
import me.tantsz.practicegames.database.DatabaseService;
import me.tantsz.practicegames.listeners.*;
import me.tantsz.practicegames.managers.*;
import me.tantsz.practicegames.utils.TeleportUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class PracticeGames extends JavaPlugin {
    
    private static PracticeGames instance;
    
    private DatabaseManager databaseManager;
    private DatabaseService databaseService;
    private GameManager gameManager;
    private RaceManager raceManager;
    private KangarooManager kangarooManager;
    private GrapplerManager grapplerManager;
    private CheckpointManager checkpointManager;
    private TeleportUtil teleportUtil;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        initializeManagers();
        registerCommands();
        registerListeners();
        gameManager.initializeServer();
        
        getLogger().info("PracticeGames carregado com sucesso!");
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnections();
        }
        getLogger().info("PracticeGames descarregado!");
    }
    
    private void initializeManagers() {
        teleportUtil = new TeleportUtil(this);
        checkpointManager = new CheckpointManager(this);
        databaseManager = new DatabaseManager(this);
        databaseService = new DatabaseService(this);
        gameManager = new GameManager(this);
        raceManager = new RaceManager(this);
        kangarooManager = new KangarooManager(this);
        grapplerManager = new GrapplerManager(this);
    }
    
    private void registerCommands() {
        if (getCommand("minigame") != null) {
            getCommand("minigame").setExecutor(new GameCommandExecutor(this));
        } else {
            getLogger().warning("Comando 'minigame' não encontrado no plugin.yml!");
        }
        
        if (getCommand("iniciar") != null) {
            getCommand("iniciar").setExecutor(new IniciarCommand(this));
        } else {
            getLogger().warning("Comando 'iniciar' não encontrado no plugin.yml!");
        }
        
        if (getCommand("cancelar") != null) {
            getCommand("cancelar").setExecutor(new CancelarCommand(this));
        } else {
            getLogger().warning("Comando 'cancelar' não encontrado no plugin.yml!");
        }
        
        if (getCommand("reiniciar") != null) {
            getCommand("reiniciar").setExecutor(new ReiniciarCommand(this));
        } else {
            getLogger().warning("Comando 'reiniciar' não encontrado no plugin.yml!");
        }
        
        if (getCommand("top") != null) {
            getCommand("top").setExecutor(new TopCommand(this));
        } else {
            getLogger().warning("Comando 'top' não encontrado no plugin.yml!");
        }
        
        if (getCommand("stats") != null) {
            getCommand("stats").setExecutor(new StatsCommand(this));
        } else {
            getLogger().warning("Comando 'stats' não encontrado no plugin.yml!");
        }
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CheckpointDetectionListener(this), this);
        getServer().getPluginManager().registerEvents(new HotbarInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new KangarooInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new GrapplerListener(this), this);
        getServer().getPluginManager().registerEvents(new GameProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new FreezeTimeListener(this), this);
        getServer().getPluginManager().registerEvents(gameManager, this);
    }
    
    public static PracticeGames getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public DatabaseService getDatabaseService() { return databaseService; }
    public GameManager getGameManager() { return gameManager; }
    public RaceManager getRaceManager() { return raceManager; }
    public KangarooManager getKangarooManager() { return kangarooManager; }
    public GrapplerManager getGrapplerManager() { return grapplerManager; }
    public CheckpointManager getCheckpointManager() { return checkpointManager; }
    public TeleportUtil getTeleportUtil() { return teleportUtil; }
}
