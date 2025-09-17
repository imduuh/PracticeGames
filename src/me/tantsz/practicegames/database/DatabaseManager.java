package me.tantsz.practicegames.database;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.database.impl.MySQLDatabase;
import me.tantsz.practicegames.database.impl.SQLiteDatabase;

import java.sql.ResultSet;
import java.util.Map;

public class DatabaseManager {
    
    private final PracticeGames plugin;
    private final Database database;
    
    public DatabaseManager(PracticeGames plugin) {
        this.plugin = plugin;
        
        if (plugin.getConfig().getBoolean("MySQL.Ativado")) {
            this.database = new MySQLDatabase(plugin);
        } else {
            this.database = new SQLiteDatabase(plugin);
        }
        
        database.initialize();
    }
    
    public void saveKangarooTimes(String playerName, double totalTime, Map<String, Double> individualTimes) {
        try {
            plugin.getLogger().info("DEBUG: DatabaseManager - Salvando tempos Kangaroo para " + playerName);
            plugin.getLogger().info("DEBUG: Tempo total: " + totalTime + "s");
            plugin.getLogger().info("DEBUG: Tempos individuais: " + individualTimes);
            
            database.saveKangarooTimes(playerName, totalTime, individualTimes);
            
            plugin.getLogger().info("DEBUG: Tempos Kangaroo salvos com sucesso!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: Erro ao salvar tempos Kangaroo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveKangarooPartialTimes(String playerName, Map<String, Double> partialTimes) {
        try {
            plugin.getLogger().info("DEBUG: DatabaseManager - Salvando tempos parciais Kangaroo para " + playerName);
            plugin.getLogger().info("DEBUG: Tempos parciais: " + partialTimes);
            
            database.saveKangarooPartialTimes(playerName, partialTimes);
            
            plugin.getLogger().info("DEBUG: Tempos parciais Kangaroo salvos com sucesso!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: Erro ao salvar tempos parciais Kangaroo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveRaceTimes(String playerName, String mapId, double totalTime, Map<String, Double> checkpointTimes) {
        try {
            database.saveRaceTimes(playerName, mapId, totalTime, checkpointTimes);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao salvar tempos de corrida: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void savePartialRaceTimes(String playerName, String mapId, Map<String, Double> partialTimes) {
        try {
            database.savePartialRaceTimes(playerName, mapId, partialTimes);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao salvar tempos parciais de corrida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean alterPlayerTime(String playerName, String mapId, String checkpoint, double newTime) {
        try {
            if (database instanceof MySQLDatabase) {
                return ((MySQLDatabase) database).alterPlayerTime(playerName, mapId, checkpoint, newTime);
            } else if (database instanceof SQLiteDatabase) {
                return ((SQLiteDatabase) database).alterPlayerTime(playerName, mapId, checkpoint, newTime);
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao alterar tempo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public ResultSet getRaceRanking(String mapId, String checkpoint, int limit) {
        try {
            if (database instanceof MySQLDatabase) {
                return ((MySQLDatabase) database).getRaceRanking(mapId, checkpoint, limit);
            } else if (database instanceof SQLiteDatabase) {
                return ((SQLiteDatabase) database).getRaceRanking(mapId, checkpoint, limit);
            }
            return null;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao buscar ranking de corrida: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public ResultSet getKangarooRanking(String checkpoint, int limit) {
        try {
            if (database instanceof MySQLDatabase) {
                return ((MySQLDatabase) database).getKangarooRanking(checkpoint, limit);
            } else if (database instanceof SQLiteDatabase) {
                return ((SQLiteDatabase) database).getKangarooRanking(checkpoint, limit);
            }
            return null;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao buscar ranking do Kangaroo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public void saveGrapplerTimes(String playerName, double totalTime, Map<String, Double> individualTimes) {
        try {
            plugin.getLogger().info("DEBUG: DatabaseManager - Salvando tempos Grappler para " + playerName);
            plugin.getLogger().info("DEBUG: Tempo total: " + totalTime + "s");
            
            database.saveGrapplerTimes(playerName, totalTime, individualTimes);
            
            plugin.getLogger().info("DEBUG: Tempos Grappler salvos com sucesso!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: Erro ao salvar tempos Grappler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public ResultSet getGrapplerRanking(int limit) {
        try {
            if (database instanceof MySQLDatabase) {
                return ((MySQLDatabase) database).getGrapplerRanking(limit);
            } else if (database instanceof SQLiteDatabase) {
                return ((SQLiteDatabase) database).getGrapplerRanking(limit);
            }
            return null;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao buscar ranking do Grappler: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public void incrementGameCount(String playerName, String mapId) {
        try {
            if (database instanceof MySQLDatabase) {
                ((MySQLDatabase) database).incrementGameCount(playerName, mapId);
            } else if (database instanceof SQLiteDatabase) {
                ((SQLiteDatabase) database).incrementGameCount(playerName, mapId);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void incrementKangarooGameCount(String playerName) {
        try {
            if (database instanceof MySQLDatabase) {
                ((MySQLDatabase) database).incrementKangarooGameCount(playerName);
            } else if (database instanceof SQLiteDatabase) {
                ((SQLiteDatabase) database).incrementKangarooGameCount(playerName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos Kangaroo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void incrementGrapplerGameCount(String playerName) {
        try {
            if (database instanceof MySQLDatabase) {
                ((MySQLDatabase) database).incrementGrapplerGameCount(playerName);
            } else if (database instanceof SQLiteDatabase) {
                ((SQLiteDatabase) database).incrementGrapplerGameCount(playerName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao incrementar contador de jogos Grappler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public int getGameCount(String playerName, String mapId) {
        try {
            if (database instanceof MySQLDatabase) {
                return ((MySQLDatabase) database).getGameCount(playerName, mapId);
            } else if (database instanceof SQLiteDatabase) {
                return ((SQLiteDatabase) database).getGameCount(playerName, mapId);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao buscar contador de jogos: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getKangarooGameCount(String playerName) {
        try {
            if (database instanceof MySQLDatabase) {
                return ((MySQLDatabase) database).getKangarooGameCount(playerName);
            } else if (database instanceof SQLiteDatabase) {
                return ((SQLiteDatabase) database).getKangarooGameCount(playerName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao buscar contador de jogos Kangaroo: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getGrapplerGameCount(String playerName) {
        try {
            if (database instanceof MySQLDatabase) {
                return ((MySQLDatabase) database).getGrapplerGameCount(playerName);
            } else if (database instanceof SQLiteDatabase) {
                return ((SQLiteDatabase) database).getGrapplerGameCount(playerName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao buscar contador de jogos Grappler: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    public void closeConnections() {
        if (database != null) {
            database.close();
        }
    }
    
    public Database getDatabase() {
        return database;
    }
}
