package me.tantsz.practicegames.database.impl;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.database.Database;

import java.sql.*;
import java.util.Map;

public class MySQLDatabase implements Database {
    
    private final PracticeGames plugin;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String connectionUrl;
    
    public MySQLDatabase(PracticeGames plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("MySQL.Host");
        this.port = plugin.getConfig().getInt("MySQL.Port");
        this.database = plugin.getConfig().getString("MySQL.Database");
        this.username = plugin.getConfig().getString("MySQL.Username");
        this.password = plugin.getConfig().getString("MySQL.Password");
        
        this.connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + 
                           "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
    }
    
    @Override
    public void initialize() {
        try {
            try (Connection testConn = DriverManager.getConnection(connectionUrl, username, password)) {
                createTables(testConn);
                plugin.getLogger().info("Conectado ao MySQL com sucesso!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao conectar ao MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl, username, password);
    }
    
    private void createTables(Connection conn) throws SQLException {
        String createRaceTimesTable = "CREATE TABLE IF NOT EXISTS race_times (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL, " +
            "map_id VARCHAR(10) NOT NULL, " +
            "total_time DOUBLE NOT NULL, " +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX idx_player_map (player_name, map_id), " +
            "INDEX idx_map_time (map_id, total_time)" +
            ")";
        
        String createRacePartialTimesTable = "CREATE TABLE IF NOT EXISTS race_partial_times (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL, " +
            "map_id VARCHAR(10) NOT NULL, " +
            "checkpoint VARCHAR(20) NOT NULL, " +
            "time DOUBLE NOT NULL, " +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX idx_player_map_checkpoint (player_name, map_id, checkpoint), " +
            "INDEX idx_map_checkpoint_time (map_id, checkpoint, time)" +
            ")";
        
        String createKangarooTimesTable = "CREATE TABLE IF NOT EXISTS kangaroo_times (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL, " +
            "total_time DOUBLE NOT NULL, " +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX idx_player (player_name), " +
            "INDEX idx_time (total_time)" +
            ")";
        
        String createKangarooPartialTimesTable = "CREATE TABLE IF NOT EXISTS kangaroo_partial_times (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL, " +
            "checkpoint VARCHAR(20) NOT NULL, " +
            "time DOUBLE NOT NULL, " +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX idx_player_checkpoint (player_name, checkpoint), " +
            "INDEX idx_checkpoint_time (checkpoint, time)" +
            ")";

        String createGrapplerTimesTable = "CREATE TABLE IF NOT EXISTS grappler_times (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL, " +
            "total_time DOUBLE NOT NULL, " +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX idx_player (player_name), " +
            "INDEX idx_time (total_time)" +
            ")";
        
        String createGameCountTable = "CREATE TABLE IF NOT EXISTS game_count (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL, " +
            "map_id VARCHAR(10) NOT NULL, " +
            "games_played INT DEFAULT 1, " +
            "UNIQUE KEY unique_player_map (player_name, map_id)" +
            ")";
        
        String createKangarooGameCountTable = "CREATE TABLE IF NOT EXISTS kangaroo_game_count (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL UNIQUE, " +
            "games_played INT DEFAULT 1" +
            ")";
        
        String createGrapplerGameCountTable = "CREATE TABLE IF NOT EXISTS grappler_game_count (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_name VARCHAR(16) NOT NULL UNIQUE, " +
            "games_played INT DEFAULT 1" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createRaceTimesTable);
            stmt.execute(createRacePartialTimesTable);
            stmt.execute(createKangarooTimesTable);
            stmt.execute(createKangarooPartialTimesTable);
            stmt.execute(createGrapplerTimesTable);
            stmt.execute(createGameCountTable);
            stmt.execute(createKangarooGameCountTable);
            stmt.execute(createGrapplerGameCountTable);
            
            plugin.getLogger().info("Tabelas MySQL criadas/verificadas com sucesso!");
        }
    }
    
    @Override
    public void saveKangarooTimes(String playerName, double totalTime, Map<String, Double> individualTimes) {
        try (Connection conn = getConnection()) {
            String insertTotal = "INSERT INTO kangaroo_times (player_name, total_time) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTotal)) {
                stmt.setString(1, playerName);
                stmt.setDouble(2, totalTime);
                stmt.executeUpdate();
            }
            
            if (!individualTimes.isEmpty()) {
                String insertPartial = "INSERT INTO kangaroo_partial_times (player_name, checkpoint, time) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertPartial)) {
                    for (Map.Entry<String, Double> entry : individualTimes.entrySet()) {
                        stmt.setString(1, playerName);
                        stmt.setString(2, entry.getKey());
                        stmt.setDouble(3, entry.getValue());
                        stmt.executeUpdate();
                    }
                }
            }
            
            plugin.getLogger().info("DEBUG: Tempos Kangaroo salvos no MySQL para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos Kangaroo no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveKangarooPartialTimes(String playerName, Map<String, Double> partialTimes) {
        try (Connection conn = getConnection()) {
            String insert = "INSERT INTO kangaroo_partial_times (player_name, checkpoint, time) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                for (Map.Entry<String, Double> entry : partialTimes.entrySet()) {
                    stmt.setString(1, playerName);
                    stmt.setString(2, entry.getKey());
                    stmt.setDouble(3, entry.getValue());
                    stmt.executeUpdate();
                }
            }
            
            plugin.getLogger().info("DEBUG: Tempos parciais Kangaroo salvos no MySQL para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos parciais Kangaroo no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveRaceTimes(String playerName, String mapId, double totalTime, Map<String, Double> checkpointTimes) {
        try (Connection conn = getConnection()) {
            String insertTotal = "INSERT INTO race_times (player_name, map_id, total_time) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTotal)) {
                stmt.setString(1, playerName);
                stmt.setString(2, mapId);
                stmt.setDouble(3, totalTime);
                stmt.executeUpdate();
            }
            
            if (!checkpointTimes.isEmpty()) {
                String insertPartial = "INSERT INTO race_partial_times (player_name, map_id, checkpoint, time) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertPartial)) {
                    for (Map.Entry<String, Double> entry : checkpointTimes.entrySet()) {
                        stmt.setString(1, playerName);
                        stmt.setString(2, mapId);
                        stmt.setString(3, entry.getKey());
                        stmt.setDouble(4, entry.getValue());
                        stmt.executeUpdate();
                    }
                }
            }
            
            plugin.getLogger().info("DEBUG: Tempos de corrida salvos no MySQL para " + playerName + " no mapa " + mapId);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos de corrida no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void savePartialRaceTimes(String playerName, String mapId, Map<String, Double> partialTimes) {
        try (Connection conn = getConnection()) {
            String insert = "INSERT INTO race_partial_times (player_name, map_id, checkpoint, time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                for (Map.Entry<String, Double> entry : partialTimes.entrySet()) {
                    stmt.setString(1, playerName);
                    stmt.setString(2, mapId);
                    stmt.setString(3, entry.getKey());
                    stmt.setDouble(4, entry.getValue());
                    stmt.executeUpdate();
                }
            }
            
            plugin.getLogger().info("DEBUG: Tempos parciais de corrida salvos no MySQL para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos parciais de corrida no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveGrapplerTimes(String playerName, double totalTime, Map<String, Double> individualTimes) {
        try (Connection conn = getConnection()) {
            String insertTotal = "INSERT INTO grappler_times (player_name, total_time) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTotal)) {
                stmt.setString(1, playerName);
                stmt.setDouble(2, totalTime);
                stmt.executeUpdate();
            }

            plugin.getLogger().info("DEBUG: Tempos Grappler salvos no MySQL para " + playerName);

        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos Grappler no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveGrapplerPartialTimes(String playerName, Map<String, Double> partialTimes) {
    }
    
    public void incrementGameCount(String playerName, String mapId) {
        try (Connection conn = getConnection()) {
            String upsert = "INSERT INTO game_count (player_name, map_id, games_played) VALUES (?, ?, 1) " +
                           "ON DUPLICATE KEY UPDATE games_played = games_played + 1";
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, playerName);
                stmt.setString(2, mapId);
                stmt.executeUpdate();
            }
            
            plugin.getLogger().info("DEBUG: Contador de jogos incrementado para " + playerName + " no mapa " + mapId);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void incrementKangarooGameCount(String playerName) {
        try (Connection conn = getConnection()) {
            String upsert = "INSERT INTO kangaroo_game_count (player_name, games_played) VALUES (?, 1) " +
                           "ON DUPLICATE KEY UPDATE games_played = games_played + 1";
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
            }
            
            plugin.getLogger().info("DEBUG: Contador de jogos Kangaroo incrementado para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos Kangaroo no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void incrementGrapplerGameCount(String playerName) {
        try (Connection conn = getConnection()) {
            String upsert = "INSERT INTO grappler_game_count (player_name, games_played) VALUES (?, 1) " +
                           "ON DUPLICATE KEY UPDATE games_played = games_played + 1";
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
            }
            
            plugin.getLogger().info("DEBUG: Contador de jogos Grappler incrementado para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos Grappler no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public int getGameCount(String playerName, String mapId) {
        try (Connection conn = getConnection()) {
            String query = "SELECT games_played FROM game_count WHERE player_name = ? AND map_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, playerName);
                stmt.setString(2, mapId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("games_played");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar contador de jogos no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getKangarooGameCount(String playerName) {
        try (Connection conn = getConnection()) {
            String query = "SELECT games_played FROM kangaroo_game_count WHERE player_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, playerName);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("games_played");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar contador de jogos Kangaroo no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getGrapplerGameCount(String playerName) {
        try (Connection conn = getConnection()) {
            String query = "SELECT games_played FROM grappler_game_count WHERE player_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, playerName);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("games_played");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar contador de jogos Grappler no MySQL: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    public boolean alterPlayerTime(String playerName, String mapId, String checkpoint, double newTime) {
        try (Connection conn = getConnection()) {
            String update = "UPDATE race_partial_times SET time = ? WHERE player_name = ? AND map_id = ? AND checkpoint = ?";
            try (PreparedStatement stmt = conn.prepareStatement(update)) {
                stmt.setDouble(1, newTime);
                stmt.setString(2, playerName);
                stmt.setString(3, mapId);
                stmt.setString(4, checkpoint);
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao alterar tempo no MySQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public ResultSet getRaceRanking(String mapId, String checkpoint, int limit) {
        try {
            Connection conn = getConnection();
            String query;
            
            if ("TOTAL".equals(checkpoint)) {
                query = "SELECT player_name, MIN(total_time) as best_time FROM race_times " +
                       "WHERE map_id = ? GROUP BY player_name ORDER BY best_time ASC LIMIT ?";
            } else {
                query = "SELECT player_name, MIN(time) as best_time FROM race_partial_times " +
                       "WHERE map_id = ? AND checkpoint = ? GROUP BY player_name ORDER BY best_time ASC LIMIT ?";
            }
            
            PreparedStatement stmt = conn.prepareStatement(query);
            
            if ("TOTAL".equals(checkpoint)) {
                stmt.setString(1, mapId);
                stmt.setInt(2, limit);
            } else {
                stmt.setString(1, mapId);
                stmt.setString(2, checkpoint);
                stmt.setInt(3, limit);
            }
            
            return stmt.executeQuery();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar ranking de corrida no MySQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public ResultSet getKangarooRanking(String checkpoint, int limit) {
        try {
            Connection conn = getConnection();
            String query;
            
            if ("TOTAL".equals(checkpoint)) {
                query = "SELECT player_name, MIN(total_time) as best_time FROM kangaroo_times " +
                       "GROUP BY player_name ORDER BY best_time ASC LIMIT ?";
            } else {
                query = "SELECT player_name, MIN(time) as best_time FROM kangaroo_partial_times " +
                       "WHERE checkpoint = ? GROUP BY player_name ORDER BY best_time ASC LIMIT ?";
            }
            
            PreparedStatement stmt = conn.prepareStatement(query);
            
            if ("TOTAL".equals(checkpoint)) {
                stmt.setInt(1, limit);
            } else {
                stmt.setString(1, checkpoint);
                stmt.setInt(2, limit);
            }
            
            return stmt.executeQuery();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar ranking do Kangaroo no MySQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getGrapplerRanking(int limit) {
        try {
            Connection conn = getConnection();
            String query = "SELECT player_name, MIN(total_time) as best_time FROM grappler_times " +
                          "GROUP BY player_name ORDER BY best_time ASC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);

            return stmt.executeQuery();

        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao buscar ranking do Grappler no MySQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void close() {
        plugin.getLogger().info("MySQL configurado para conexões sob demanda - nenhuma conexão persistente para fechar.");
    }
}
