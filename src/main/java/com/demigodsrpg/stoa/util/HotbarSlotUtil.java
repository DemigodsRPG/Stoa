package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.model.HotbarSlotModel;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.iciql.Db;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class HotbarSlotUtil {
    public static boolean isBound(Player player, int slot) {
        HotbarSlotModel alias = new HotbarSlotModel();
        Db db = StoaServer.openDb();
        try {
            return !db.from(alias).where(alias.characterId).is(CharacterUtil.currentFromPlayer(player).uuid)
                    .and(alias.slot).is(slot).select().isEmpty();
        } finally {
            db.close();
        }
    }

    public static HotbarSlotModel getBound(Player player, int slot) {
        HotbarSlotModel alias = new HotbarSlotModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.characterId).is(CharacterUtil.currentFromPlayer(player).uuid)
                    .and(alias.slot).is(slot).selectFirst();
        } finally {
            db.close();
        }
    }

    public static boolean canMove(Player player, int slot) {
        HotbarSlotModel model = getBound(player, slot);
        return model.getDivineItem().getFlags().contains(DivineItem.Flag.MOVEABLE);
    }

    public static Collection<HotbarSlotModel> getCurrentBound(DivineItem item) {
        return Collections2.filter(getAllBound(item), new Predicate<HotbarSlotModel>() {
            @Override
            public boolean apply(HotbarSlotModel hotbarSlotModel) {
                return CharacterUtil.fromId(hotbarSlotModel.getCharacterId()).getActive();
            }
        });
    }

    public static List<HotbarSlotModel> getAllBound(DivineItem item) {
        HotbarSlotModel alias = new HotbarSlotModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.itemId).is(item.getId()).select();
        } finally {
            db.close();
        }
    }
}
