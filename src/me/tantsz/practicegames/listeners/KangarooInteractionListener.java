package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class KangarooInteractionListener implements Listener {
    
    private final PracticeGames plugin;
    
    public KangarooInteractionListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() != Material.FIREWORK) {
            return;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        if (displayName.equals("§aKangaroo")) {
            event.setCancelled(true);
            
            if (plugin.getKangarooManager().isPlayerInRace(player)) {
                boolean success = plugin.getKangarooManager().useBoost(player);
                if (!success) {
                }
            } else {
                player.sendMessage("§cVocê não está em uma corrida Kangaroo!");
            }
        }
    }
}
