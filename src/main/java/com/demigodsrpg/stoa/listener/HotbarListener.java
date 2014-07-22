package com.demigodsrpg.stoa.listener;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.model.HotbarSlotModel;
import com.demigodsrpg.stoa.util.CharacterUtil;
import com.demigodsrpg.stoa.util.HotbarUtilaaa;
import com.demigodsrpg.stoa.util.PlayerUtil;
import com.demigodsrpg.stoa.util.ZoneUtil;
import com.iciql.Db;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class HotbarListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void preventDropEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (PlayerUtil.fromPlayer(player).hasCharacter() && !ZoneUtil.inNoStoaZone(player.getLocation())) {
            int slot = player.getInventory().getHeldItemSlot();
            if (HotbarUtilaaa.isBound(player, slot) && !HotbarUtilaaa.canMove(player, slot)) {
                HotbarSlotModel bound = HotbarUtilaaa.getBound(player, slot);
                if (DivineItem.Type.ABILITY.equals(bound.getType())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!ZoneUtil.inNoStoaZone(player.getLocation())) {
            if (Stoa.getItemRegistry().isDivineItemType(event.getItem().getItemStack())) {
                if (!PlayerUtil.fromPlayer(player).hasCharacter()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // TODO Put divine items into a special inventory to prevent players from screwing with them.
    // They should only be able to send them from their inventory to the hotbar, and vice versa.

    @EventHandler(priority = EventPriority.LOWEST)
    public void preventMoveEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (PlayerUtil.fromPlayer(player).hasCharacter() && !ZoneUtil.inNoStoaZone(player.getLocation())) {
            if (InventoryType.SlotType.QUICKBAR.equals(event.getSlotType())) {
                if (HotbarUtilaaa.isBound(player, event.getSlot()) && !HotbarUtilaaa.canMove(player, event.getSlot())) {
                    event.setCancelled(true);
                    switch (event.getClick()) {
                        case LEFT:
                        case SHIFT_LEFT:
                            break;
                        case RIGHT:
                        case SHIFT_RIGHT:
                        case DOUBLE_CLICK:
                            break;
                    }
                } else {
                    DivineItem item = Stoa.getItemRegistry().getDivineItemType(event.getCurrentItem(), DivineItem.Type.PASSIVE, DivineItem.Type.CONSUMABLE);
                    if (item != null) {
                        Db db = StoaServer.openDb();
                        HotbarSlotModel model = new HotbarSlotModel();
                        model.slot = event.getSlot();
                        model.itemId = item.getId();
                        model.type = item.getType();
                        model.characterId = CharacterUtil.currentFromPlayer(player).getId();
                        db.insert(model);
                        db.close();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void whenClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (PlayerUtil.fromPlayer(player).hasCharacter() && !ZoneUtil.inNoStoaZone(player.getLocation())) {
            switch (event.getAction()) {
                case RIGHT_CLICK_BLOCK:
                case RIGHT_CLICK_AIR:
                    int slot = player.getInventory().getHeldItemSlot();
                    if (HotbarUtilaaa.isBound(player, slot)) {
                        event.setCancelled(true);
                        HotbarSlotModel model = HotbarUtilaaa.getBound(player, slot);
                        if (model.use()) {
                            Db db = StoaServer.openDb();
                            model.useCount++;
                            if (DivineItem.Type.CONSUMABLE.equals(model.getType()) && model.getUsesLeft() < 1) {
                                db.delete(model);
                                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                            } else {
                                db.update(model);
                            }
                            db.close();
                        }
                    }
            }
        }
    }
}
