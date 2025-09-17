package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class CheckpointDetectionListener implements Listener {
    
    private final PracticeGames plugin;
    
    public CheckpointDetectionListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = event.getTo();
        
        if (playerLocation == null) {
            return;
        }
        
        if (plugin.getRaceManager().isPlayerInRace(player)) {
            checkForRaceCheckpoints(player, playerLocation);
        }
        
        if (plugin.getKangarooManager().isPlayerInRace(player)) {
            checkForKangarooCheckpoints(player, playerLocation);
        }
        
        if (plugin.getGrapplerManager().isPlayerInRace(player)) {
            checkForGrapplerCheckpoint(player, playerLocation);
        }
    }
    
    private void checkForRaceCheckpoints(Player player, Location playerLocation) {
        String currentMap = plugin.getRaceManager().getCurrentMap(player);
        if (currentMap == null) {
            return;
        }
        
        for (int y = 0; y <= 15; y++) {
            Location checkLocation = playerLocation.clone().subtract(0, y, 0);
            Material blockType = checkLocation.getBlock().getType();
            
            if (isValidRaceCheckpointMaterial(blockType, currentMap)) {
                plugin.getLogger().info("DEBUG: Checkpoint de corrida detectado " + y + " blocos abaixo - Material: " + blockType + " para " + player.getName());
                plugin.getRaceManager().processCheckpoint(player, blockType, checkLocation.getBlock());
                return;
            }
        }
    }
    
    private void checkForKangarooCheckpoints(Player player, Location playerLocation) {
        for (int y = 0; y <= 40; y++) {
            Location checkLocation = playerLocation.clone().subtract(0, y, 0);
            Material blockType = checkLocation.getBlock().getType();
            
            if (blockType == Material.DIAMOND_BLOCK) {
                plugin.getLogger().info("DEBUG: Checkpoint Kangaroo detectado " + y + " blocos abaixo para " + player.getName());
                
                String checkpointName = determineKangarooCheckpoint(checkLocation);
                if (checkpointName != null && !plugin.getKangarooManager().hasPassedCheckpoint(player, checkpointName)) {
                    plugin.getKangarooManager().processCheckpoint(player, checkpointName);
                }
                return;
            }
        }
    }
    
    private void checkForGrapplerCheckpoint(Player player, Location playerLocation) {
        for (int y = 0; y <= 10; y++) {
            Location checkLocation = playerLocation.clone().subtract(0, y, 0);
            Material blockType = checkLocation.getBlock().getType();
            
            if (blockType == Material.DIAMOND_BLOCK) {
                plugin.getLogger().info("DEBUG: Checkpoint Grappler detectado " + y + " blocos abaixo para " + player.getName());
                plugin.getGrapplerManager().finishRace(player);
                return;
            }
        }
    }
    
    private boolean isValidRaceCheckpointMaterial(Material material, String mapId) {
        if (mapId.equals("mapa1")) {
            return material == Material.COAL_BLOCK || 
                   material == Material.GOLD_BLOCK || 
                   material == Material.EMERALD_BLOCK || 
                   material == Material.IRON_BLOCK || 
                   material == Material.REDSTONE_BLOCK;
        } else if (mapId.equals("mapa2")) {
            return material == Material.WOOL;
        }
        return false;
    }
    
    private String determineKangarooCheckpoint(Location checkpointLocation) {

        int x = checkpointLocation.getBlockX();
        int z = checkpointLocation.getBlockZ();
        
        if (isInRange(x, z, -100, -50, 50, 100)) {
            return "CHECKPOINT1";
        } else if (isInRange(x, z, 50, 100, 150, 200)) {
            return "CHECKPOINT2";
        } else if (isInRange(x, z, 150, 200, 250, 300)) {
            return "CHECKPOINT3";
        } else if (isInRange(x, z, 250, 300, 350, 400)) {
            return "CHECKPOINT4";
        }
        
        plugin.getLogger().warning("DEBUG: Não foi possível determinar checkpoint Kangaroo na posição X:" + x + " Z:" + z);
        return null;
    }
    
    private boolean isInRange(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
