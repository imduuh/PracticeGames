package me.tantsz.practicegames.managers;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.models.Race;
import me.tantsz.practicegames.utils.ItemBuilder;
import me.tantsz.practicegames.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RaceManager {
    
    private final PracticeGames plugin;
    private final Map<UUID, Race> activeRaces;
    
    private static final List<String> CHECKPOINT_ORDER_OLD = Arrays.asList("END", "DESERTO", "JUNGLE", "FLORESTA", "NETHER");
    private static final List<String> CHECKPOINT_ORDER_NEW = Arrays.asList("FLORESTA", "CAVERNA", "GELO", "DESERTO", "NETHER", "PLANICIE");
    
    public RaceManager(PracticeGames plugin) {
        this.plugin = plugin;
        this.activeRaces = new HashMap<>();
    }
    
    public void startRace(Player player, String mapId, String startCheckpointName) {
        UUID playerId = player.getUniqueId();
        
        plugin.getLogger().info("DEBUG: Iniciando corrida - Jogador: " + player.getName() + ", Mapa: " + mapId + ", Checkpoint: " + startCheckpointName);
        
        if (activeRaces.containsKey(playerId)) {
            cancelRace(player);
        }
        
        Race race = new Race(player, mapId, startCheckpointName);
        activeRaces.put(playerId, race);
        
        setupPlayer(player);
        plugin.getGameManager().addPlayerToRace(player);
        
        if (startCheckpointName != null && !startCheckpointName.isEmpty()) {
            markPreviousCheckpoints(race, startCheckpointName);
        }
        
        startTimer(race);
        updateScoreboard(player);
        
        String raceType = race.isCompleteRace() ? " (Corrida Completa)" : " (Corrida Parcial - " + startCheckpointName + ")";
        player.sendMessage("§aCorrida iniciada no §e" + getMapDisplayName(mapId) + raceType + "§a!");
    }
    
    private void setupPlayer(Player player) {
        player.getInventory().clear();
        
        player.getInventory().setItem(0, new ItemBuilder(Material.WATCH)
            .setDisplayName("§eREINICIAR CORRIDA")
            .setLore("§7Clique para reiniciar", "§7a corrida do início.")
            .build());
        
        player.getInventory().setItem(8, new ItemBuilder(Material.BED)
            .setDisplayName("§cCANCELAR CORRIDA")
            .setLore("§7Clique para cancelar", "§7a corrida atual.")
            .build());
        
        player.updateInventory();
        
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }
    
    private void markPreviousCheckpoints(Race race, String startCheckpointName) {
        List<String> checkpointOrder = getCheckpointOrderForMap(race.getMapId());
        int startIndex = -1;
        
        for (int i = 0; i < checkpointOrder.size(); i++) {
            if (checkpointOrder.get(i).equalsIgnoreCase(startCheckpointName)) {
                startIndex = i;
                break;
            }
        }
        
        if (startIndex > 0) {
            for (int i = 0; i < startIndex; i++) {
                String cpName = checkpointOrder.get(i);
                Material cpMaterial = getMaterialForCheckpointName(cpName, race.getMapId());
                if (cpMaterial != null) {
                    race.markCheckpointAsPassed(cpMaterial, getDataValueForCheckpoint(cpName, race.getMapId()));
                    plugin.getLogger().info("DEBUG: Marcando checkpoint " + cpName + " como passado (anterior ao inicial)");
                }
            }
        }
    }
    
    public void processCheckpoint(Player player, Material material, Block block) {
        UUID playerId = player.getUniqueId();
        Race race = activeRaces.get(playerId);
        
        if (race == null) {
            return;
        }
        
        short dataValue = block.getData();
        String checkpointName = getCheckpointName(material, dataValue, race.getMapId());
        
        if (race.hasPassedCheckpoint(material, dataValue)) {
            return;
        }
        
        long currentTime = System.currentTimeMillis() - race.getStartTime();
        race.addCheckpoint(checkpointName, currentTime, material, dataValue);
        
        double cumulativeSeconds = currentTime / 1000.0;
        double individualTime = race.getIndividualTime(checkpointName);
        
        player.sendMessage("§e" + checkpointName + " §7» §f" + 
            String.format("%.3f", cumulativeSeconds) + "s §7(" + 
            String.format("%.3f", individualTime) + "s)");
        
        updateScoreboard(player);
        
        savePartialTime(player, race, checkpointName, individualTime);
        
        if (isLastCheckpoint(material, dataValue, race.getMapId())) {
            finishRace(player);
        }
    }
    
    private void savePartialTime(Player player, Race race, String checkpointName, double individualTime) {
        try {
            if (individualTime <= 0) {
                return;
            }
            
            String biome = extractBiomeFromName(checkpointName);
            if (!biome.isEmpty()) {
                Map<String, Double> partialTimes = new HashMap<>();
                partialTimes.put(biome, individualTime);
                
                plugin.getDatabaseManager().savePartialRaceTimes(player.getName(), race.getMapId(), partialTimes);
                
                plugin.getLogger().info("DEBUG: Tempo parcial salvo - " + biome + ": " + individualTime + "s");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: Erro ao salvar tempo parcial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void finishRace(Player player) {
        UUID playerId = player.getUniqueId();
        Race race = activeRaces.get(playerId);
        
        if (race == null) {
            return;
        }
        
        plugin.getLogger().info("DEBUG: Finalizando corrida para " + player.getName());
        
        race.stopTimer();
        
        long totalTime = System.currentTimeMillis() - race.getStartTime();
        double totalSeconds = totalTime / 1000.0;
        
        cleanupPlayer(player);
        plugin.getGameManager().removePlayerFromRace(player);
        
        String raceType = race.isCompleteRace() ? " (Corrida Completa)" : " (Corrida Parcial)";
        Bukkit.broadcastMessage("§6" + player.getName() + " §fcompletou a corrida no §e" + 
            getMapDisplayName(race.getMapId()) + " §fem §e" + String.format("%.3f", totalSeconds) + "s§f!" + raceType);
        
        showRaceResults(player, race, totalSeconds);
        
        if (race.isCompleteRace()) {
            saveRaceResults(player, race, totalSeconds);
            plugin.getDatabaseManager().incrementGameCount(player.getName(), race.getMapId());
        }
        
        teleportToExit(player, race.getMapId());
        
        activeRaces.remove(playerId);
    }
    
    public void finishRaceByDeath(Player player) {
        UUID playerId = player.getUniqueId();
        Race race = activeRaces.get(playerId);
        
        if (race == null) {
            return;
        }
        
        plugin.getLogger().info("DEBUG: Corrida finalizada por morte para " + player.getName());
        
        race.stopTimer();
        cleanupPlayer(player);
        plugin.getGameManager().removePlayerFromRace(player);
        
        plugin.getDatabaseManager().incrementGameCount(player.getName(), race.getMapId());
        
        player.sendMessage("§cCORRIDA FINALIZADA!");
        player.sendMessage("§cVocê morreu durante a corrida no " + getMapDisplayName(race.getMapId()) + ".");
        
        teleportToExit(player, race.getMapId());
        activeRaces.remove(playerId);
    }
    
    public void cancelRace(Player player) {
        UUID playerId = player.getUniqueId();
        Race race = activeRaces.get(playerId);
        
        if (race == null) {
            player.sendMessage("§cVocê não está em uma corrida!");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Corrida cancelada para " + player.getName());
        
        race.stopTimer();
        cleanupPlayer(player);
        plugin.getGameManager().removePlayerFromRace(player);
        
        player.sendMessage("§eSua corrida no " + getMapDisplayName(race.getMapId()) + " §efoi cancelada com sucesso.");
        
        teleportToExit(player, race.getMapId());
        activeRaces.remove(playerId);
    }
    
    public void restartRace(Player player) {
        UUID playerId = player.getUniqueId();
        Race race = activeRaces.get(playerId);
        
        if (race == null) {
            player.sendMessage("§cVocê não está em uma corrida!");
            return;
        }
        
        if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
            player.sendMessage("§cVocê já está em uma contagem regressiva! Aguarde...");
            return;
        }
        
        String mapId = race.getMapId();
        String startCheckpoint = race.getStartCheckpoint();
        
        plugin.getLogger().info("DEBUG: Reiniciando corrida para " + player.getName() + " no mapa " + mapId);
        
        race.stopTimer();
        activeRaces.remove(playerId);
        player.sendMessage("§eReiniciando corrida...");
        
        String spawnKey = mapId.equals("mapa1") ? "end" : "grande";
        boolean teleportSuccess = plugin.getTeleportUtil().teleport(spawnKey, player);
        
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
                            startRace(player, mapId, startCheckpoint);
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
            
        } else {
            player.sendMessage("§cErro: Spawn do " + getMapDisplayName(mapId) + " não configurado. Use /minigame set " + spawnKey);
            plugin.getLogger().warning("DEBUG: Spawn " + spawnKey + " não configurado para reiniciar corrida");
        }
    }
    
    private void showRaceResults(Player player, Race race, double totalSeconds) {
        player.sendMessage("§fSeu resultado no mapa §6" + getMapDisplayName(race.getMapId()) + "§f:");
        player.sendMessage("§bTempo total: §f" + String.format("%.3f", totalSeconds) + "s");
        
        Map<String, Long> checkpoints = race.getCheckpoints();
        if (!checkpoints.isEmpty()) {
            player.sendMessage("§eCheckpoints:");
            
            int index = 1;
            for (Map.Entry<String, Long> entry : checkpoints.entrySet()) {
                double checkpointTime = entry.getValue() / 1000.0;
                double individualTime = race.getIndividualTime(entry.getKey());
                
                if (checkpointTime > 0) {
                    player.sendMessage("§f" + index + ". " + entry.getKey() + " §7» §f" + 
                        String.format("%.3f", checkpointTime) + "s §7(" + 
                        String.format("%.3f", individualTime) + "s)");
                    index++;
                }
            }
        }
    }
    
    private void saveRaceResults(Player player, Race race, double totalSeconds) {
        try {
            plugin.getLogger().info("DEBUG: Salvando resultados finais para " + player.getName());
            
            Map<String, Double> checkpointTimes = new HashMap<>();
            for (Map.Entry<String, Long> entry : race.getCheckpoints().entrySet()) {
                double individualTime = race.getIndividualTime(entry.getKey());
                if (individualTime > 0) {
                    String biome = extractBiomeFromName(entry.getKey());
                    if (!biome.isEmpty()) {
                        checkpointTimes.put(biome, individualTime);
                    }
                }
            }
            
            plugin.getDatabaseManager().saveRaceTimes(player.getName(), race.getMapId(), totalSeconds, checkpointTimes);
            
            player.sendMessage("§aSeus tempos foram salvos no banco de dados!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: Erro ao salvar resultados: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cErro ao salvar tempos: " + e.getMessage());
        }
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
    
    private void teleportToExit(Player player, String mapId) {
        String exitKey = (mapId.equals("mapa1") ? "end" : "grande") + ".saida";
        boolean success = plugin.getTeleportUtil().teleport(exitKey, player);
        
        if (success) {
            player.sendMessage("§aTeleportado para a saída!");
        } else {
            plugin.getLogger().warning("DEBUG: Saída não configurada para " + mapId);
        }
    }
    
    private void startTimer(Race race) {
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
        Race race = activeRaces.get(playerId);
        
        if (race == null) return;
        
        ScoreboardUtil.createRaceScoreboard(player, race);
    }
    
    // Métodos utilitários
    public String getMapDisplayName(String mapId) {
        switch (mapId.toLowerCase()) {
            case "mapa1":
                return "§6Mapa End";
            case "mapa2":
                return "§aMapa Grande";
            default:
                return "§fMapa Desconhecido";
        }
    }
    
    public boolean isValidCheckpointName(String name) {
        return CHECKPOINT_ORDER_OLD.contains(name.toUpperCase()) || CHECKPOINT_ORDER_NEW.contains(name.toUpperCase());
    }
    
    private List<String> getCheckpointOrderForMap(String mapId) {
        return mapId.equals("mapa2") ? CHECKPOINT_ORDER_NEW : CHECKPOINT_ORDER_OLD;
    }
    
    private Material getMaterialForCheckpointName(String name, String mapId) {
        if (mapId.equals("mapa1")) {
            switch (name.toUpperCase()) {
                case "END": return Material.COAL_BLOCK;
                case "DESERTO": return Material.GOLD_BLOCK;
                case "JUNGLE": return Material.EMERALD_BLOCK;
                case "FLORESTA": return Material.IRON_BLOCK;
                case "NETHER": return Material.REDSTONE_BLOCK;
                default: return null;
            }
        } else if (mapId.equals("mapa2")) {
            switch (name.toUpperCase()) {
                case "FLORESTA":
                case "CAVERNA":
                case "GELO":
                case "DESERTO":
                case "NETHER":
                case "PLANICIE":
                    return Material.WOOL;
                default: return null;
            }
        }
        return null;
    }
    
    private short getDataValueForCheckpoint(String name, String mapId) {
        if (mapId.equals("mapa2")) {
            switch (name.toUpperCase()) {
                case "FLORESTA": return 5;
                case "CAVERNA": return 15;
                case "GELO": return 11;
                case "DESERTO": return 4;
                case "NETHER": return 14;
                case "PLANICIE": return 13;
                default: return 0;
            }
        }
        return 0;
    }
    
    private String getCheckpointName(Material material, short dataValue, String mapId) {
        if (mapId.equals("mapa1")) {
            switch (material) {
                case COAL_BLOCK: return "§6END";
                case GOLD_BLOCK: return "§eDESERTO";
                case EMERALD_BLOCK: return "§2JUNGLE";
                case IRON_BLOCK: return "§aFLORESTA";
                case REDSTONE_BLOCK: return "§cNETHER";
                default: return "§bDesconhecido";
            }
        } else if (mapId.equals("mapa2") && material == Material.WOOL) {
            switch (dataValue) {
                case 5: return "§aFLORESTA";
                case 15: return "§8CAVERNA";
                case 11: return "§bGELO";
                case 4: return "§eDESERTO";
                case 14: return "§cNETHER";
                case 13: return "§2PLANICIE";
                default: return "§bDesconhecido";
            }
        }
        return "§bDesconhecido";
    }
    
    private boolean isLastCheckpoint(Material material, short dataValue, String mapId) {
        if (mapId.equals("mapa1")) {
            return material == Material.REDSTONE_BLOCK;
        } else if (mapId.equals("mapa2")) {
            return material == Material.WOOL && dataValue == 13;
        }
        return false;
    }
    
    private String extractBiomeFromName(String formattedName) {
        if (formattedName.contains("END")) return "END";
        else if (formattedName.contains("DESERTO")) return "DESERTO";
        else if (formattedName.contains("JUNGLE")) return "JUNGLE";
        else if (formattedName.contains("FLORESTA")) return "FLORESTA";
        else if (formattedName.contains("NETHER")) return "NETHER";
        else if (formattedName.contains("CAVERNA")) return "CAVERNA";
        else if (formattedName.contains("GELO")) return "GELO";
        else if (formattedName.contains("PLANICIE")) return "PLANICIE";
        return "";
    }
    
    public boolean isPlayerInRace(Player player) {
        return activeRaces.containsKey(player.getUniqueId());
    }
    
    public String getCurrentMap(Player player) {
        Race race = activeRaces.get(player.getUniqueId());
        return race != null ? race.getMapId() : null;
    }
    
    public void cleanupDisconnectedPlayer(UUID playerId) {
        Race race = activeRaces.remove(playerId);
        if (race != null) {
            race.stopTimer();
            plugin.getLogger().info("DEBUG: Dados de corrida limpos para jogador desconectado: " + playerId);
        }
    }
}
