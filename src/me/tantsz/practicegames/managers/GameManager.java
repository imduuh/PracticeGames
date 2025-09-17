package me.tantsz.practicegames.managers;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GameManager implements Listener {
    
    private final PracticeGames plugin;
    private final Set<UUID> playersInRace;
    private final Set<UUID> playersInCountdown;
    private final Set<UUID> playersWithHiddenPlayers;
    
    public GameManager(PracticeGames plugin) {
        this.plugin = plugin;
        this.playersInRace = new HashSet<>();
        this.playersInCountdown = new HashSet<>();
        this.playersWithHiddenPlayers = new HashSet<>();
    }
    
    public void initializeServer() {
        for (World world : Bukkit.getWorlds()) {
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(0);
            world.setSpawnFlags(false, false);
            removeAllAnimals(world);
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getDifficulty() != Difficulty.PEACEFUL) {
                        world.setDifficulty(Difficulty.PEACEFUL);
                    }
                    
                    if (world.hasStorm() || world.isThundering()) {
                        world.setStorm(false);
                        world.setThundering(false);
                        world.setWeatherDuration(0);
                    }
                    
                    removeAllAnimals(world);
                }
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getFoodLevel() < 20) {
                        player.setFoodLevel(20);
                        player.setSaturation(20.0f);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }
    
    private void removeAllAnimals(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Animals || entity instanceof Monster) {
                entity.remove();
            }
        }
    }
    
    public void addPlayerToRace(Player player) {
        UUID id = player.getUniqueId();
        playersInRace.add(id);
        
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        hideOtherPlayers(player);
    }
    
    public void removePlayerFromRace(Player player) {
        UUID id = player.getUniqueId();
        playersInRace.remove(id);
        
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        showOtherPlayers(player);
    }
    
    public boolean isPlayerInRace(UUID playerId) {
        return playersInRace.contains(playerId);
    }
    
    public void addPlayerToCountdown(UUID playerId) {
        playersInCountdown.add(playerId);
        plugin.getLogger().info("DEBUG: Jogador " + playerId + " adicionado à contagem regressiva.");
    }
    
    public void removePlayerFromCountdown(UUID playerId) {
        playersInCountdown.remove(playerId);
        plugin.getLogger().info("DEBUG: Jogador " + playerId + " removido da contagem regressiva.");
    }
    
    public boolean isPlayerInCountdown(UUID playerId) {
        return playersInCountdown.contains(playerId);
    }
    
    private void hideOtherPlayers(Player player) {
        UUID id = player.getUniqueId();
        playersWithHiddenPlayers.add(id);
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                player.hidePlayer(online);
            }
        }
        
        plugin.getLogger().info("DEBUG: Outros jogadores escondidos para " + player.getName());
    }
    
    private void showOtherPlayers(Player player) {
        UUID id = player.getUniqueId();
        playersWithHiddenPlayers.remove(id);
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                player.showPlayer(online);
            }
        }
        
        plugin.getLogger().info("DEBUG: Outros jogadores mostrados novamente para " + player.getName());
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        
        for (UUID playerId : playersWithHiddenPlayers) {
            Player playerWithHidden = Bukkit.getPlayer(playerId);
            if (playerWithHidden != null && playerWithHidden.isOnline()) {
                playerWithHidden.hidePlayer(newPlayer);
            }
        }
        
        if (newPlayer.hasPermission("practice.admin")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(newPlayer)) {
                    newPlayer.showPlayer(online);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        playersInRace.remove(playerId);
        playersInCountdown.remove(playerId);
        playersWithHiddenPlayers.remove(playerId);
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
        }
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isPlayerInRace(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Animals || event.getEntity() instanceof Monster) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID id = player.getUniqueId();
        
        plugin.getLogger().info("DEBUG: Jogador " + player.getName() + " morreu");
        
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        if (plugin.getRaceManager().isPlayerInRace(player)) {
            plugin.getLogger().info("DEBUG: " + player.getName() + " morreu durante corrida normal - finalizando");
            plugin.getRaceManager().finishRaceByDeath(player);
        } else if (plugin.getKangarooManager().isPlayerInRace(player)) {
            plugin.getLogger().info("DEBUG: " + player.getName() + " morreu durante Kangaroo - cancelando");
            plugin.getKangarooManager().cancelRace(player);
        } else {
            plugin.getLogger().info("DEBUG: " + player.getName() + " morreu mas não estava em corrida");
        }
        
        removePlayerFromRace(player);
        playersInCountdown.remove(id);
        
        plugin.getLogger().info("DEBUG: Jogador " + player.getName() + " removido de todos os sistemas após morte");
    }
}
