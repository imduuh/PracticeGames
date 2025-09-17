package me.tantsz.practicegames.database;

import java.util.Map;

public interface Database {
    
    void initialize();
    
    void saveKangarooTimes(String playerName, double totalTime, Map<String, Double> individualTimes);
    
    void saveKangarooPartialTimes(String playerName, Map<String, Double> partialTimes);
    
    void saveRaceTimes(String playerName, String mapId, double totalTime, Map<String, Double> checkpointTimes);
    
    void savePartialRaceTimes(String playerName, String mapId, Map<String, Double> partialTimes);
    
    void saveGrapplerTimes(String playerName, double totalTime, Map<String, Double> individualTimes);
    
    void saveGrapplerPartialTimes(String playerName, Map<String, Double> partialTimes);
    
    void close();
}
