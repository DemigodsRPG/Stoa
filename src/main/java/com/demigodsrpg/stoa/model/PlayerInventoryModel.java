package com.demigodsrpg.stoa.model;


import com.demigodsrpg.stoa.util.InventoryUtil;
import com.iciql.Iciql;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Iciql.IQTable(name = "dg_inventory")
public class PlayerInventoryModel
{
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
        armor = InventoryUtil.serializeItemStacks(inventory.getArmorContents());
        items = InventoryUtil.serializeItemStacks(inventory.getContents());
        empty = false;
        this.ownerId = ownerId;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = InventoryUtil.serializeItemStacks(armor);
    }

    public void setContents(ItemStack[] items) {
        this.items = InventoryUtil.serializeItemStacks(items);
    }

    public void setToPlayer(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armor = InventoryUtil.deserializeItemStacks(this.armor);
        ItemStack[] items = InventoryUtil.deserializeItemStacks(this.items);
        inventory.setArmorContents(armor);
        inventory.setContents(items);
    }
}
