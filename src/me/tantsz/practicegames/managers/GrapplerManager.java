package me.tantsz.practicegames.managers;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.models.GrapplerRace;
import me.tantsz.practicegames.utils.ItemBuilder;
import me.tantsz.practicegames.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GrapplerManager {
    
    private final PracticeGames plugin;
    private final Map<UUID, GrapplerRace> activeRaces;
    
    public GrapplerManager(PracticeGames plugin) {
        this.plugin = plugin;
        this.activeRaces = new HashMap<>();
    }
    
    public void startRace(Player player, int spawnNumber) {
        UUID playerId = player.getUniqueId();
        
        plugin.getLogger().info("DEBUG: Iniciando corrida Grappler para " + player.getName() + " no spawn " + spawnNumber);

        if (activeRaces.containsKey(playerId)) {
            cancelRace(player);
        }
        
        GrapplerRace race = new GrapplerRace(player, spawnNumber);
        activeRaces.put(playerId, race);
        
        setupPlayer(player);
        plugin.getGameManager().addPlayerToRace(player);
        
        startTimer(race);
        updateScoreboard(player);
        
        player.sendMessage("§aCorrida Grappler iniciada no spawn " + spawnNumber + "!");
        player.sendMessage("§eUse o §aGrappler §epara se mover rapidamente!");
        player.sendMessage("§7Botão esquerdo: Lançar | Botão direito: Puxar");
        
        plugin.getLogger().info("DEBUG: Grappler iniciado para " + player.getName());
    }
    
    private void setupPlayer(Player player) {
        player.getInventory().clear();
        
        player.getInventory().setItem(0, new ItemBuilder(Material.LEASH)
            .setDisplayName("§aGrappler")
            .setLore("§7Botão esquerdo: Lançar grappler", 
                    "§7Botão direito: Puxar até o bloco",
                    "§7Use para se mover rapidamente!")
            .build());
        
        player.getInventory().setItem(4, new ItemBuilder(Material.WATCH)
            .setDisplayName("§eREINICIAR CORRIDA")
            .setLore("§7Clique para reiniciar", "§7a corrida Grappler.")
            .build());
        
        player.getInventory().setItem(8, new ItemBuilder(Material.BED)
            .setDisplayName("§cCANCELAR CORRIDA")
            .setLore("§7Clique para cancelar", "§7a corrida Grappler.")
            .build());
        
        player.updateInventory();
        
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }
    
    public void finishRace(Player player) {
        UUID playerId = player.getUniqueId();
        GrapplerRace race = activeRaces.get(playerId);
        
        if (race == null) {
            plugin.getLogger().warning("DEBUG: Tentativa de finalizar corrida Grappler para jogador não em corrida: " + player.getName());
            return;
        }
        
        plugin.getLogger().info("DEBUG: Finalizando corrida Grappler para " + player.getName());
        
        long totalTime = System.currentTimeMillis() - race.getStartTime();
        double totalSeconds = totalTime / 1000.0;
        
        race.stopTimer();
        cleanupPlayer(player);
        plugin.getGameManager().removePlayerFromRace(player);
        Bukkit.broadcastMessage("§6" + player.getName() + " §fcompletou o §aGrappler §fem §e" + 
            String.format("%.3f", totalSeconds) + "s§f! (Spawn " + race.getSpawnNumber() + ")");
        showRaceResults(player, race, totalSeconds);
        saveRaceResults(player, race, totalSeconds);
        plugin.getDatabaseManager().incrementGrapplerGameCount(player.getName());
        teleportToExit(player);
        activeRaces.remove(playerId);
       
        plugin.getLogger().info("DEBUG: Corrida Grappler finalizada com sucesso para " + player.getName() + " em " + totalSeconds + "s");
    }
    
    private void showRaceResults(Player player, GrapplerRace race, double totalSeconds) {
        player.sendMessage("§fSeu resultado no §aGrappler§f:");
        player.sendMessage("§bTempo total: §f" + String.format("%.3f", totalSeconds) + "s");
        player.sendMessage("§7Spawn usado: §e" + race.getSpawnNumber());
        player.sendMessage("§7Parabéns por completar a corrida!");
    }
    
    private void saveRaceResults(Player player, GrapplerRace race, double totalSeconds) {
        try {
            plugin.getLogger().info("DEBUG: Salvando resultados Grappler para " + player.getName());
            
           
            Map<String, Double> emptyCheckpoints = new HashMap<>();
            
            plugin.getDatabaseManager().saveGrapplerTimes(player.getName(), totalSeconds, emptyCheckpoints);
            
            player.sendMessage("§aSeu tempo foi salvo no banco de dados!");
            plugin.getLogger().info("DEBUG: Tempos Grappler salvos com SUCESSO no banco para " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: EXCEÇÃO ao salvar tempos Grappler: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cErro ao salvar tempo: " + e.getMessage());
        }
    }
    
    public void restartRace(Player player) {
        UUID playerId = player.getUniqueId();
        GrapplerRace currentRace = activeRaces.get(playerId);
        
        if (currentRace == null) {
            player.sendMessage("§cVocê não está em uma corrida Grappler!");
            return;
        }
        
        if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
            player.sendMessage("§cVocê já está em uma contagem regressiva! Aguarde...");
            return;
        }
        
        int spawnNumber = currentRace.getSpawnNumber();
        
        plugin.getLogger().info("DEBUG: Reiniciando corrida Grappler para " + player.getName() + " no spawn " + spawnNumber);
        
        currentRace.stopTimer();
        activeRaces.remove(playerId);
        
        player.sendMessage("§eReiniciando corrida Grappler...");
        
        boolean teleportSuccess = plugin.getTeleportUtil().teleport("grappler." + spawnNumber, player);
        
        if (teleportSuccess) {
            plugin.getGameManager().addPlayerToCountdown(playerId);
            
            new BukkitRunnable() {
                int contador = 3;
                
                @Override
                public void run() {
                    if (!player.isOnline() || !plugin.getGameManager().isPlayerInCountdown(playerId)) {
                        cancel();
                        plugin.getGameManager().removePlayerFromCountdown(playerId);
                        return;
                    }
                    
                    if (contador > 0) {
                        player.sendMessage("§e" + contador + "...");
                        contador--;
                    } else {
                        plugin.getGameManager().removePlayerFromCountdown(playerId);
                        player.sendMessage("§aVAI!");
                        
                        if (player.isOnline()) {
                            startRace(player, spawnNumber);
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        
        } else {
            player.sendMessage("§cErro: Spawn " + spawnNumber + " do Grappler não configurado. Use /minigame set grappler " + spawnNumber);
            plugin.getLogger().warning("DEBUG: Spawn " + spawnNumber + " do Grappler não configurado para reiniciar corrida");
        }
    }
    
    public void cancelRace(Player player) {
        UUID playerId = player.getUniqueId();
        GrapplerRace race = activeRaces.get(playerId);
        
        if (race == null) {
            player.sendMessage("§cVocê não está em uma corrida Grappler!");
            return;
        }
        
        race.stopTimer();
        cleanupPlayer(player);
        plugin.getGameManager().removePlayerFromRace(player);
        
        teleportToExit(player);
        
        activeRaces.remove(playerId);
        
        player.sendMessage("§eSua corrida Grappler foi cancelada.");
        
        plugin.getLogger().info("DEBUG: Corrida Grappler cancelada para " + player.getName());
    }
    
    private void cleanupPlayer(Player player) {
        player.setLevel(0);
        player.setExp(0.0f);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.getInventory().clear();
        player.updateInventory();
        
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }
    
    private void teleportToExit(Player player) {
        boolean teleportSuccess = plugin.getTeleportUtil().teleport("grappler.saida", player);
        
        if (teleportSuccess) {
            player.sendMessage("§aTeleportado para a saída do Grappler!");
        } else {
            plugin.getLogger().warning("DEBUG: Saída do Grappler não configurada. Use /minigame set grappler saida");
        }
    }
    
    private void startTimer(GrapplerRace race) {
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = race.getPlayer();
                if (player == null || !player.isOnline() || !activeRaces.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }
                
                long currentTime = System.currentTimeMillis() - race.getStartTime();
                double seconds = currentTime / 1000.0;
                
                int wholeSeconds = (int) seconds;
                float fraction = (float) (seconds - wholeSeconds);
                
                player.setLevel(wholeSeconds);
                player.setExp(fraction);
            }
        };
        
        timer.runTaskTimer(plugin, 0L, 2L);
        race.setTimer(timer);
    }
    
    private void updateScoreboard(Player player) {
        UUID playerId = player.getUniqueId();
        GrapplerRace race = activeRaces.get(playerId);
        
        if (race == null) return;
        
        ScoreboardUtil.createGrapplerScoreboard(player, race);
    }
    
    public boolean isPlayerInRace(Player player) {
        return activeRaces.containsKey(player.getUniqueId());
    }
    
    public void cleanupDisconnectedPlayer(UUID playerId) {
        GrapplerRace race = activeRaces.remove(playerId);
        if (race != null) {
            race.stopTimer();
            plugin.getLogger().info("DEBUG: Dados Grappler limpos para jogador desconectado: " + playerId);
        }
    }

    public void checkForExit(Player player, Location location) {
        if (!isPlayerInRace(player)) {
            return;
        }
        
        if (isNearDiamondBlock(location)) {
            plugin.getLogger().info("DEBUG: Checkpoint Grappler detectado para " + player.getName());
            finishRace(player);
        }
    }

    private boolean isNearDiamondBlock(Location playerLocation) {
        for (int y = 0; y <= 7; y++) {
            Location checkLocation = playerLocation.clone().subtract(0, y, 0);
            
            if (checkLocation.getBlock().getType() == Material.DIAMOND_BLOCK) {
                plugin.getLogger().info("DEBUG: Bloco de diamante encontrado " + y + " blocos abaixo da posição do jogador");
                return true;
            }
        }
        
        return false;
    }
}
