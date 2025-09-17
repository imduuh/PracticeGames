package me.tantsz.practicegames.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tantsz.practicegames.gui.TopGUI;
import me.tantsz.practicegames.gui.StatsGUI;
import me.tantsz.practicegames.PracticeGames;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class GameCommandExecutor implements CommandExecutor {

    private final PracticeGames plugin;
    private final Map<UUID, BukkitTask> countdownTasks = new HashMap<>();

    public GameCommandExecutor(PracticeGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reiniciar")) {
            return handleRestartCommand(player);
        } else if (subCommand.equals("top")) {
            TopGUI.abrirEscolhaMapa(player);
            return true;
        } else if (subCommand.equals("stats")) {
            StatsGUI.abrirEscolhaMapa(player);
            return true;
        } else if (subCommand.equals("iniciar")) {
            return handleStartCommand(player, args);
        } else if (subCommand.equals("cancelar")) {
            return handleCancelCommand(player);
        } else if (subCommand.equals("set")) {
            return handleSetCommand(player, args);
        } else if (subCommand.equals("alterartempo")) {
            return true;
        } else {
            showHelp(player);
            return true;
        }
    }

    private boolean handleRestartCommand(Player player) {
        UUID playerId = player.getUniqueId();
    
        if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
            player.sendMessage("§cVocê já está em uma contagem regressiva! Aguarde...");
            return true;
        }
        
        if (plugin.getRaceManager().isPlayerInRace(player)) {
            plugin.getRaceManager().restartRace(player);
            return true;
        } 
        else if (plugin.getKangarooManager().isPlayerInRace(player)) {
            plugin.getKangarooManager().restartRace(player);
            return true;
        } 
        else if (plugin.getGrapplerManager().isPlayerInRace(player)) {
            plugin.getGrapplerManager().restartRace(player);
            return true;
        } 
        else {
            player.sendMessage("§cVocê não está em nenhum jogo para reiniciar.");
            return true;
        }
    }

    private boolean handleStartCommand(Player player, String[] args) {
        UUID playerId = player.getUniqueId();
        
        if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
            player.sendMessage("§cVocê já está em uma contagem regressiva! Aguarde...");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUso: /minigame iniciar <end|grande|kangaroo|grappler> [checkpoint/spawn]");
            return true;
        }

        String mapType = args[1].toLowerCase();
        String checkpoint = (args.length > 2) ? args[2].toUpperCase() : null;

        if (plugin.getRaceManager().isPlayerInRace(player)) {
            player.sendMessage("§cVocê já está em uma corrida! Use /minigame cancelar primeiro.");
            return true;
        }

        if (plugin.getKangarooManager().isPlayerInRace(player)) {
            player.sendMessage("§cVocê já está em uma corrida Kangaroo! Use /minigame cancelar primeiro.");
            return true;
        }

        if (plugin.getGrapplerManager().isPlayerInRace(player)) {
            player.sendMessage("§cVocê já está em uma corrida Grappler! Use /minigame cancelar primeiro.");
            return true;
        }

        if (mapType.equals("grappler")) {
            int spawnNumber = 1;
        
            if (checkpoint != null) {
                try {
                    spawnNumber = Integer.parseInt(checkpoint);
                    if (spawnNumber < 1 || spawnNumber > 12) {
                        player.sendMessage("§cSpawn inválido! Use um número de 1 a 12.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cSpawn inválido! Use um número de 1 a 12.");
                    return true;
                }
            }
        
            return startGrapplerRace(player, spawnNumber);
        }

        if (mapType.equals("kangaroo")) {
        	player.sendMessage("§cEste modo está desabilitado temporariamente.");
        	return true;
        }

        return startNormalRace(player, mapType, checkpoint);
    }

    private boolean startKangarooRace(Player player) {
    	
        UUID playerId = player.getUniqueId();

        boolean teleportSuccess = plugin.getTeleportUtil().teleport("kangaroo", player);
        if (!teleportSuccess) {
            player.sendMessage("§cErro: Spawn do Kangaroo não configurado. Use /minigame set kangaroo spawn");
            return true;
        }

        plugin.getGameManager().addPlayerToCountdown(playerId);

        BukkitTask countdownTask = new BukkitRunnable() {
            int seconds = 3;

            @Override
            public void run() {
                if (!player.isOnline() || !plugin.getGameManager().isPlayerInCountdown(playerId)) {
                    cancel();
                    countdownTasks.remove(playerId);
                    return;
                }

                if (seconds > 0) {
                    player.sendMessage("§e" + seconds);
                    seconds--;
                } else {
                    cancel();
                    countdownTasks.remove(playerId);
                    plugin.getGameManager().removePlayerFromCountdown(playerId);

                    if (player.isOnline()) {
                        plugin.getKangarooManager().startRace(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        countdownTasks.put(playerId, countdownTask);
        return true;
    }

    private boolean startNormalRace(Player player, String mapType, String checkpoint) {
        UUID playerId = player.getUniqueId();
        
        String mapId;
        if (mapType.equals("end")) {
            mapId = "mapa1";
        } else if (mapType.equals("grande")) {
            mapId = "mapa2";
        } else {
            player.sendMessage("§cMapa inválido! Use: end, grande, kangaroo ou grappler");
            return true;
        }

        boolean teleportSuccess = plugin.getTeleportUtil().teleport(mapType, player);
        if (!teleportSuccess) {
            player.sendMessage("§cErro: Spawn do mapa não configurado. Use /minigame set " + mapType + " spawn");
            return true;
        }

        plugin.getGameManager().addPlayerToCountdown(playerId);

        BukkitTask countdownTask = new BukkitRunnable() {
            int seconds = 3;

            @Override
            public void run() {
                if (!player.isOnline() || !plugin.getGameManager().isPlayerInCountdown(playerId)) {
                    cancel();
                    countdownTasks.remove(playerId);
                    return;
                }

                if (seconds > 0) {
                    player.sendMessage("§e" + seconds);
                    seconds--;
                } else {
                    cancel();
                    countdownTasks.remove(playerId);
                    plugin.getGameManager().removePlayerFromCountdown(playerId);

                    if (player.isOnline()) {
                        plugin.getRaceManager().startRace(player, mapId, checkpoint);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        countdownTasks.put(playerId, countdownTask);
        return true;
    }

    private boolean startGrapplerRace(Player player, int spawnNumber) {
        UUID playerId = player.getUniqueId();

        boolean teleportSuccess = plugin.getTeleportUtil().teleport("grappler." + spawnNumber, player);
        if (!teleportSuccess) {
            player.sendMessage("§cErro: Spawn " + spawnNumber + " do Grappler não configurado. Use /minigame set grappler " + spawnNumber);
            return true;
        }

        plugin.getGameManager().addPlayerToCountdown(playerId);

        BukkitTask countdownTask = new BukkitRunnable() {
            int seconds = 3;

            @Override
            public void run() {
                if (!player.isOnline() || !plugin.getGameManager().isPlayerInCountdown(playerId)) {
                    cancel();
                    countdownTasks.remove(playerId);
                    return;
                }

                if (seconds > 0) {
                    player.sendMessage("§e" + seconds);
                    seconds--;
                } else {
                    cancel();
                    countdownTasks.remove(playerId);
                    plugin.getGameManager().removePlayerFromCountdown(playerId);

                    if (player.isOnline()) {
                        plugin.getGrapplerManager().startRace(player, spawnNumber);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        countdownTasks.put(playerId, countdownTask);
        return true;
    }

    private boolean handleCancelCommand(Player player) {
        UUID playerId = player.getUniqueId();

        if (plugin.getGameManager().isPlayerInCountdown(playerId)) {
            BukkitTask task = countdownTasks.get(playerId);
            if (task != null) {
                task.cancel();
                countdownTasks.remove(playerId);
                plugin.getGameManager().removePlayerFromCountdown(playerId);
                player.sendMessage("§cContagem regressiva cancelada.");
                return true;
            }
        }

        if (plugin.getRaceManager().isPlayerInRace(player)) {
            plugin.getRaceManager().cancelRace(player);
        } else if (plugin.getKangarooManager().isPlayerInRace(player)) {
            plugin.getKangarooManager().cancelRace(player);
        } else if (plugin.getGrapplerManager().isPlayerInRace(player)) {
            plugin.getGrapplerManager().cancelRace(player);
        } else {
            player.sendMessage("§cVocê não está em nenhuma corrida ou contagem regressiva!");
        }

        return true;
    }

    private boolean handleSetCommand(Player player, String[] args) {
        if (!player.hasPermission("practicegames.admin")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§cUso: /minigame set <mapa> <tipo> [nome/numero]");
            player.sendMessage("§7Exemplos:");
            player.sendMessage("§7  /minigame set grappler 1 (spawn 1)");
            player.sendMessage("§7  /minigame set grappler 12 (spawn 12)");
            player.sendMessage("§7  /minigame set grappler saida");
            return true;
        }

        String mapId = args[1].toLowerCase();
        if (!mapId.equals("end") && !mapId.equals("grande") && !mapId.equals("kangaroo") && !mapId.equals("grappler")) {
            player.sendMessage("§cMapa inválido! Use: end, grande, kangaroo ou grappler.");
            return true;
        }

        String setType = args[2].toLowerCase();
        Location location = player.getLocation();
        String locationKey;
        String message;

        if (mapId.equals("grappler")) {
            if (setType.equals("saida")) {
                locationKey = mapId + ".saida";
                message = "§aLocal de saída do §aGrappler §adefinido com sucesso.";
            } else {
                try {
                    int spawnNumber = Integer.parseInt(setType);
                    if (spawnNumber < 1 || spawnNumber > 12) {
                        player.sendMessage("§cSpawn inválido! Use um número de 1 a 12 ou 'saida'.");
                        return true;
                    }
                    locationKey = mapId + "." + spawnNumber;
                    message = "§aSpawn " + spawnNumber + " do §aGrappler §adefinido com sucesso.";
                } catch (NumberFormatException e) {
                    player.sendMessage("§cPara Grappler, use um número de 1 a 12 ou 'saida'.");
                    return true;
                }
            }
        } else if (setType.equals("spawn")) {
            locationKey = mapId;
            if (mapId.equals("kangaroo")) {
                message = "§aLocal de início do §eKangaroo §adefinido com sucesso.";
            } else {
                message = "§aLocal de início da §e" + plugin.getRaceManager().getMapDisplayName(mapId.equals("end") ? "mapa1" : "mapa2") + " §adefinido com sucesso.";
            }
        } else if (setType.equals("saida")) {
            locationKey = mapId + ".saida";
            if (mapId.equals("kangaroo")) {
                message = "§aLocal de saída do §eKangaroo §adefinido com sucesso.";
            } else {
                message = "§aLocal de saída da §e" + plugin.getRaceManager().getMapDisplayName(mapId.equals("end") ? "mapa1" : "mapa2") + " §adefinido com sucesso.";
            }
        } else {
            if (mapId.equals("kangaroo")) {
                player.sendMessage("§cPara Kangaroo, use apenas: spawn ou saida");
                return true;
            }

            if (!plugin.getRaceManager().isValidCheckpointName(setType.toUpperCase())) {
                return true;
            }

            locationKey = mapId + ".checkpoints." + setType.toUpperCase();
            message = "§aLocal do checkpoint §e" + setType.toUpperCase() + "§a para " +
                    plugin.getRaceManager().getMapDisplayName(mapId.equals("end") ? "mapa1" : "mapa2") + " §adefinido com sucesso.";
        }

        plugin.getTeleportUtil().setLocation(locationKey, location);
        player.sendMessage(message);
        return true;
    }

    private void showHelp(Player player) {
    	player.sendMessage("§e/minigame iniciar <end|grande|kangaroo|grappler> [checkpoint/spawn] §7- §fIniciar corrida");
    	player.sendMessage("§e/minigame cancelar §7- §fCancelar corrida/jogo");;
        player.sendMessage("§e/minigame reiniciar §7- §fReiniciar corrida atual");
        player.sendMessage("§e/minigame top §7- §fVer rankings");
        player.sendMessage("§e/minigame stats §7- §fVer suas estatísticas");
       
        if (player.hasPermission("practicegames.admin")) {
            player.sendMessage("§c/minigame set <end|grande|kangaroo|grappler> <spawn|saida|checkpoint_name|1-12> §7- §fDefinir spawn/saída/checkpoint (Admin)");
        }
    }

    public void cleanupPlayer(UUID playerId) {
        BukkitTask task = countdownTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }
}
