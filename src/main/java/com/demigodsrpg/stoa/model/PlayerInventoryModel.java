package com.demigodsrpg.stoa.model;


import com.censoredsoftware.library.util.ItemUtil;
import com.iciql.Iciql;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Iciql.IQTable(name = "dg_inventory")
public class PlayerInventoryModel {
    @Iciql.IQColumn(primaryKey = true)
    public String ownerId;
    @Iciql.IQColumn
    public Boolean empty = true;
    @Iciql.IQColumn
    private String armor;
    @Iciql.IQColumn
    private String items;

    public PlayerInventoryModel() {
    }

    public PlayerInventoryModel(String ownerId) {
        this.ownerId = ownerId;
    }

    public PlayerInventoryModel(Player player, String ownerId) {
        PlayerInventory inventory = player.getInventory();
        armor = ItemUtil.serializeItemStacks(inventory.getArmorContents());
        items = ItemUtil.serializeItemStacks(inventory.getContents());
        empty = false;
        this.ownerId = ownerId;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = ItemUtil.serializeItemStacks(armor);
    }

    public void setContents(ItemStack[] items) {
        this.items = ItemUtil.serializeItemStacks(items);
    }

    public void setToPlayer(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armor = ItemUtil.deserializeItemStacks(this.armor);
        ItemStack[] items = ItemUtil.deserializeItemStacks(this.items);
        inventory.setArmorContents(armor);
        inventory.setContents(items);
    }
}
