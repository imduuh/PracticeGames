package me.tantsz.practicegames.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    
    private final ItemStack item;
    
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }
    
    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
    }
    
    public ItemBuilder(Material material, int amount, short data) {
        this.item = new ItemStack(material, amount, data);
    }
    
    public ItemBuilder setDisplayName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return this;
    }
    
    public ItemBuilder setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }
    
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return this;
    }
    
    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }
    
    public ItemBuilder setDurability(short durability) {
        item.setDurability(durability);
        return this;
    }
    
    public ItemStack build() {
        return item.clone();
    }
}
