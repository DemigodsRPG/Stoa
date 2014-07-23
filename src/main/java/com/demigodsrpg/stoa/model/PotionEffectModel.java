package com.demigodsrpg.stoa.model;

import com.censoredsoftware.library.util.PotionEffectUtil;
import com.iciql.Iciql;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.List;

@Iciql.IQTable(name = "dg_potion_effects")
public class PotionEffectModel {
    @Iciql.IQColumn(primaryKey = true)
    public String id;
    @Iciql.IQColumn
    public String data;

    public PotionEffectModel() {
    }

    public PotionEffectModel(String id, Collection<PotionEffect> effects) {
        this.id = id;
        data = PotionEffectUtil.serializePotionEffects(effects);
    }

    public String getId() {
        return id;
    }

    /**
     * Returns a built PotionEffect.
     */
    public List<PotionEffect> getPotionEffects() {
        return PotionEffectUtil.deserializePotionEffects(data);
    }
}
