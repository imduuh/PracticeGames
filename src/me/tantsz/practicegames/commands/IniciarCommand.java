package me.tantsz.practicegames.commands;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IniciarCommand implements CommandExecutor {
    
    private final PracticeGames plugin;
    
    public IniciarCommand(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado IN-GAME.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§cUso: /iniciar <end|grande|kangaroo> [checkpoint]");
            player.sendMessage("§7Exemplos:");
            player.sendMessage("§7  /iniciar kangaroo");
            player.sendMessage("§7  /iniciar end FLORESTA");
            player.sendMessage("§7  /iniciar grande CAVERNA");
            return true;
        }
        
        String mapa = args[0].toLowerCase();
        
        String[] newArgs;
        if (args.length >= 2) {
            newArgs = new String[]{"iniciar", mapa, args[1]};
        } else {
            newArgs = new String[]{"iniciar", mapa};
        }
        
        GameCommandExecutor executor = (GameCommandExecutor) plugin.getCommand("minigame").getExecutor();
        return executor.onCommand(sender, plugin.getCommand("minigame"), "minigame", newArgs);
    }
}
