package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class HotbarInteractionListener implements Listener {
    
    private final PracticeGames plugin;
    
    public HotbarInteractionListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String itemName = item.getItemMeta().getDisplayName();
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (item.getType() == Material.WATCH && itemName.contains("REINICIAR")) {
            event.setCancelled(true);
            
            if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
                player.sendMessage("§cVocê já está em uma contagem regressiva! Aguarde...");
                return;
            }
            
            if (plugin.getRaceManager().isPlayerInRace(player)) {
                plugin.getRaceManager().restartRace(player);
            } else if (plugin.getKangarooManager().isPlayerInRace(player)) {
                plugin.getKangarooManager().restartRace(player);
            } else if (plugin.getGrapplerManager().isPlayerInRace(player)) {
                plugin.getGrapplerManager().restartRace(player);
            }
        }
        
        else if (item.getType() == Material.BED && itemName.contains("CANCELAR")) {
            event.setCancelled(true);
            
            if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
                plugin.getGameManager().removePlayerFromCountdown(playerId);
                player.sendMessage("§cContagem regressiva cancelada.");
                return;
            }
            
            if (plugin.getRaceManager().isPlayerInRace(player)) {
                plugin.getRaceManager().cancelRace(player);
            } else if (plugin.getKangarooManager().isPlayerInRace(player)) {
                plugin.getKangarooManager().cancelRace(player);
            } else if (plugin.getGrapplerManager().isPlayerInRace(player)) {
                plugin.getGrapplerManager().cancelRace(player);
            } else {
                player.sendMessage("§cVocê não está em nenhuma corrida!");
            }
        }
        
        else if (item.getType() == Material.FIREWORK && itemName.contains("Kangaroo")) {
            event.setCancelled(true);
            
            if (plugin.getKangarooManager().isPlayerInRace(player)) {
                plugin.getKangarooManager().useBoost(player);
            }
        }
    }
}
