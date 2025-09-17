package me.tantsz.practicegames.gui;

import me.tantsz.practicegames.PracticeGames;
import me.tantsz.practicegames.database.DatabaseService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class StatsGUI {

    private static ItemStack criarItemDecorativo(Material material, String nome) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nome);
        item.setItemMeta(meta);
        return item;
    }

    public static void abrirEscolhaMapa(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "SUAS ESTATISTICAS");

        ItemStack vidro = criarItemDecorativo(Material.STAINED_GLASS_PANE, " ");
        vidro.setDurability((short) 7);
        
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, vidro);
            inv.setItem(18 + i, vidro);
        }
        for (int i = 9; i < 18; i += 9) {
            inv.setItem(i, vidro);
            inv.setItem(i + 8, vidro);
        }

        ItemStack mapa1 = new ItemStack(Material.ENDER_STONE);
        ItemMeta meta1 = mapa1.getItemMeta();
        meta1.setDisplayName("§6MAPA END");
        List<String> lore1 = new ArrayList<>();
        lore1.add("§7Checkpoints: 5");
        lore1.add("");
        lore1.add("§eClique para ver suas stats");
        meta1.setLore(lore1);
        mapa1.setItemMeta(meta1);

        ItemStack mapa2 = new ItemStack(Material.GRASS);
        ItemMeta meta2 = mapa2.getItemMeta();
        meta2.setDisplayName("§aMAPA GRANDE");
        List<String> lore2 = new ArrayList<>();
        lore2.add("§7Checkpoints: 6");
        lore2.add("");
        lore2.add("§eClique para ver suas stats");
        meta2.setLore(lore2);
        mapa2.setItemMeta(meta2);

        ItemStack kangaroo = new ItemStack(Material.FIREWORK);
        ItemMeta metaKangaroo = kangaroo.getItemMeta();
        metaKangaroo.setDisplayName("§eKANGAROO");
        List<String> loreKangaroo = new ArrayList<>();
        loreKangaroo.add("§7Checkpoints: 4");
        loreKangaroo.add("");
        loreKangaroo.add("§eClique para ver suas stats");
        metaKangaroo.setLore(loreKangaroo);
        kangaroo.setItemMeta(metaKangaroo);

        ItemStack grappler = new ItemStack(Material.LEASH);
        ItemMeta metaGrappler = grappler.getItemMeta();
        metaGrappler.setDisplayName("§aGRAPPLER");
        List<String> loreGrappler = new ArrayList<>();
        loreGrappler.add("§7Checkpoint: 1 (Final)");
        loreGrappler.add("");
        loreGrappler.add("§eClique para ver suas stats");
        metaGrappler.setLore(loreGrappler);
        grappler.setItemMeta(metaGrappler);

        inv.setItem(10, mapa1);
        inv.setItem(12, mapa2);  
        inv.setItem(14, kangaroo);
        inv.setItem(16, grappler); 

        ItemStack livro = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta metaLivro = livro.getItemMeta();
        metaLivro.setDisplayName("§aSUAS ESTATISTICAS");
        List<String> loreEstrela = new ArrayList<>();
        loreEstrela.add("§7Escolha um modo para ver");
        loreEstrela.add("§7suas estatísticas pessoais");
        loreEstrela.add("");
        loreEstrela.add("§eJogador: §f" + player.getName());
        metaLivro.setLore(loreEstrela);
        livro.setItemMeta(metaLivro);
        inv.setItem(4, livro);

        ItemStack fechar = new ItemStack(Material.BARRIER);
        ItemMeta metaFechar = fechar.getItemMeta();
        metaFechar.setDisplayName("§cFECHAR");
        fechar.setItemMeta(metaFechar);
        inv.setItem(22, fechar);

        player.openInventory(inv);
    }

    public static void mostrarStats(Player player, String mapa) {
        String nomeMapa = getNomeMapa(mapa);
        Inventory inv = Bukkit.createInventory(null, 27, nomeMapa.replaceAll("§.", "") + " - " + player.getName());

        ItemStack vidro = criarItemDecorativo(Material.STAINED_GLASS_PANE, " ");
        vidro.setDurability((short) 7);
        
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, vidro);
            inv.setItem(18 + i, vidro);
        }
        inv.setItem(9, vidro);
        inv.setItem(17, vidro);

        DatabaseService dbService = PracticeGames.getInstance().getDatabaseService();
        DatabaseService.PlayerStats stats = dbService.buscarPlayerStats(player.getName(), mapa);

        ItemStack info = criarCabecaComSkin(player.getName());
        ItemMeta metaInfo = info.getItemMeta();
        metaInfo.setDisplayName("§eINFORMACOES GERAIS");
        List<String> loreInfo = new ArrayList<>();
        loreInfo.add("§7Jogador: §e" + player.getName());
        loreInfo.add("§7Mapa: " + nomeMapa);
        loreInfo.add("§7Jogos: §a" + stats.getJogos());
        
        if (stats.getMelhorTotal() < 999999.999) {
            loreInfo.add("§7Melhor: §b" + String.format("%.3f", stats.getMelhorTotal()) + "s");
        } else {
            loreInfo.add("§7Melhor: §cNenhum");
        }
        metaInfo.setLore(loreInfo);
        info.setItemMeta(metaInfo);
        inv.setItem(4, info);

        if (mapa.equals("grappler")) {
        } else {
            ItemStack tempoTeoricoJogador = criarTempoTeoricoJogador(mapa, stats, player.getName());
            inv.setItem(13, tempoTeoricoJogador);

            List<String> checkpoints = getCheckpointsParaMapa(mapa);
        
            int[] slots = {10, 11, 12, 14, 15, 16};
            for (int i = 0; i < Math.min(checkpoints.size(), slots.length); i++) {
                String checkpoint = checkpoints.get(i);
                ItemStack item = criarItemCheckpointStats(checkpoint, mapa, stats);
                inv.setItem(slots[i], item);
            }
        }

        ItemStack voltar = new ItemStack(Material.ARROW);
        ItemMeta metaVoltar = voltar.getItemMeta();
        metaVoltar.setDisplayName("§eVOLTAR");
        voltar.setItemMeta(metaVoltar);
        inv.setItem(21, voltar);

        ItemStack fechar = new ItemStack(Material.BARRIER);
        ItemMeta metaFechar = fechar.getItemMeta();
        metaFechar.setDisplayName("§cFECHAR");
        fechar.setItemMeta(metaFechar);
        inv.setItem(23, fechar);

        player.openInventory(inv);
    }

    private static ItemStack criarCabecaComSkin(String playerName) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        
        try {
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwner(playerName);
            skull.setItemMeta(skullMeta);
        } catch (Exception e) {
            PracticeGames.getInstance().getLogger().warning("Erro ao definir skin da cabeça para " + playerName + ": " + e.getMessage());
        }
        
        return skull;
    }

    private static ItemStack criarTempoTeoricoJogador(String mapa, DatabaseService.PlayerStats stats, String playerName) {
        List<String> checkpoints = getCheckpointsParaMapa(mapa);
        double tempoTotal = 0.0;
        int checkpointsComTempo = 0;
        
        List<String> detalhes = new ArrayList<>();
        
        for (String checkpoint : checkpoints) {
            double tempo = stats.getCheckpointTime(checkpoint);
            
            if (tempo < 999999.999) {
                tempoTotal += tempo;
                checkpointsComTempo++;
                detalhes.add("§7" + getNomeCheckpointFormatado(checkpoint).replaceAll("§.", "") + ": §a" + 
                           String.format("%.3f", tempo) + "s");
            } else {
                detalhes.add("§7" + getNomeCheckpointFormatado(checkpoint).replaceAll("§.", "") + ": §cSem tempo");
            }
        }
        
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6SEU TEMPO TEORICO");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Soma dos seus melhores");
        lore.add("§7tempos em cada checkpoint");
        lore.add("");
        
        if (checkpointsComTempo > 0) {
            lore.add("§eTotal: §b" + String.format("%.3f", tempoTotal) + "s");
            lore.add("§7Checkpoints: §a" + checkpointsComTempo + "§7/§e" + checkpoints.size());
        } else {
            lore.add("§cVocê ainda não tem tempos");
        }
        
        lore.add("");
        lore.addAll(detalhes);
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }

    
    private static String getNomeCheckpointFormatado(String checkpoint) {
        switch (checkpoint.toUpperCase()) {
            case "END": return "§6END";
            case "DESERTO": return "§eDESERTO";
            case "JUNGLE": return "§2JUNGLE";
            case "FLORESTA": return "§aFLORESTA";
            case "NETHER": return "§cNETHER";
            case "CAVERNA": return "§8CAVERNA";
            case "GELO": return "§bGELO";
            case "PLANICIE": return "§2PLANICIE";
            case "CHECKPOINT1": return "§8CHECKPOINT 1";
            case "CHECKPOINT2": return "§7CHECKPOINT 2";
            case "CHECKPOINT3": return "§6CHECKPOINT 3";
            case "CHECKPOINT4": return "§bCHECKPOINT 4";
            default: return "§f" + checkpoint.toUpperCase();
        }
    }

    private static List<String> getCheckpointsParaMapa(String mapa) {
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

    private static ItemStack criarItemCheckpointStats(String checkpoint, String mapa, DatabaseService.PlayerStats stats) {
        Material material;
        String nome;

        if (mapa.equals("mapa1")) {
            switch (checkpoint) {
                case "END": material = Material.COAL_BLOCK; nome = "§6END"; break;
                case "DESERTO": material = Material.GOLD_BLOCK; nome = "§eDESERTO"; break;
                case "JUNGLE": material = Material.EMERALD_BLOCK; nome = "§2JUNGLE"; break;
                case "FLORESTA": material = Material.IRON_BLOCK; nome = "§aFLORESTA"; break;
                case "NETHER": material = Material.REDSTONE_BLOCK; nome = "§cNETHER"; break;
                default: material = Material.STONE; nome = "§f" + checkpoint; break;
            }
        } else if (mapa.equals("mapa2")) {
            switch (checkpoint) {
                case "FLORESTA": material = Material.LEAVES; nome = "§aFLORESTA"; break;
                case "CAVERNA": material = Material.OBSIDIAN; nome = "§8CAVERNA"; break;
                case "GELO": material = Material.ICE; nome = "§bGELO"; break;
                case "DESERTO": material = Material.SAND; nome = "§eDESERTO"; break;
                case "NETHER": material = Material.NETHERRACK; nome = "§cNETHER"; break;
                case "PLANICIE": material = Material.GRASS; nome = "§2PLANICIE"; break;
                default: material = Material.STONE; nome = "§f" + checkpoint; break;
            }
        } else if (mapa.equals("kangaroo")) {
            switch (checkpoint) {
                case "CHECKPOINT1": material = Material.COAL_ORE; nome = "§8CHECKPOINT 1"; break;
                case "CHECKPOINT2": material = Material.IRON_ORE; nome = "§7CHECKPOINT 2"; break;
                case "CHECKPOINT3": material = Material.GOLD_ORE; nome = "§6CHECKPOINT 3"; break;
                case "CHECKPOINT4": material = Material.DIAMOND_ORE; nome = "§bCHECKPOINT 4"; break;
                default: material = Material.STONE; nome = "§f" + checkpoint; break;
            }
        } else {
            material = Material.STONE;
            nome = "§f" + checkpoint;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nome);
        
        List<String> lore = new ArrayList<>();
        
        double tempo = stats.getCheckpointTime(checkpoint);
        
        if (tempo < 999999.999) {
            lore.add("§7Melhor: §a" + String.format("%.3f", tempo) + "s");
        } else {
            lore.add("§7Melhor: §cNenhum");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static String getNomeMapa(String mapa) {
        switch (mapa) {
            case "mapa1": return "§6Mapa End";
            case "mapa2": return "§aMapa Grande";
            case "kangaroo": return "§eKangaroo";
            case "grappler": return "§aGrappler";
            default: return "§fMapa Desconhecido";
        }
    }
}
