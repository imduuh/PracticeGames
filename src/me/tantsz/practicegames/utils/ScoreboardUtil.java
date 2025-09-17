package me.tantsz.practicegames.utils;

import me.tantsz.practicegames.models.GrapplerRace;
import me.tantsz.practicegames.models.KangarooRace;
import me.tantsz.practicegames.models.Race;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ScoreboardUtil {
    
    public static void createRaceScoreboard(Player player, Race race) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        
        Objective obj = board.registerNewObjective("race", "dummy");
        obj.setDisplayName("§6§lCorrida");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Map<String, Long> checkpoints = race.getCheckpoints();
        
        // Ordenar checkpoints por tempo (ordem cronológica de coleta)
        List<Map.Entry<String, Long>> sortedCheckpoints = new ArrayList<>(checkpoints.entrySet());
        Collections.sort(sortedCheckpoints, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                return Long.compare(o1.getValue(), o2.getValue());
            }
        });
        
        // Começar do score mais alto e ir diminuindo (para aparecer na ordem correta)
        int score = sortedCheckpoints.size();
        
        // Mostrar checkpoints na ordem cronológica que foram coletados
        for (Map.Entry<String, Long> entry : sortedCheckpoints) {
            String checkpointName = entry.getKey();
            long time = entry.getValue();
            double seconds = time / 1000.0;
            
            String displayName = getCheckpointDisplayName(checkpointName);
            obj.getScore(displayName + " §7» §f" + String.format("%.3f", seconds) + "s").setScore(score--);
        }
        
        player.setScoreboard(board);
    }
    
    public static void createKangarooScoreboard(Player player, KangarooRace race) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        
        Objective obj = board.registerNewObjective("kangaroo", "dummy");
        obj.setDisplayName("§e§lKangaroo");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Map<String, Long> checkpoints = race.getCheckpoints();
        
        // Ordenar checkpoints por tempo (ordem cronológica de coleta)
        List<Map.Entry<String, Long>> sortedCheckpoints = new ArrayList<>(checkpoints.entrySet());
        Collections.sort(sortedCheckpoints, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                return Long.compare(o1.getValue(), o2.getValue());
            }
        });
        
        // Começar do score mais alto e ir diminuindo
        int score = sortedCheckpoints.size() + 1;
        
        obj.getScore(" ").setScore(score--);
        
        // Mostrar checkpoints na ordem cronológica que foram coletados
        for (Map.Entry<String, Long> entry : sortedCheckpoints) {
            String checkpointName = entry.getKey();
            long time = entry.getValue();
            double seconds = time / 1000.0;
            
            String displayName = getKangarooCheckpointDisplayName(checkpointName);
            obj.getScore(displayName + " §7» §f" + String.format("%.3f", seconds) + "s").setScore(score--);
        }
        
        player.setScoreboard(board);
    }
    
    public static void createKangarooScoreboardWithoutBoosts(Player player, KangarooRace race) {
        createKangarooScoreboard(player, race);
    }
    
    public static void createGrapplerScoreboard(Player player, GrapplerRace race) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        
        Objective obj = board.registerNewObjective("grappler", "dummy");
        obj.setDisplayName("§a§lGrappler");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        obj.getScore("§7Spawn: §e" + race.getSpawnNumber()).setScore(2);
        obj.getScore(" ").setScore(1);
        
        player.setScoreboard(board);
    }
    
    private static String getCheckpointDisplayName(String checkpointName) {
        switch (checkpointName.toUpperCase()) {
            case "END": return "§6End";
            case "DESERTO": return "§eDeserto";
            case "JUNGLE": return "§2Jungle";
            case "FLORESTA": return "§aFloresta";
            case "NETHER": return "§cNether";
            case "CAVERNA": return "§8Caverna";
            case "GELO": return "§bGelo";
            case "PLANICIE": return "§2Planície";
            default: return "§f" + checkpointName;
        }
    }
    
    private static String getKangarooCheckpointDisplayName(String checkpointName) {
        switch (checkpointName.toUpperCase()) {
            case "CHECKPOINT1": return "§8Checkpoint 1";
            case "CHECKPOINT2": return "§7Checkpoint 2";
            case "CHECKPOINT3": return "§6Checkpoint 3";
            case "CHECKPOINT4": return "§bCheckpoint 4";
            default: return "§fCheckpoint";
        }
    }
}
