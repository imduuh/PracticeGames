package me.tantsz.practicegames.commands;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.gui.TopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommand implements CommandExecutor {
    
    private final PracticeGames plugin;
    
    public TopCommand(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado IN-GAME.");
            return true;
        }
        
        Player player = (Player) sender;
        TopGUI.abrirEscolhaMapa(player);
        return true;
    }
}
