package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GameProtectionListener implements Listener {
    
    private final PracticeGames plugin;
    
    public GameProtectionListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getRaceManager().isPlayerInRace(player) || 
            plugin.getKangarooManager().isPlayerInRace(player) ||
            plugin.getGrapplerManager().isPlayerInRace(player)) {
            if (!player.hasPermission("practice.admin")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getRaceManager().isPlayerInRace(player) || 
            plugin.getKangarooManager().isPlayerInRace(player) ||
            plugin.getGrapplerManager().isPlayerInRace(player)) {
            if (!player.hasPermission("practice.admin")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getRaceManager().isPlayerInRace(player) || 
            plugin.getKangarooManager().isPlayerInRace(player) ||
            plugin.getGrapplerManager().isPlayerInRace(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getRaceManager().isPlayerInRace(player) || 
            plugin.getKangarooManager().isPlayerInRace(player) ||
            plugin.getGrapplerManager().isPlayerInRace(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
    
            if (plugin.getKangarooManager().isPlayerInRace(player)) {
                if (event.getCause() == EntityDamageEvent.DamageCause.LAVA || 
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    return;
                }
        
                event.setCancelled(true);
            }
            
            if (plugin.getGrapplerManager().isPlayerInRace(player)) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                }
            }
    
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        plugin.getLogger().info("DEBUG: Jogador " + player.getName() + " respawnando");
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.setMaxHealth(20.0);
                    player.setHealth(20.0);
                    player.setFoodLevel(20);
                    player.setSaturation(20.0f);

                    player.getInventory().clear();
                    player.updateInventory();
                    
                    plugin.getLogger().info("DEBUG: Jogador " + player.getName() + " limpo após respawn");
                }
            }
        }.runTaskLater(plugin, 5L);
    }
}
