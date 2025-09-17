package me.tantsz.practicegames.utils;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportUtil {
    
    private final PracticeGames plugin;
    
    public TeleportUtil(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    public boolean teleport(String locationKey, Player player) {
        Location location = getLocation(locationKey);
        
        if (location == null) {
            player.sendMessage("§cLocalização '" + locationKey + "' não configurada!");
            plugin.getLogger().warning("Tentativa de teleporte para localização não configurada: " + locationKey);
            return false;
        }
        
        try {
            player.teleport(location);
            plugin.getLogger().info("DEBUG: Jogador " + player.getName() + " teleportado para " + locationKey);
            return true;
        } catch (Exception e) {
            player.sendMessage("§cErro ao teleportar: " + e.getMessage());
            plugin.getLogger().severe("Erro ao teleportar " + player.getName() + " para " + locationKey + ": " + e.getMessage());
            return false;
        }
    }
    
    public Location getLocation(String locationKey) {
        // Verificar se é checkpoint personalizado
        if (locationKey.startsWith("players.")) {
            return plugin.getCheckpointManager().getCheckpointLocation(locationKey);
        }
        
        // Verificar configuração padrão
        String configPath = "locations." + locationKey;
        
        if (!plugin.getConfig().contains(configPath)) {
            plugin.getLogger().warning("Localização não encontrada na config: " + locationKey);
            return null;
        }
        
        try {
            String worldName = plugin.getConfig().getString(configPath + ".world");
            double x = plugin.getConfig().getDouble(configPath + ".x");
            double y = plugin.getConfig().getDouble(configPath + ".y");
            double z = plugin.getConfig().getDouble(configPath + ".z");
            float yaw = (float) plugin.getConfig().getDouble(configPath + ".yaw");
            float pitch = (float) plugin.getConfig().getDouble(configPath + ".pitch");
            
            return new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar localização " + locationKey + ": " + e.getMessage());
            return null;
        }
    }
    
    public void setLocation(String locationKey, Location location) {
        String configPath = "locations." + locationKey;
        plugin.getConfig().set(configPath + ".world", location.getWorld().getName());
        plugin.getConfig().set(configPath + ".x", location.getX());
        plugin.getConfig().set(configPath + ".y", location.getY());
        plugin.getConfig().set(configPath + ".z", location.getZ());
        plugin.getConfig().set(configPath + ".yaw", (double) location.getYaw());
        plugin.getConfig().set(configPath + ".pitch", (double) location.getPitch());
        
        plugin.saveConfig();
        
        plugin.getLogger().info("DEBUG: Localização " + locationKey + " definida em " + 
            location.getWorld().getName() + " (" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ")");
    }
}
