package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.commands.GameCommandExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerConnectionListener implements Listener {
    
    private final PracticeGames plugin;
    
    public PlayerConnectionListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setSaturation(20.0f);
        
        plugin.getLogger().info("DEBUG: Jogador " + event.getPlayer().getName() + " entrou no servidor");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        plugin.getRaceManager().cleanupDisconnectedPlayer(playerId);
        plugin.getKangarooManager().cleanupDisconnectedPlayer(playerId);
        plugin.getGrapplerManager().cleanupDisconnectedPlayer(playerId);
        
        if (plugin.getCommand("minigame").getExecutor() instanceof GameCommandExecutor) {
            ((GameCommandExecutor) plugin.getCommand("minigame").getExecutor()).cleanupPlayer(playerId);
        }
        
        plugin.getLogger().info("DEBUG: Jogador " + event.getPlayer().getName() + " saiu do servidor - dados limpos");
    }
}
