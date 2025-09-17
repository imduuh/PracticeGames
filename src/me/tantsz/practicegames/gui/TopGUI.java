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

public class TopGUI {

    private static ItemStack criarItemDecorativo(Material material, String nome) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nome);
        item.setItemMeta(meta);
        return item;
    }

    public static void abrirEscolhaMapa(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "TOP RANKINGS");

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
        lore1.add("§eClique para ver rankings");
        meta1.setLore(lore1);
        mapa1.setItemMeta(meta1);

        ItemStack mapa2 = new ItemStack(Material.GRASS);
        ItemMeta meta2 = mapa2.getItemMeta();
        meta2.setDisplayName("§aMAPA GRANDE");
        List<String> lore2 = new ArrayList<>();
        lore2.add("§7Checkpoints: 6");
        lore2.add("");
        lore2.add("§eClique para ver rankings");
        meta2.setLore(lore2);
        mapa2.setItemMeta(meta2);

        ItemStack kangaroo = new ItemStack(Material.FIREWORK);
        ItemMeta metaKangaroo = kangaroo.getItemMeta();
        metaKangaroo.setDisplayName("§eKANGAROO");
        List<String> loreKangaroo = new ArrayList<>();
        loreKangaroo.add("§7Checkpoints: 4");
        loreKangaroo.add("");
        loreKangaroo.add("§eClique para ver rankings");
        metaKangaroo.setLore(loreKangaroo);
        kangaroo.setItemMeta(metaKangaroo);

        ItemStack grappler = new ItemStack(Material.LEASH);
        ItemMeta metaGrappler = grappler.getItemMeta();
        metaGrappler.setDisplayName("§aGRAPPLER");
        List<String> loreGrappler = new ArrayList<>();
        loreGrappler.add("§7Checkpoint: 1 (Final)");
        loreGrappler.add("");
        loreGrappler.add("§eClique para ver rankings");
        metaGrappler.setLore(loreGrappler);
        grappler.setItemMeta(metaGrappler);

        inv.setItem(10, mapa1);
        inv.setItem(12, mapa2); 
        inv.setItem(14, kangaroo);
        inv.setItem(16, grappler);

        ItemStack estrela = new ItemStack(Material.NETHER_STAR);
        ItemMeta metaEstrela = estrela.getItemMeta();
        metaEstrela.setDisplayName("§eTOP RANKINGS");
        List<String> loreEstrela = new ArrayList<>();
        loreEstrela.add("§7Escolha um modo para ver");
        loreEstrela.add("§7os melhores tempos");
        metaEstrela.setLore(loreEstrela);
        estrela.setItemMeta(metaEstrela);
        inv.setItem(4, estrela);

        ItemStack fechar = new ItemStack(Material.BARRIER);
        ItemMeta metaFechar = fechar.getItemMeta();
        metaFechar.setDisplayName("§cFECHAR");
        fechar.setItemMeta(metaFechar);
        inv.setItem(22, fechar);

        player.openInventory(inv);
    }

    public static void abrirRankingMapa(Player player, String mapa) {
        String nomeMapa = getNomeMapa(mapa);
        Inventory inv = Bukkit.createInventory(null, 27, "TOP " + nomeMapa.replaceAll("§.", ""));

        ItemStack vidro = criarItemDecorativo(Material.STAINED_GLASS_PANE, " ");
        vidro.setDurability((short) 7);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, vidro);
            inv.setItem(18 + i, vidro);
        }
        inv.setItem(9, vidro);
        inv.setItem(17, vidro);

        if (mapa.equals("grappler")) {
            mostrarRanking(player, "TOTAL", mapa);
            return;
        } else {
            List<String> checkpoints = getCheckpointsParaMapa(mapa);

            ItemStack total = new ItemStack(Material.DIAMOND);
            ItemMeta metaTotal = total.getItemMeta();
            metaTotal.setDisplayName("§bTEMPO TOTAL");
            List<String> loreTotal = new ArrayList<>();
            loreTotal.add("§7Ranking Completo");
            loreTotal.add("§7Melhores Tempos");
            loreTotal.add("");
            loreTotal.add("§eClique para ver");
            metaTotal.setLore(loreTotal);
            total.setItemMeta(metaTotal);
            inv.setItem(4, total);

            ItemStack tempoTeorico = criarTempoTeoricoPerfeito(mapa);
            inv.setItem(13, tempoTeorico);

            int[] slots = {10, 11, 12, 14, 15, 16};
            for (int i = 0; i < Math.min(checkpoints.size(), slots.length); i++) {
                String checkpoint = checkpoints.get(i);
                ItemStack item = criarItemCheckpoint(checkpoint, mapa);
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

    private static ItemStack criarTempoTeoricoPerfeito(String mapa) {
        DatabaseService dbService = PracticeGames.getInstance().getDatabaseService();
        List<DatabaseService.CheckpointRecord> records = dbService.buscarMelhoresTemposPorCheckpoint(mapa);

        double tempoTotal = 0.0;
        List<String> detalhes = new ArrayList<>();
        List<String> checkpoints = getCheckpointsParaMapa(mapa);

        for (String checkpoint : checkpoints) {
            boolean encontrado = false;
            for (DatabaseService.CheckpointRecord record : records) {
                if (record.getCheckpoint().equals(checkpoint)) {
                    tempoTotal += record.getTempo();
                    detalhes.add("§7" + getNomeCheckpointFormatado(checkpoint).replaceAll("§.", "") + ": §a" +
                            String.format("%.3f", record.getTempo()) + "s §7(" + record.getPlayerName() + ")");
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                detalhes.add("§7" + getNomeCheckpointFormatado(checkpoint).replaceAll("§.", "") + ": §cSem tempo");
            }
        }

        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aTEMPO TEORICO PERFEITO");

        List<String> lore = new ArrayList<>();
        lore.add("§7Soma dos melhores tempos");
        lore.add("§7de cada checkpoint");
        lore.add("");

        if (records.size() > 0) {
            lore.add("§eTotal: §b" + String.format("%.3f", tempoTotal) + "s");
            lore.add("§7Checkpoints: §a" + records.size() + "§7/§e" + checkpoints.size());
        } else {
            lore.add("§cNenhum tempo disponível");
        }

        lore.add("");
        lore.addAll(detalhes);

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public static void mostrarRanking(Player player, String tipo, String mapa) {
        String nomeMapa = getNomeMapa(mapa);
        String nomeCheckpoint = getNomeCheckpointFormatado(tipo);

        Inventory inv = Bukkit.createInventory(null, 27, nomeCheckpoint.replaceAll("§.", "") + " - " + nomeMapa.replaceAll("§.", ""));

        ItemStack vidro = criarItemDecorativo(Material.STAINED_GLASS_PANE, " ");
        vidro.setDurability((short) 7);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, vidro);
            inv.setItem(18 + i, vidro);
        }
        inv.setItem(9, vidro);
        inv.setItem(17, vidro);

        DatabaseService dbService = PracticeGames.getInstance().getDatabaseService();
        List<DatabaseService.RankingRecord> ranking = dbService.buscarTop10(tipo.trim().toUpperCase(), mapa);
        DatabaseService.PlayerPosition playerPos = dbService.buscarPosicaoJogador(player.getName(), tipo.trim().toUpperCase(), mapa);

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < Math.min(ranking.size(), 7); i++) {
            DatabaseService.RankingRecord record = ranking.get(i);

            if (record.getTempo() >= 999999.999) continue;

            ItemStack item = criarCabecaComSkin(record.getPlayerName());
            ItemMeta meta = item.getItemMeta();

            String posicao = "§" + getPosicaoCor(i + 1) + "#" + (i + 1);
            meta.setDisplayName(posicao + " §f" + record.getPlayerName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Tempo: §e" + String.format("%.3f", record.getTempo()) + "s");
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slots[i], item);
        }

        if (playerPos != null) {
            ItemStack posicaoItem = new ItemStack(Material.PAPER);
            ItemMeta metaPosicao = posicaoItem.getItemMeta();
            metaPosicao.setDisplayName("§eSUA POSICAO");
            List<String> lorePosicao = new ArrayList<>();
            lorePosicao.add("§7Posição: §a#" + playerPos.getPosicao());
            lorePosicao.add("§7Seu tempo: §b" + String.format("%.3f", playerPos.getTempo()) + "s");
            metaPosicao.setLore(lorePosicao);
            posicaoItem.setItemMeta(metaPosicao);
            inv.setItem(4, posicaoItem);
        } else {
            ItemStack semTempo = new ItemStack(Material.BARRIER);
            ItemMeta metaSemTempo = semTempo.getItemMeta();
            metaSemTempo.setDisplayName("§cSEM TEMPO");
            List<String> loreSemTempo = new ArrayList<>();
            loreSemTempo.add("§7Você ainda não tem");
            loreSemTempo.add("§7tempo neste checkpoint");
            metaSemTempo.setLore(loreSemTempo);
            semTempo.setItemMeta(metaSemTempo);
            inv.setItem(4, semTempo);
        }

        if (ranking.isEmpty()) {
            ItemStack semDados = new ItemStack(Material.BARRIER);
            ItemMeta meta = semDados.getItemMeta();
            meta.setDisplayName("§cNENHUM DADO ENCONTRADO");
            List<String> lore = new ArrayList<>();
            lore.add("§7Nenhum registro encontrado");
            lore.add("§7para este checkpoint");
            meta.setLore(lore);
            semDados.setItemMeta(meta);
            inv.setItem(13, semDados);
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
        } else if (mapa.equals("grappler")) {
            checkpoints.add("TOTAL");
        }

        return checkpoints;
    }

    private static ItemStack criarItemCheckpoint(String checkpoint, String mapa) {
        Material material;
        String nome;

        if (mapa.equals("mapa1")) {
            switch (checkpoint) {
                case "END":
                    material = Material.COAL_BLOCK;
                    nome = "§6END";
                    break;
                case "DESERTO":
                    material = Material.GOLD_BLOCK;
                    nome = "§eDESERTO";
                    break;
                case "JUNGLE":
                    material = Material.EMERALD_BLOCK;
                    nome = "§2JUNGLE";
                    break;
                case "FLORESTA":
                    material = Material.IRON_BLOCK;
                    nome = "§aFLORESTA";
                    break;
                case "NETHER":
                    material = Material.REDSTONE_BLOCK;
                    nome = "§cNETHER";
                    break;
                default:
                    material = Material.STONE;
                    nome = "§f" + checkpoint;
                    break;
            }
        } else if (mapa.equals("mapa2")) {
            switch (checkpoint) {
                case "FLORESTA":
                    material = Material.LEAVES;
                    nome = "§aFLORESTA";
                    break;
                case "CAVERNA":
                    material = Material.OBSIDIAN;
                    nome = "§8CAVERNA";
                    break;
                case "GELO":
                    material = Material.ICE;
                    nome = "§bGELO";
                    break;
                case "DESERTO":
                    material = Material.SAND;
                    nome = "§eDESERTO";
                    break;
                case "NETHER":
                    material = Material.NETHERRACK;
                    nome = "§cNETHER";
                    break;
                case "PLANICIE":
                    material = Material.GRASS;
                    nome = "§2PLANICIE";
                    break;
                default:
                    material = Material.STONE;
                    nome = "§f" + checkpoint;
                    break;
            }
        } else if (mapa.equals("kangaroo")) {
            switch (checkpoint) {
                case "CHECKPOINT1":
                    material = Material.COAL_ORE;
                    nome = "§8CHECKPOINT 1";
                    break;
                case "CHECKPOINT2":
                    material = Material.IRON_ORE;
                    nome = "§7CHECKPOINT 2";
                    break;
                case "CHECKPOINT3":
                    material = Material.GOLD_ORE;
                    nome = "§6CHECKPOINT 3";
                    break;
                case "CHECKPOINT4":
                    material = Material.DIAMOND_ORE;
                    nome = "§bCHECKPOINT 4";
                    break;
                default:
                    material = Material.STONE;
                    nome = "§f" + checkpoint;
                    break;
            }
        } else {
            material = Material.STONE;
            nome = "§f" + checkpoint;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nome);
        List<String> lore = new ArrayList<>();
        lore.add("§7Ranking Individual");
        lore.add("");
        lore.add("§eClique para ver ranking");
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static String getNomeCheckpointFormatado(String tipo) {
        switch (tipo.toUpperCase()) {
            case "TOTAL":
                return "§bTOTAL";
            case "END":
                return "§6END";
            case "DESERTO":
                return "§eDESERTO";
            case "JUNGLE":
                return "§2JUNGLE";
            case "FLORESTA":
                return "§aFLORESTA";
            case "NETHER":
                return "§cNETHER";
            case "CAVERNA":
                return "§8CAVERNA";
            case "GELO":
                return "§bGELO";
            case "PLANICIE":
                return "§2PLANICIE";
            case "CHECKPOINT1":
                return "§8CHECKPOINT 1";
            case "CHECKPOINT2":
                return "§7CHECKPOINT 2";
            case "CHECKPOINT3":
                return "§6CHECKPOINT 3";
            case "CHECKPOINT4":
                return "§bCHECKPOINT 4";
            default:
                return "§f" + tipo.toUpperCase();
        }
    }

    private static String getPosicaoCor(int posicao) {
        switch (posicao) {
            case 1:
                return "6";
            case 2:
                return "7";
            case 3:
                return "c";
            default:
                return "f";
        }
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

    private static String getNomeMapa(String mapa) {
        switch (mapa) {
            case "mapa1":
                return "§6Mapa End";
            case "mapa2":
                return "§aMapa Grande";
            case "kangaroo":
                return "§eKangaroo";
            case "grappler":
                return "§aGrappler";
            default:
                return "§fMapa Desconhecido";
        }
    }
}
