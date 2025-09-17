package me.tantsz.practicegames.commands;

import me.tantsz.practicegames.PracticeGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReiniciarCommand implements CommandExecutor {
    
    private final PracticeGames plugin;
    
    public ReiniciarCommand(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado IN-GAME.");
            return true;
        }
        
        String[] newArgs = {"reiniciar"};
        GameCommandExecutor executor = (GameCommandExecutor) plugin.getCommand("minigame").getExecutor();
        return executor.onCommand(sender, plugin.getCommand("minigame"), "minigame", newArgs);
    }
}
