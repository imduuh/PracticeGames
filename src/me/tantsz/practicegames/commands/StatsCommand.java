package me.tantsz.practicegames.commands;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.gui.StatsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {
    
    private final PracticeGames plugin;
    
    public StatsCommand(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado IN-GAME.");
            return true;
        }
        
        Player player = (Player) sender;
        StatsGUI.abrirEscolhaMapa(player);
        return true;
    }
}
