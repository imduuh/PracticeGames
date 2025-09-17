package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class FreezeTimeListener implements Listener {
    
    private final PracticeGames plugin;
    
    public FreezeTimeListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        //plugin.getLogger().info("DEBUG: Player se moveu - " + player.getName());
        if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            double deltaX = Math.abs(to.getX() - from.getX());
            double deltaY = Math.abs(to.getY() - from.getY());
            double deltaZ = Math.abs(to.getZ() - from.getZ());
            
            plugin.getLogger().info("DEBUG: FREEZETIME - Jogador " + player.getName() + " tentou se mover durante contagem!");
            //plugin.getLogger().info("DEBUG: Delta X: " + deltaX + ", Y: " + deltaY + ", Z: " + deltaZ);
            
            if (deltaX > 0.001 || deltaY > 0.001 || deltaZ > 0.001) {
                plugin.getLogger().info("DEBUG: MOVIMENTO CANCELADO para " + player.getName() + " durante contagem regressiva!");
                
                event.setCancelled(true);
                
                player.teleport(from);
                
                //player.sendMessage("§cVocê não pode se mover durante a contagem regressiva!");
                
                return;
            }
        }
    }
}
