package me.tantsz.practicegames.managers;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.models.KangarooRace;
import me.tantsz.practicegames.utils.ItemBuilder;
import me.tantsz.practicegames.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class KangarooManager {

    private final PracticeGames plugin;
    private final Map<UUID, KangarooRace> activeRaces;
    private final Map<UUID, Long> lastBoostTime;
    
    private static final long COOLDOWN_BOOST = 800; // 0.8 segundos

    public KangarooManager(PracticeGames plugin) {
        this.plugin = plugin;
        this.activeRaces = new HashMap<>();
        this.lastBoostTime = new HashMap<>();
    }

    public void startRace(Player player) {
        UUID playerId = player.getUniqueId();
        
        plugin.getLogger().info("DEBUG: Iniciando corrida Kangaroo para " + player.getName());
        
        if (activeRaces.containsKey(playerId)) {
            cancelRace(player);
        }
        
        KangarooRace race = new KangarooRace(player, 2);
        activeRaces.put(playerId, race);
        lastBoostTime.put(playerId, 0L);
        
        setupPlayer(player);
        plugin.getGameManager().addPlayerToRace(player);
        
        startTimer(race);
        updateScoreboard(player);
        
        player.sendMessage("§aCorrida Kangaroo iniciada! Use o firework para dar boosts!");
        player.sendMessage("§eVocê tem §c2 boosts §edisponíveis!");
        
        plugin.getLogger().info("DEBUG: Kangaroo iniciado para " + player.getName() + " com 2 boosts");
    }

    private void setupPlayer(Player player) {
        player.getInventory().clear();
        
        player.getInventory().setItem(0, new ItemBuilder(Material.FIREWORK)
            .setDisplayName("§aKangaroo")
            .setLore("§7Clique direito para dar um boost!", "§7Boosts restantes: §c2")
            .build());
        
        player.getInventory().setItem(4, new ItemBuilder(Material.WATCH)
            .setDisplayName("§eREINICIAR CORRIDA")
            .setLore("§7Clique para reiniciar", "§7a corrida Kangaroo.")
            .build());
        
        player.getInventory().setItem(8, new ItemBuilder(Material.BED)
            .setDisplayName("§cCANCELAR CORRIDA")
            .setLore("§7Clique para cancelar", "§7a corrida Kangaroo.")
            .build());
        
        player.updateInventory();
        
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }

    public boolean useBoost(Player player) {
        UUID playerId = player.getUniqueId();
        KangarooRace race = activeRaces.get(playerId);
        
        if (race == null) {
            return false;
        }
        
        long agora = System.currentTimeMillis();
        long ultimoUso = lastBoostTime.getOrDefault(playerId, 0L);
        
        plugin.getLogger().info("DEBUG: Tentativa de boost - Player: " + player.getName() + ", Boosts restantes: " + race.getBoostsRemaining());
        
        if (race.getBoostsRemaining() <= 0) {
            player.sendMessage("§cVocê não tem mais boosts disponíveis!");
            return false;
        }
        
        if (agora - ultimoUso < COOLDOWN_BOOST) {
            return false;
        }
        
        if (!isProximoDoChao(player)) {
            player.sendMessage("§cVocê precisa estar próximo do chão para usar o boost!");
            return false;
        }
        
        org.bukkit.util.Vector direction = player.getLocation().getDirection();
        Location playerLoc = player.getLocation();
        
        int alturaObstaculo = verificarAlturaObstaculo(playerLoc, direction);
        
        plugin.getLogger().info("DEBUG: Altura do obstáculo detectada: " + alturaObstaculo + " blocos para " + player.getName());
        
        double alturaBoost;
        double velocidadeHorizontal;
        String mensagem;
        
        if (alturaObstaculo >= 3) {
            alturaBoost = 2.2;
            velocidadeHorizontal = 2.8;
            mensagem = "§eBoost SUPER usado para obstáculo alto!";
        } else if (alturaObstaculo >= 2) {
            alturaBoost = 1.8;
            velocidadeHorizontal = 2.5;
            mensagem = "§eBoost FORTE usado para obstáculo médio!";
        } else if (alturaObstaculo >= 1) {
            alturaBoost = 1.4;
            velocidadeHorizontal = 2.2;
            mensagem = "§eBoost usado para obstáculo baixo!";
        } else {
            alturaBoost = 1.0;
            velocidadeHorizontal = 2.0;
            mensagem = "§eBoost normal usado!";
        }
        
        direction.setY(0);
        direction = direction.normalize();
        direction = direction.multiply(velocidadeHorizontal);
        direction.setY(alturaBoost);
        
        if (alturaObstaculo >= 2) {
            direction.setY(direction.getY() + 0.4);
        }
        
        player.setVelocity(direction);
        
        race.useBoost();
        lastBoostTime.put(playerId, agora);
        
        updateFireworkInventory(player);
        updateScoreboard(player);
        
        player.sendMessage(mensagem);
        if (race.getBoostsRemaining() > 0) {
            player.sendMessage("§aBoosts restantes: §c" + race.getBoostsRemaining());
        } else {
            player.sendMessage("§cNenhum boost restante!");
        }
        
        plugin.getLogger().info("DEBUG: Boost usado por " + player.getName() + ". Altura: " + alturaBoost + ", Velocidade: " + velocidadeHorizontal + ", Boosts restantes: " + race.getBoostsRemaining());
        
        return true;
    }

    private int verificarAlturaObstaculo(Location playerLoc, org.bukkit.util.Vector direction) {
        for (int distancia = 1; distancia <= 2; distancia++) {
            Location checkLoc = playerLoc.clone().add(direction.clone().multiply(distancia));
            
            int altura = 0;
            for (int y = 0; y <= 5; y++) {
                Location blockLoc = checkLoc.clone().add(0, y, 0);
                if (blockLoc.getBlock().getType().isSolid()) {
                    altura = y + 1;
                } else {
                    break;
                }
            }
            
            if (altura > 0) {
                return altura;
            }
        }
        
        return 0;
    }

    private boolean isProximoDoChao(Player player) {
        Location loc = player.getLocation();
        
        for (int y = -1; y <= 3; y++) {
            Location checkLoc = loc.clone().subtract(0, y, 0);
            if (checkLoc.getBlock().getType().isSolid()) {
                return true;
            }
        }
        
        return false;
    }

    private void updateFireworkInventory(Player player) {
        UUID playerId = player.getUniqueId();
        KangarooRace race = activeRaces.get(playerId);
        
        if (race != null) {
            player.getInventory().setItem(0, new ItemBuilder(Material.FIREWORK)
                .setDisplayName("§aKangaroo")
                .setLore("§7Clique direito para dar um boost!", "§7Boosts restantes: §c" + race.getBoostsRemaining())
                .build());
            player.updateInventory();
        }
    }

    public void processCheckpoint(Player player, String checkpointName) {
        UUID playerId = player.getUniqueId();
        KangarooRace race = activeRaces.get(playerId);
        
        if (race == null) {
            plugin.getLogger().warning("DEBUG: Tentativa de processar checkpoint para jogador não em corrida: " + player.getName());
            return;
        }
        
        if (race.hasPassedCheckpoint(checkpointName)) {
            plugin.getLogger().info("DEBUG: Checkpoint " + checkpointName + " já foi passado por " + player.getName());
            return;
        }
        
        long tempoAtual = System.currentTimeMillis() - race.getStartTime();
        race.addCheckpoint(checkpointName, tempoAtual);
        
        String nomeFormatado = getNomeCheckpointFormatado(checkpointName);
        double segundosCumulativo = tempoAtual / 1000.0;
        double tempoIndividual = race.getIndividualTime(checkpointName);
        
        player.sendMessage("§e" + nomeFormatado + " §7» §f" + String.format("%.3f", segundosCumulativo) + "s §7(" + String.format("%.3f", tempoIndividual) + "s)");
        
        updateScoreboard(player);
        
        salvarTempoParcial(player, checkpointName, tempoIndividual);
        
        plugin.getLogger().info("DEBUG: Checkpoint Kangaroo " + checkpointName + " registrado para " + player.getName() + " em " + segundosCumulativo + "s (individual: " + tempoIndividual + "s)");
    }

    private void salvarTempoParcial(Player player, String checkpointName, double tempoIndividual) {
        try {
            if (tempoIndividual <= 0) {
                plugin.getLogger().warning("DEBUG: Tempo individual inválido para " + checkpointName + ": " + tempoIndividual);
                return;
            }
            
            Map<String, Double> temposIndividuais = new HashMap<>();
            temposIndividuais.put(checkpointName, tempoIndividual);
            
            plugin.getLogger().info("DEBUG: Salvando tempo parcial Kangaroo - " + checkpointName + ": " + tempoIndividual + "s");
            
            plugin.getDatabaseManager().saveKangarooPartialTimes(player.getName(), temposIndividuais);
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: Erro ao salvar tempo parcial Kangaroo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void finishRace(Player player) {
        UUID playerId = player.getUniqueId();
        KangarooRace race = activeRaces.get(playerId);
        
        if (race == null) {
            plugin.getLogger().warning("DEBUG: Tentativa de finalizar corrida Kangaroo para " + player.getName() + " mas jogador não está em corrida!");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Finalizando corrida Kangaroo para " + player.getName());
        
        long tempoTotal = System.currentTimeMillis() - race.getStartTime();
        double segundos = tempoTotal / 1000.0;
        
        race.stopTimer();
        
        cleanupPlayer(player);
        plugin.getGameManager().removePlayerFromRace(player);
        
        Bukkit.broadcastMessage("§6" + player.getName() + " §fcompletou o §eKangaroo §fem §e" + String.format("%.3f", segundos) + "s§f!");
        
        showRaceResults(player, race, segundos);
        
        saveRaceResults(player, race, segundos);
        
        plugin.getDatabaseManager().incrementKangarooGameCount(player.getName());
        
        teleportToExit(player);
        
        activeRaces.remove(playerId);
        lastBoostTime.remove(playerId);
        
        plugin.getLogger().info("DEBUG: Corrida Kangaroo finalizada com sucesso para " + player.getName() + " em " + segundos + "s");
    }

    private void showRaceResults(Player player, KangarooRace race, double totalSeconds) {
        player.sendMessage("§fSeu resultado no §eKangaroo§f:");
        player.sendMessage("§bTempo total: §f" + String.format("%.3f", totalSeconds) + "s");
        
        Map<String, Long> checkpoints = race.getCheckpoints();
        if (!checkpoints.isEmpty()) {
            player.sendMessage("§eCheckpoints:");
            
            int index = 1;
            for (Map.Entry<String, Long> entry : checkpoints.entrySet()) {
                double checkpointTime = entry.getValue() / 1000.0;
                double individualTime = race.getIndividualTime(entry.getKey());
                String nomeFormatado = getNomeCheckpointFormatado(entry.getKey());
                
                if (checkpointTime > 0) {
                    player.sendMessage("§f" + index + ". " + nomeFormatado + " §7» §f" + 
                        String.format("%.3f", checkpointTime) + "s §7(" + 
                        String.format("%.3f", individualTime) + "s)");
                    index++;
                }
            }
        }
    }

    private void saveRaceResults(Player player, KangarooRace race, double totalSeconds) {
        try {
            plugin.getLogger().info("DEBUG: INICIANDO salvamento de tempos Kangaroo para " + player.getName());
            plugin.getLogger().info("DEBUG: Tempo total: " + totalSeconds + "s");
            
            Map<String, Double> temposIndividuais = new HashMap<>();
            
            Map<String, Long> checkpoints = race.getCheckpoints();
            if (!checkpoints.isEmpty()) {
                plugin.getLogger().info("DEBUG: Processando " + checkpoints.size() + " checkpoints...");
                
                for (Map.Entry<String, Long> entry : checkpoints.entrySet()) {
                    double tempoIndividual = race.getIndividualTime(entry.getKey());
                    
                    if (tempoIndividual > 0) {
                        temposIndividuais.put(entry.getKey(), tempoIndividual);
                        plugin.getLogger().info("DEBUG: Checkpoint " + entry.getKey() + 
                            " - Tempo INDIVIDUAL: " + tempoIndividual + "s");
                    } else {
                        plugin.getLogger().warning("DEBUG: Tempo individual inválido para " + entry.getKey() + ": " + tempoIndividual);
                    }
                }
            } else {
                plugin.getLogger().warning("DEBUG: Nenhum checkpoint para salvar!");
            }
            
            plugin.getLogger().info("DEBUG: Total de checkpoints processados: " + temposIndividuais.size());
            
            plugin.getDatabaseManager().saveKangarooTimes(player.getName(), totalSeconds, temposIndividuais);
            
            player.sendMessage("§aSeus tempos foram salvos no banco de dados!");
            plugin.getLogger().info("DEBUG: Tempos Kangaroo salvos com SUCESSO no banco para " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: EXCEÇÃO ao salvar tempos Kangaroo: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cErro ao salvar tempos: " + e.getMessage());
        }
    }

    public void restartRace(Player player) {
    UUID playerId = player.getUniqueId();
    
    if (!activeRaces.containsKey(playerId)) {
        player.sendMessage("§cVocê não está em uma corrida Kangaroo!");
        return;
    }
    
    if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
        player.sendMessage("§cVocê já está em uma contagem regressiva! Aguarde...");
        return;
    }
    
    plugin.getLogger().info("DEBUG: Reiniciando corrida Kangaroo para " + player.getName());
    
    KangarooRace currentRace = activeRaces.get(playerId);
    if (currentRace != null) {
        currentRace.stopTimer();
    }
    activeRaces.remove(playerId);
    lastBoostTime.remove(playerId);
    
    player.sendMessage("§eReiniciando corrida Kangaroo...");
    
    boolean teleportSuccess = plugin.getTeleportUtil().teleport("kangaroo", player);
    
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
                        startRace(player);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
    } else {
        player.sendMessage("§cErro: Spawn do Kangaroo não configurado. Use /minigame set kangaroo spawn");
        plugin.getLogger().warning("DEBUG: Spawn do Kangaroo não configurado para reiniciar corrida");
    }
}

    public void cancelRace(Player player) {
        UUID playerId = player.getUniqueId();
        KangarooRace race = activeRaces.get(playerId);
        
        if (race == null) {
            player.sendMessage("§cVocê não está em uma corrida Kangaroo!");
            return;
        }
        
        race.stopTimer();
        cleanupPlayer(player);
        plugin.getGameManager().removePlayerFromRace(player);
        teleportToExit(player);
        
        activeRaces.remove(playerId);
        lastBoostTime.remove(playerId);
        
        player.sendMessage("§eSua corrida Kangaroo foi cancelada.");
        
        plugin.getLogger().info("DEBUG: Corrida Kangaroo cancelada para " + player.getName());
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
        boolean teleportSuccess = plugin.getTeleportUtil().teleport("kangaroo.saida", player);
        
        if (teleportSuccess) {
            player.sendMessage("§aTeleportado para a saída do Kangaroo!");
        } else {
            plugin.getLogger().warning("DEBUG: Saída do Kangaroo não configurada. Use /minigame set kangaroo saida");
        }
    }

    private void startTimer(KangarooRace race) {
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = race.getPlayer();
                if (player == null || !player.isOnline() || !activeRaces.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }
                
                long tempoAtual = System.currentTimeMillis() - race.getStartTime();
                double segundos = tempoAtual / 1000.0;
                
                int segundosInteiros = (int) segundos;
                float fracao = (float) (segundos - segundosInteiros);
                
                player.setLevel(segundosInteiros);
                player.setExp(fracao);
            }
        };
        
        timer.runTaskTimer(plugin, 0L, 2L);
        race.setTimer(timer);
    }

    private void updateScoreboard(Player player) {
        UUID playerId = player.getUniqueId();
        KangarooRace race = activeRaces.get(playerId);
        
        if (race == null) return;
        
        ScoreboardUtil.createKangarooScoreboard(player, race);
    }

    private String getNomeCheckpointFormatado(String checkpoint) {
        switch (checkpoint) {
            case "CHECKPOINT1": return "§8Checkpoint 1";
            case "CHECKPOINT2": return "§7Checkpoint 2";
            case "CHECKPOINT3": return "§6Checkpoint 3";
            case "CHECKPOINT4": return "§bCheckpoint 4";
            default: return "§fCheckpoint";
        }
    }

    public boolean isPlayerInRace(Player player) {
        return activeRaces.containsKey(player.getUniqueId());
    }

    public boolean hasPassedCheckpoint(Player player, String checkpointName) {
        KangarooRace race = activeRaces.get(player.getUniqueId());
        return race != null && race.hasPassedCheckpoint(checkpointName);
    }

    public void cleanupDisconnectedPlayer(UUID playerId) {
        KangarooRace race = activeRaces.remove(playerId);
        if (race != null) {
            race.stopTimer();
            plugin.getLogger().info("DEBUG: Dados Kangaroo limpos para jogador desconectado: " + playerId);
        }
        lastBoostTime.remove(playerId);
    }
}
