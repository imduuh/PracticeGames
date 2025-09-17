package me.tantsz.practicegames.managers;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CheckpointManager {
    
    private final PracticeGames plugin;
    private final File checkpointsFile;
    private FileConfiguration checkpointsConfig;
    
    public CheckpointManager(PracticeGames plugin) {
        this.plugin = plugin;
        this.checkpointsFile = new File(plugin.getDataFolder(), "checkpoints.yml");
        loadCheckpoints();
    }
    
    private void loadCheckpoints() {
        if (!checkpointsFile.exists()) {
            plugin.saveResource("checkpoints.yml", false);
        }
        checkpointsConfig = YamlConfiguration.loadConfiguration(checkpointsFile);
    }
    
    private void saveCheckpoints() {
        try {
            checkpointsConfig.save(checkpointsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar checkpoints: " + e.getMessage());
        }
    }
    
    public boolean createPersonalizedCheckpoint(String playerName, String mapType, String checkpointName, Location location) {
        String path = "players." + playerName + "." + mapType + "." + checkpointName;
        
        checkpointsConfig.set(path + ".world", location.getWorld().getName());
        checkpointsConfig.set(path + ".x", location.getX());
        checkpointsConfig.set(path + ".y", location.getY());
        checkpointsConfig.set(path + ".z", location.getZ());
        checkpointsConfig.set(path + ".yaw", location.getYaw());
        checkpointsConfig.set(path + ".pitch", location.getPitch());
        
        saveCheckpoints();
        return true;
    }
    
    public String getPersonalizedCheckpoint(String playerName, String mapType, String checkpointName) {
        String path = "players." + playerName + "." + mapType + "." + checkpointName;
        
        if (checkpointsConfig.contains(path)) {
            return path;
        }
        return null;
    }
    
    public boolean removePersonalizedCheckpoint(String playerName, String mapType, String checkpointName) {
        String path = "players." + playerName + "." + mapType + "." + checkpointName;
        
        if (checkpointsConfig.contains(path)) {
            checkpointsConfig.set(path, null);
            saveCheckpoints();
            return true;
        }
        return false;
    }
    
    public Set<String> getPlayerCheckpointsForMap(String playerName, String mapType) {
        Set<String> checkpoints = new HashSet<>();
        String basePath = "players." + playerName + "." + mapType;
        
        if (checkpointsConfig.contains(basePath)) {
            Set<String> keys = checkpointsConfig.getConfigurationSection(basePath).getKeys(false);
            checkpoints.addAll(keys);
        }
        
        return checkpoints;
    }
    
    public boolean hasPersonalizedCheckpointInAnyMap(String playerName, String checkpointName) {
        String playerPath = "players." + playerName;
        
        if (!checkpointsConfig.contains(playerPath)) {
            return false;
        }
        
        for (String mapType : checkpointsConfig.getConfigurationSection(playerPath).getKeys(false)) {
            if (checkpointsConfig.contains(playerPath + "." + mapType + "." + checkpointName)) {
                return true;
            }
        }
        
        return false;
    }
    
    public String findMapForCheckpoint(String playerName, String checkpointName) {
        String playerPath = "players." + playerName;
        
        if (!checkpointsConfig.contains(playerPath)) {
            return null;
        }
        
        for (String mapType : checkpointsConfig.getConfigurationSection(playerPath).getKeys(false)) {
            if (checkpointsConfig.contains(playerPath + "." + mapType + "." + checkpointName)) {
                return mapType;
            }
        }
        
        return null;
    }
    
    public String detectMapByWorld(String worldName) {
        if (worldName.toLowerCase().contains("end") || worldName.toLowerCase().contains("mapa1")) {
            return "end";
        } else if (worldName.toLowerCase().contains("grande") || worldName.toLowerCase().contains("mapa2")) {
            return "grande";
        }
        
        if (plugin.getConfig().contains("worlds.end") && 
            plugin.getConfig().getString("worlds.end").equalsIgnoreCase(worldName)) {
            return "end";
        } else if (plugin.getConfig().contains("worlds.grande") && 
                   plugin.getConfig().getString("worlds.grande").equalsIgnoreCase(worldName)) {
            return "grande";
        }
        
        return null;
    }
    
    public Location getCheckpointLocation(String path) {
        if (!checkpointsConfig.contains(path)) {
            return null;
        }
        
        try {
            String worldName = checkpointsConfig.getString(path + ".world");
            double x = checkpointsConfig.getDouble(path + ".x");
            double y = checkpointsConfig.getDouble(path + ".y");
            double z = checkpointsConfig.getDouble(path + ".z");
            float yaw = (float) checkpointsConfig.getDouble(path + ".yaw");
            float pitch = (float) checkpointsConfig.getDouble(path + ".pitch");
            
            return new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao carregar checkpoint " + path + ": " + e.getMessage());
            return null;
        }
    }
}
