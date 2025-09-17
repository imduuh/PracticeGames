package me.tantsz.practicegames.models;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class KangarooRace {
    
    private final Player player;
    private final long startTime;
    private final Map<String, Long> checkpoints;
    private BukkitRunnable timer;
    private int boostsRemaining;
    
    public KangarooRace(Player player, int maxBoosts) {
        this.player = player;
        this.startTime = System.currentTimeMillis();
        this.checkpoints = new HashMap<>();
        this.boostsRemaining = maxBoosts;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public Map<String, Long> getCheckpoints() {
        return new HashMap<>(checkpoints);
    }
    
    public void addCheckpoint(String name, long time) {
        checkpoints.put(name, time);
    }
    
    public boolean hasPassedCheckpoint(String name) {
        return checkpoints.containsKey(name);
    }
    
    public double getIndividualTime(String checkpointName) {
        if (!checkpoints.containsKey(checkpointName)) {
            return 0.0;
        }
        
        long checkpointTime = checkpoints.get(checkpointName);
        
        // Encontrar o checkpoint anterior
        long previousTime = 0L;
        String[] orderedCheckpoints = {"CHECKPOINT1", "CHECKPOINT2", "CHECKPOINT3", "CHECKPOINT4"};
        
        for (int i = 0; i < orderedCheckpoints.length; i++) {
            if (orderedCheckpoints[i].equals(checkpointName)) {
                if (i > 0) {
                    String previousCheckpoint = orderedCheckpoints[i - 1];
                    if (checkpoints.containsKey(previousCheckpoint)) {
                        previousTime = checkpoints.get(previousCheckpoint);
                    }
                }
                break;
            }
        }
        
        return (checkpointTime - previousTime) / 1000.0;
    }
    
    public void setTimer(BukkitRunnable timer) {
        this.timer = timer;
    }
    
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    // Sistema de boosts
    public boolean canUseBoost() {
        return boostsRemaining > 0;
    }
    
    public void useBoost() {
        if (boostsRemaining > 0) {
            boostsRemaining--;
        }
    }
    
    public int getBoostsRemaining() {
        return boostsRemaining;
    }
}
