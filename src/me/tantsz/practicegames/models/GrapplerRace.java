package me.tantsz.practicegames.models;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GrapplerRace {
    
    private final Player player;
    private final long startTime;
    private final int spawnNumber;
    private BukkitRunnable timer;
    
    public GrapplerRace(Player player, int spawnNumber) {
        this.player = player;
        this.startTime = System.currentTimeMillis();
        this.spawnNumber = spawnNumber;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public int getSpawnNumber() {
        return spawnNumber;
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
}
