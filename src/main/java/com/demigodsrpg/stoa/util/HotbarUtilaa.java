package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.model.HotbarSlotModelaa;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.iciql.Db;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class HotbarUtilaa {
    public static boolean isBound(Player player, int slot) {
        HotbarSlotModelaa alias = new HotbarSlotModelaa();
        Db db = StoaServer.openDb();
        try {
            return !db.from(alias).where(alias.characterId).is(CharacterUtil.currentFromPlayer(player).uuid)
                    .and(alias.slot).is(slot).select().isEmpty();
        } finally {
            db.close();
        }
    }

    public static HotbarSlotModelaa getBound(Player player, int slot) {
        HotbarSlotModelaa alias = new HotbarSlotModelaa();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.characterId).is(CharacterUtil.currentFromPlayer(player).uuid)
                    .and(alias.slot).is(slot).selectFirst();
        } finally {
            db.close();
        }
    }

    public static boolean canMove(Player player, int slot) {
        HotbarSlotModelaa model = getBound(player, slot);
        return model.getDivineItem().getFlags().contains(DivineItem.Flag.MOVEABLE);
    }

    public static Collection<HotbarSlotModelaa> getCurrentBound(DivineItem item) {
        return Collections2.filter(getAllBound(item), new Predicate<HotbarSlotModelaa>() {
            @Override
            public boolean apply(HotbarSlotModelaa hotbarSlotModel) {
                return CharacterUtil.fromId(hotbarSlotModel.getCharacterId()).getActive();
            }
        });
    }

    public static List<HotbarSlotModelaa> getAllBound(DivineItem item) {
        HotbarSlotModelaa alias = new HotbarSlotModelaa();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.itemId).is(item.getId()).select();
        } finally {
            db.close();
        }
    }
}
