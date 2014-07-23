package com.demigodsrpg.stoa.item;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemRegistry {
    public final static BiMap<String, DivineItem> ABILITY_MAP = HashBiMap.create();
    BiMap<String, DivineItem> PASSIVE_MAP = HashBiMap.create();
    BiMap<String, DivineItem> CONSUMABLE_MAP = HashBiMap.create();

    public boolean addItem(DivineItem item) {
        String id = item.getId();
        switch (item.getType()) {
            case ABILITY:
                ABILITY_MAP.put(id, item);
                break;
            case PASSIVE:
                PASSIVE_MAP.put(id, item);
                break;
            case CONSUMABLE:
                CONSUMABLE_MAP.put(id, item);
                break;
            default:
                return false;
        }
        return true;
    }

    public void removeItem(String id) {
        ABILITY_MAP.remove(id);
        PASSIVE_MAP.remove(id);
        CONSUMABLE_MAP.remove(id);
    }

    public void removeItem(DivineItem item) {
        ABILITY_MAP.inverse().remove(item);
        PASSIVE_MAP.inverse().remove(item);
        CONSUMABLE_MAP.inverse().remove(item);
    }

    public DivineItem getItem(String id, DivineItem.Type type) {
        switch (type) {
            case ABILITY:
                return ABILITY_MAP.get(id);
            case PASSIVE:
                return PASSIVE_MAP.get(id);
            case CONSUMABLE:
                return CONSUMABLE_MAP.get(id);
        }
        return null;
    }

    public boolean isDivineItemType(final ItemStack item, DivineItem.Type... search) {
        if (search.length == 0) {
            search = new DivineItem.Type[]{DivineItem.Type.ABILITY, DivineItem.Type.PASSIVE, DivineItem.Type.CONSUMABLE};
        }
        return Iterables.any(Arrays.asList(search), new Predicate<DivineItem.Type>() {
            @Override
            public boolean apply(DivineItem.Type type) {
                switch (type) {
                    case ABILITY:
                        return Iterables.any(ABILITY_MAP.values(), new DivinePredicate(item));
                    case PASSIVE:
                        return Iterables.any(PASSIVE_MAP.values(), new DivinePredicate(item));
                    case CONSUMABLE:
                        return Iterables.any(CONSUMABLE_MAP.values(), new DivinePredicate(item));
                }
                return false;
            }
        });
    }

    public DivineItem getDivineItemType(final ItemStack item, DivineItem.Type... search) {
        if (search.length == 0) {
            search = new DivineItem.Type[]{DivineItem.Type.ABILITY, DivineItem.Type.PASSIVE, DivineItem.Type.CONSUMABLE};
        }
        for (DivineItem.Type type : search) {
            DivineItem found;
            switch (type) {
                case ABILITY:
                    found = Iterables.find(ABILITY_MAP.values(), new DivinePredicate(item), null);
                    if (found != null) return found;
                case PASSIVE:
                    found = Iterables.find(PASSIVE_MAP.values(), new DivinePredicate(item), null);
                    if (found != null) return found;
                case CONSUMABLE:
                    found = Iterables.find(CONSUMABLE_MAP.values(), new DivinePredicate(item), null);
                    if (found != null) return found;
            }
        }
        return null;
    }

    public boolean itemHasFlag(ItemStack itemStack, DivineItem.Flag flag) {
        return isDivineItemType(itemStack) && getDivineItemType(itemStack).getFlags().contains(flag);
    }

    private static class DivinePredicate implements Predicate<DivineItem> {
        private ItemStack checking;

        private DivinePredicate(ItemStack checking) {
            this.checking = checking;
        }

        @Override
        public boolean apply(DivineItem item) {
            return item.getItem().equals(checking);
        }
    }
}
