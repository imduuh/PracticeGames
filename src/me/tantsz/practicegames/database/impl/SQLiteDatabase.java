package me.tantsz.practicegames.database.impl;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.database.Database;

import java.io.File;
import java.sql.*;
import java.util.Map;

public class SQLiteDatabase implements Database {
    
    private final PracticeGames plugin;
    private final String databasePath;
    
    public SQLiteDatabase(PracticeGames plugin) {
        this.plugin = plugin;
        this.databasePath = plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db";
    }
    
    @Override
    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            try (Connection testConn = DriverManager.getConnection("jdbc:sqlite:" + databasePath)) {
                createTables(testConn);
                plugin.getLogger().info("Conectado ao SQLite com sucesso!");
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao conectar ao SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
    }
    
    private void createTables(Connection conn) throws SQLException {
        String createRaceTimesTable = "CREATE TABLE IF NOT EXISTS race_times (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL, " +
            "map_id TEXT NOT NULL, " +
            "total_time REAL NOT NULL, " +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        String createRacePartialTimesTable = "CREATE TABLE IF NOT EXISTS race_partial_times (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL, " +
            "map_id TEXT NOT NULL, " +
            "checkpoint TEXT NOT NULL, " +
            "time REAL NOT NULL, " +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        String createKangarooTimesTable = "CREATE TABLE IF NOT EXISTS kangaroo_times (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL, " +
            "total_time REAL NOT NULL, " +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        String createKangarooPartialTimesTable = "CREATE TABLE IF NOT EXISTS kangaroo_partial_times (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL, " +
            "checkpoint TEXT NOT NULL, " +
            "time REAL NOT NULL, " +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";

        String createGrapplerTimesTable = "CREATE TABLE IF NOT EXISTS grappler_times (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL, " +
            "total_time REAL NOT NULL, " +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        String createGameCountTable = "CREATE TABLE IF NOT EXISTS game_count (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL, " +
            "map_id TEXT NOT NULL, " +
            "games_played INTEGER DEFAULT 1, " +
            "UNIQUE(player_name, map_id)" +
            ")";
        
        String createKangarooGameCountTable = "CREATE TABLE IF NOT EXISTS kangaroo_game_count (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL UNIQUE, " +
            "games_played INTEGER DEFAULT 1" +
            ")";
        
        String createGrapplerGameCountTable = "CREATE TABLE IF NOT EXISTS grappler_game_count (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_name TEXT NOT NULL UNIQUE, " +
            "games_played INTEGER DEFAULT 1" +
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
            
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_race_player_map ON race_times(player_name, map_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_race_partial_player_map ON race_partial_times(player_name, map_id, checkpoint)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_kangaroo_player ON kangaroo_times(player_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_kangaroo_partial_player ON kangaroo_partial_times(player_name, checkpoint)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_grappler_player ON grappler_times(player_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_game_count_player_map ON game_count(player_name, map_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_kangaroo_game_count_player ON kangaroo_game_count(player_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_grappler_game_count_player ON grappler_game_count(player_name)");
            
            plugin.getLogger().info("Tabelas SQLite criadas/verificadas com sucesso!");
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
            
            plugin.getLogger().info("DEBUG: Tempos Kangaroo salvos no SQLite para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos Kangaroo no SQLite: " + e.getMessage());
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
            
            plugin.getLogger().info("DEBUG: Tempos parciais Kangaroo salvos no SQLite para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos parciais Kangaroo no SQLite: " + e.getMessage());
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
            
            plugin.getLogger().info("DEBUG: Tempos de corrida salvos no SQLite para " + playerName + " no mapa " + mapId);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos de corrida no SQLite: " + e.getMessage());
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
            
            plugin.getLogger().info("DEBUG: Tempos parciais de corrida salvos no SQLite para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos parciais de corrida no SQLite: " + e.getMessage());
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
            
            plugin.getLogger().info("DEBUG: Tempos Grappler salvos no SQLite para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar tempos Grappler no SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveGrapplerPartialTimes(String playerName, Map<String, Double> partialTimes) {
    }
    
    public void incrementGameCount(String playerName, String mapId) {
        try (Connection conn = getConnection()) {
            String insert = "INSERT OR IGNORE INTO game_count (player_name, map_id, games_played) VALUES (?, ?, 1)";
            try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                stmt.setString(1, playerName);
                stmt.setString(2, mapId);
                stmt.executeUpdate();
            }
            
            String update = "UPDATE game_count SET games_played = games_played + 1 WHERE player_name = ? AND map_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(update)) {
                stmt.setString(1, playerName);
                stmt.setString(2, mapId);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    String insertFirst = "INSERT INTO game_count (player_name, map_id, games_played) VALUES (?, ?, 1)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertFirst)) {
                        insertStmt.setString(1, playerName);
                        insertStmt.setString(2, mapId);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            plugin.getLogger().info("DEBUG: Contador de jogos incrementado para " + playerName + " no mapa " + mapId);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos no SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void incrementKangarooGameCount(String playerName) {
        try (Connection conn = getConnection()) {
            String insert = "INSERT OR IGNORE INTO kangaroo_game_count (player_name, games_played) VALUES (?, 1)";
            try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
            }
            
            String update = "UPDATE kangaroo_game_count SET games_played = games_played + 1 WHERE player_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(update)) {
                stmt.setString(1, playerName);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected == 0) {
 
                    String insertFirst = "INSERT INTO kangaroo_game_count (player_name, games_played) VALUES (?, 1)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertFirst)) {
                        insertStmt.setString(1, playerName);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            plugin.getLogger().info("DEBUG: Contador de jogos Kangaroo incrementado para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos Kangaroo no SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void incrementGrapplerGameCount(String playerName) {
        try (Connection conn = getConnection()) {
            String insert = "INSERT OR IGNORE INTO grappler_game_count (player_name, games_played) VALUES (?, 1)";
            try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
            }
            
            String update = "UPDATE grappler_game_count SET games_played = games_played + 1 WHERE player_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(update)) {
                stmt.setString(1, playerName);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    String insertFirst = "INSERT INTO grappler_game_count (player_name, games_played) VALUES (?, 1)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertFirst)) {
                        insertStmt.setString(1, playerName);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            plugin.getLogger().info("DEBUG: Contador de jogos Grappler incrementado para " + playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos Grappler no SQLite: " + e.getMessage());
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
            plugin.getLogger().severe("Erro ao buscar contador de jogos no SQLite: " + e.getMessage());
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
            plugin.getLogger().severe("Erro ao buscar contador de jogos Kangaroo no SQLite: " + e.getMessage());
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
            plugin.getLogger().severe("Erro ao buscar contador de jogos Grappler no SQLite: " + e.getMessage());
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
            plugin.getLogger().severe("Erro ao alterar tempo no SQLite: " + e.getMessage());
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
            plugin.getLogger().severe("Erro ao buscar ranking de corrida no SQLite: " + e.getMessage());
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
            plugin.getLogger().severe("Erro ao buscar ranking do Kangaroo no SQLite: " + e.getMessage());
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
            plugin.getLogger().severe("Erro ao buscar ranking do Grappler no SQLite: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void close() {
        plugin.getLogger().info("SQLite configurado para conexões sob demanda");
    }
}
