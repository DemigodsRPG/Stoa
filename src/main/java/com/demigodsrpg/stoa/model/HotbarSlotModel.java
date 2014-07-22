package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.util.CharacterUtil;
import com.iciql.Iciql;
import org.bukkit.inventory.ItemStack;

@Iciql.IQTable(name = "dg_hotbarslots")
public class HotbarSlotModel {
    @Iciql.IQColumn(primaryKey = true, autoIncrement = true)
    public Long id;
    @Iciql.IQColumn(name = "character_id")
    public String characterId;
    @Iciql.IQColumn
    public Integer slot;
    @Iciql.IQEnum
    @Iciql.IQColumn
    public DivineItem.Type type;
    @Iciql.IQColumn(name = "item_id")
    public String itemId;
    @Iciql.IQColumn(name = "use_count")
    public Integer useCount = 0;

    public boolean use() {
        return getDivineItem().getAbility().use(CharacterUtil.fromId(getCharacterId()));
    }

    public String getCharacterId() {
        return characterId;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isType(DivineItem.Type type) {
        return this.type.equals(type);
    }

    public DivineItem.Type getType() {
        return type;
    }

    public Ability getAbility() {
        return getDivineItem().getAbility();
    }

    public ItemStack getItem() {
        return getDivineItem().getItem();
    }

    public int getUsesLeft() {
        return getDivineItem().getMaxUseCount() - useCount;
    }

    public DivineItem getDivineItem() {
        return Stoa.getItemRegistry().getItem(itemId, type);
    }
}
