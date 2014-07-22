package com.demigodsrpg.stoa.item;

import com.demigodsrpg.stoa.deity.Ability;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Set;

public interface DivineItem {
    @Override
    String toString();

    String getName();

    String getId();

    String getDescription();

    Set<Flag> getFlags();

    Type getType();

    ItemStack getItem();

    Recipe getRecipe();

    int getMaxUseCount();

    Ability getAbility();

    public enum Flag {
        UNENCHANTABLE, MOVEABLE
    }

    public enum Type {
        ABILITY, PASSIVE, CONSUMABLE
    }
}
