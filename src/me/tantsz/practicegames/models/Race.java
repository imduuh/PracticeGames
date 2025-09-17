package me.tantsz.practicegames.models;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Race {
    
    private final Player player;
    private final String mapId;
    private final String startCheckpoint;
    private final long startTime;
    private final boolean isCompleteRace;
    
    private final Map<String, Long> checkpoints;
    private final List<String> checkpointOrder;
    private final Set<String> passedCheckpoints;
    private BukkitRunnable timer;
    
    public Race(Player player, String mapId, String startCheckpoint) {
        this.player = player;
        this.mapId = mapId;
        this.startCheckpoint = startCheckpoint;
        this.startTime = System.currentTimeMillis();
        this.isCompleteRace = (startCheckpoint == null || startCheckpoint.isEmpty());
        
        this.checkpoints = new LinkedHashMap<>();
        this.checkpointOrder = new ArrayList<>();
        this.passedCheckpoints = new HashSet<>();
    }
    
    public void addCheckpoint(String name, long time, Material material, short dataValue) {
        if (!checkpoints.containsKey(name)) {
            checkpoints.put(name, time);
            checkpointOrder.add(name);
        }
                String key = material == Material.WOOL ? "WOOL_" + dataValue : material.name();
        passedCheckpoints.add(key);
    }
    
    public void markCheckpointAsPassed(Material material, short dataValue) {
        String key = material == Material.WOOL ? "WOOL_" + dataValue : material.name();
        passedCheckpoints.add(key);
    }
    
    public boolean hasPassedCheckpoint(Material material, short dataValue) {
        String key = material == Material.WOOL ? "WOOL_" + dataValue : material.name();
        return passedCheckpoints.contains(key);
    }
    
    public double getIndividualTime(String checkpointName) {
        if (!checkpoints.containsKey(checkpointName)) {
            return 0.0;
        }
        
        long currentTime = checkpoints.get(checkpointName);
        int currentIndex = checkpointOrder.indexOf(checkpointName);
        
        if (currentIndex <= 0) {
            return currentTime / 1000.0;
        }
        
        for (int i = currentIndex - 1; i >= 0; i--) {
            String previousCheckpoint = checkpointOrder.get(i);
            if (checkpoints.containsKey(previousCheckpoint)) {
                long previousTime = checkpoints.get(previousCheckpoint);
                return (currentTime - previousTime) / 1000.0;
            }
        }
        
        return currentTime / 1000.0;
    }
    
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    public Player getPlayer() { return player; }
    public String getMapId() { return mapId; }
    public String getStartCheckpoint() { return startCheckpoint; }
    public long getStartTime() { return startTime; }
    public boolean isCompleteRace() { return isCompleteRace; }
    public Map<String, Long> getCheckpoints() { return new HashMap<>(checkpoints); }
    public List<String> getCheckpointOrder() { return new ArrayList<>(checkpointOrder); }
    
    public void setTimer(BukkitRunnable timer) {
        this.timer = timer;
    }
}
