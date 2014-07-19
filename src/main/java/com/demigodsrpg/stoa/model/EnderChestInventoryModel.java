package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.util.InventoryUtil;
import com.iciql.Iciql;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Iciql.IQTable(name = "dg_ender_inventory")
public class EnderChestInventoryModel
{
    @Iciql.IQColumn(primaryKey = true)
    public String ownerId;
    @Iciql.IQColumn
    public Boolean empty = true;
    @Iciql.IQColumn
    private String items;

    public EnderChestInventoryModel() {
    }

    public EnderChestInventoryModel(String ownerId) {
        this.ownerId = ownerId;
    }

    public EnderChestInventoryModel(Player player, String ownerId) {
        Inventory inventory = player.getEnderChest();
        items = InventoryUtil.serializeItemStacks(inventory.getContents());
        this.ownerId = ownerId;
        empty = false;
    }

    public void setContents(ItemStack[] items) {
        this.items = InventoryUtil.serializeItemStacks(items);
    }

    public void setToPlayer(Player player) {
        ItemStack[] items = InventoryUtil.deserializeItemStacks(this.items);
        Inventory inventory = player.getEnderChest();
        inventory.setContents(items);
    }
}
