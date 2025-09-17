package me.tantsz.practicegames.database;

import me.tantsz.practicegames.PracticeGames;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseService {
    
    private final PracticeGames plugin;
    
    public DatabaseService(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    
    public List<RankingRecord> buscarTop10(String tipo, String mapa) {
        List<RankingRecord> ranking = new ArrayList<>();
        
        try {
            ResultSet rs;
            
            if (mapa.equals("kangaroo")) {
                rs = plugin.getDatabaseManager().getKangarooRanking(tipo, 10);
            } else if (mapa.equals("grappler")) {
                rs = plugin.getDatabaseManager().getGrapplerRanking(10);
            } else {
                rs = plugin.getDatabaseManager().getRaceRanking(mapa, tipo, 10);
            }
            
            if (rs != null) {
                while (rs.next()) {
                    String playerName = rs.getString("player_name");
                    double tempo = rs.getDouble("best_time");
                    ranking.add(new RankingRecord(playerName, tempo));
                }
                rs.close();
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar ranking: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ranking;
    }
    
    public PlayerPosition buscarPosicaoJogador(String playerName, String tipo, String mapa) {
        try {
            ResultSet rs;
            
            if (mapa.equals("kangaroo")) {
                rs = plugin.getDatabaseManager().getKangarooRanking(tipo, 100);
            } else if (mapa.equals("grappler")) {
                rs = plugin.getDatabaseManager().getGrapplerRanking(100);
            } else {
                rs = plugin.getDatabaseManager().getRaceRanking(mapa, tipo, 100);
            }
            
            if (rs != null) {
                int posicao = 1;
                while (rs.next()) {
                    String player = rs.getString("player_name");
                    double tempo = rs.getDouble("best_time");
                    
                    if (player.equals(playerName)) {
                        rs.close();
                        return new PlayerPosition(posicao, tempo);
                    }
                    posicao++;
                }
                rs.close();
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar posição do jogador: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    public PlayerStats buscarPlayerStats(String playerName, String mapa) {
        PlayerStats stats = new PlayerStats();
        
        try {
            if (mapa.equals("kangaroo")) {
                buscarStatsKangaroo(playerName, stats);
            } else if (mapa.equals("grappler")) {
                buscarStatsGrappler(playerName, stats);
            } else {
                buscarStatsRace(playerName, mapa, stats);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao buscar stats do jogador: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    public List<CheckpointRecord> buscarMelhoresTemposPorCheckpoint(String mapa) {
        List<CheckpointRecord> records = new ArrayList<>();
        List<String> checkpoints = getCheckpointsParaMapa(mapa);
        
        for (String checkpoint : checkpoints) {
            List<RankingRecord> ranking = buscarTop10(checkpoint, mapa);
            if (!ranking.isEmpty()) {
                RankingRecord melhor = ranking.get(0);
                if (melhor.getTempo() < 999999.999) {
                    records.add(new CheckpointRecord(checkpoint, melhor.getPlayerName(), melhor.getTempo()));
                }
            }
        }
        
        return records;
    }
    
    
    private void buscarStatsKangaroo(String playerName, PlayerStats stats) {
        // Buscar melhor tempo total
        List<RankingRecord> totalRanking = buscarTop10("TOTAL", "kangaroo");
        for (RankingRecord record : totalRanking) {
            if (record.getPlayerName().equals(playerName)) {
                stats.setMelhorTotal(record.getTempo());
                break;
            }
        }
        
        List<String> checkpoints = getCheckpointsParaMapa("kangaroo");
        for (String checkpoint : checkpoints) {
            List<RankingRecord> checkpointRanking = buscarTop10(checkpoint, "kangaroo");
            for (RankingRecord record : checkpointRanking) {
                if (record.getPlayerName().equals(playerName)) {
                    stats.addCheckpointTime(checkpoint, record.getTempo());
                    break;
                }
            }
        }
        
        int jogos = plugin.getDatabaseManager().getKangarooGameCount(playerName);
        stats.setJogos(jogos);
    }

    private void buscarStatsGrappler(String playerName, PlayerStats stats) {
        List<RankingRecord> totalRanking = buscarTop10("TOTAL", "grappler");
        for (RankingRecord record : totalRanking) {
            if (record.getPlayerName().equals(playerName)) {
                stats.setMelhorTotal(record.getTempo());
                break;
            }
        }

        int jogos = plugin.getDatabaseManager().getGrapplerGameCount(playerName);
        stats.setJogos(jogos);
    }
    
    private void buscarStatsRace(String playerName, String mapa, PlayerStats stats) {
        List<RankingRecord> totalRanking = buscarTop10("TOTAL", mapa);
        for (RankingRecord record : totalRanking) {
            if (record.getPlayerName().equals(playerName)) {
                stats.setMelhorTotal(record.getTempo());
                break;
            }
        }
        
        List<String> checkpoints = getCheckpointsParaMapa(mapa);
        for (String checkpoint : checkpoints) {
            List<RankingRecord> checkpointRanking = buscarTop10(checkpoint, mapa);
            for (RankingRecord record : checkpointRanking) {
                if (record.getPlayerName().equals(playerName)) {
                    stats.addCheckpointTime(checkpoint, record.getTempo());
                    break;
                }
            }
        }
        
        int jogos = plugin.getDatabaseManager().getGameCount(playerName, mapa);
        stats.setJogos(jogos);
    }
    
    private List<String> getCheckpointsParaMapa(String mapa) {
        List<String> checkpoints = new ArrayList<>();
        
        if (mapa.equals("mapa1")) {
            checkpoints.add("END");
            checkpoints.add("DESERTO");
            checkpoints.add("JUNGLE");
            checkpoints.add("FLORESTA");
            checkpoints.add("NETHER");
        } else if (mapa.equals("mapa2")) {
            checkpoints.add("FLORESTA");
            checkpoints.add("CAVERNA");
            checkpoints.add("GELO");
            checkpoints.add("DESERTO");
            checkpoints.add("NETHER");
            checkpoints.add("PLANICIE");
        } else if (mapa.equals("kangaroo")) {
            checkpoints.add("CHECKPOINT1");
            checkpoints.add("CHECKPOINT2");
            checkpoints.add("CHECKPOINT3");
            checkpoints.add("CHECKPOINT4");
        }
        
        return checkpoints;
    }
    
    
    public static class RankingRecord {
        private final String playerName;
        private final double tempo;
        
        public RankingRecord(String playerName, double tempo) {
            this.playerName = playerName;
            this.tempo = tempo;
        }
        
        public String getPlayerName() { return playerName; }
        public double getTempo() { return tempo; }
    }
    
    public static class PlayerPosition {
        private final int posicao;
        private final double tempo;
        
        public PlayerPosition(int posicao, double tempo) {
            this.posicao = posicao;
            this.tempo = tempo;
        }
        
        public int getPosicao() { return posicao; }
        public double getTempo() { return tempo; }
    }
    
    public static class CheckpointRecord {
        private final String checkpoint;
        private final String playerName;
        private final double tempo;
        
        public CheckpointRecord(String checkpoint, String playerName, double tempo) {
            this.checkpoint = checkpoint;
            this.playerName = playerName;
            this.tempo = tempo;
        }
        
        public String getCheckpoint() { return checkpoint; }
        public String getPlayerName() { return playerName; }
        public double getTempo() { return tempo; }
    }
    
    public static class PlayerStats {
        private double melhorTotal = 999999.999;
        private int jogos = 0;
        private final Map<String, Double> checkpointTimes = new HashMap<>();
        
        public double getMelhorTotal() { return melhorTotal; }
        public void setMelhorTotal(double melhorTotal) { 
            if (melhorTotal > 0 && melhorTotal < 999999.999) {
                this.melhorTotal = melhorTotal; 
            }
        }
        
        public int getJogos() { return jogos; }
        public void setJogos(int jogos) { this.jogos = jogos; }
        
        public void addCheckpointTime(String checkpoint, double tempo) {
            if (tempo > 0 && tempo < 999999.999) {
                checkpointTimes.put(checkpoint, tempo);
            }
        }
        
        public double getCheckpointTime(String checkpoint) {
            return checkpointTimes.getOrDefault(checkpoint, 999999.999);
        }
        
        public Map<String, Double> getAllCheckpointTimes() {
            return new HashMap<>(checkpointTimes);
        }
    }
}
