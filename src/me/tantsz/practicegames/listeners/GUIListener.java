package me.tantsz.practicegames.listeners;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.gui.StatsGUI;
import me.tantsz.practicegames.gui.TopGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    
    private final PracticeGames plugin;
    
    public GUIListener(PracticeGames plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();
        ItemStack item = event.getCurrentItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String itemName = item.getItemMeta().getDisplayName();
        
        if (title.equals("TOP RANKINGS")) {
            event.setCancelled(true);
            
            if (itemName.equals("§6MAPA END")) {
                TopGUI.abrirRankingMapa(player, "mapa1");
            } else if (itemName.equals("§aMAPA GRANDE")) {
                TopGUI.abrirRankingMapa(player, "mapa2");
            } else if (itemName.equals("§eKANGAROO")) {
                TopGUI.abrirRankingMapa(player, "kangaroo");
            } else if (itemName.equals("§aGRAPPLER")) {
                TopGUI.mostrarRanking(player, "TOTAL", "grappler");
            } else if (itemName.equals("§cFECHAR")) {
                player.closeInventory();
            }
        }
        
        else if (title.startsWith("TOP ")) {
            event.setCancelled(true);
            
            String mapa = getMapeFromTitle(title);
            
            if (itemName.equals("§bTEMPO TOTAL")) {
                TopGUI.mostrarRanking(player, "TOTAL", mapa);
            } else if (itemName.equals("§6END")) {
                TopGUI.mostrarRanking(player, "END", mapa);
            } else if (itemName.equals("§eDESERTO")) {
                TopGUI.mostrarRanking(player, "DESERTO", mapa);
            } else if (itemName.equals("§2JUNGLE")) {
                TopGUI.mostrarRanking(player, "JUNGLE", mapa);
            } else if (itemName.equals("§aFLORESTA")) {
                TopGUI.mostrarRanking(player, "FLORESTA", mapa);
            } else if (itemName.equals("§cNETHER")) {
                TopGUI.mostrarRanking(player, "NETHER", mapa);
            } else if (itemName.equals("§8CAVERNA")) {
                TopGUI.mostrarRanking(player, "CAVERNA", mapa);
            } else if (itemName.equals("§bGELO")) {
                TopGUI.mostrarRanking(player, "GELO", mapa);
            } else if (itemName.equals("§2PLANICIE")) {
                TopGUI.mostrarRanking(player, "PLANICIE", mapa);
            } else if (itemName.equals("§8CHECKPOINT 1")) {
                TopGUI.mostrarRanking(player, "CHECKPOINT1", mapa);
            } else if (itemName.equals("§7CHECKPOINT 2")) {
                TopGUI.mostrarRanking(player, "CHECKPOINT2", mapa);
            } else if (itemName.equals("§6CHECKPOINT 3")) {
                TopGUI.mostrarRanking(player, "CHECKPOINT3", mapa);
            } else if (itemName.equals("§bCHECKPOINT 4")) {
                TopGUI.mostrarRanking(player, "CHECKPOINT4", mapa);
            } else if (itemName.equals("§eVOLTAR")) {
                TopGUI.abrirEscolhaMapa(player);
            } else if (itemName.equals("§cFECHAR")) {
                player.closeInventory();
            }
        }
        
        else if (title.contains(" - ") && !title.contains(player.getName())) {
            event.setCancelled(true);
            
            if (itemName.equals("§eVOLTAR")) {
                String mapa = getMapeFromRankingTitle(title);
                if (mapa.equals("grappler")) {
                    TopGUI.abrirEscolhaMapa(player);
                } else {
                    TopGUI.abrirRankingMapa(player, mapa);
                }
            } else if (itemName.equals("§cFECHAR")) {
                player.closeInventory();
            }
        }
        
        else if (title.equals("SUAS ESTATISTICAS")) {
            event.setCancelled(true);
            
            if (itemName.equals("§6MAPA END")) {
                StatsGUI.mostrarStats(player, "mapa1");
            } else if (itemName.equals("§aMAPA GRANDE")) {
                StatsGUI.mostrarStats(player, "mapa2");
            } else if (itemName.equals("§eKANGAROO")) {
                StatsGUI.mostrarStats(player, "kangaroo");
            } else if (itemName.equals("§aGRAPPLER")) {
                StatsGUI.mostrarStats(player, "grappler");
            } else if (itemName.equals("§cFECHAR")) {
                player.closeInventory();
            }
        }

        
        else if (!title.equals("SUAS ESTATISTICAS") && !title.equals("TOP RANKINGS") && !title.startsWith("TOP ") && title.contains(player.getName())) {
            event.setCancelled(true);

            if (itemName.equals("§eVOLTAR")) {
                StatsGUI.abrirEscolhaMapa(player);
            } else if (itemName.equals("§cFECHAR")) {
                player.closeInventory();
            }
        }
    }
    
    private String getMapeFromTitle(String title) {
        if (title.contains("Mapa End")) {
            return "mapa1";
        } else if (title.contains("Mapa Grande")) {
            return "mapa2";
        } else if (title.contains("Kangaroo")) {
            return "kangaroo";
        } else if (title.contains("Grappler")) {
            return "grappler";
        }
        return "mapa1";
    }
    
    private String getMapeFromRankingTitle(String title) {
        if (title.contains("Mapa End")) {
            return "mapa1";
        } else if (title.contains("Mapa Grande")) {
            return "mapa2";
        } else if (title.contains("Kangaroo")) {
            return "kangaroo";
        } else if (title.contains("Grappler")) {
            return "grappler";
        }
        return "mapa1";
    }
}
