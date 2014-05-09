package com.demigodsrpg.stoa.entity.player.attribute;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.demigodsrpg.stoa.data.DataAccess;
import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;

public class StoaPotionEffect extends DataAccess<UUID, StoaPotionEffect>
{
	private UUID id;
	private String type;
	private int duration;
	private int amplifier;
	private boolean ambience;

	private StoaPotionEffect()
	{
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static StoaPotionEffect of(UUID id, ConfigurationSection conf)
	{
		StoaPotionEffect effect = new StoaPotionEffect();
		effect.id = id;
		effect.type = conf.getString("type");
		effect.duration = conf.getInt("duration");
		effect.amplifier = conf.getInt("amplifier");
		effect.ambience = conf.getBoolean("ambience");
		return effect;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = Maps.newHashMap();
		map.put("type", type);
		map.put("duration", duration);
		map.put("amplifier", amplifier);
		map.put("ambience", ambience);
		return map;
	}

	public UUID getId()
	{
		return id;
	}

	public PotionEffectType getType()
	{
		return PotionEffectType.getByName(type);
	}

	public int getDuration()
	{
		return duration;
	}

	public int getAmplifier()
	{
		return amplifier;
	}

	public boolean isAmbient()
	{
		return ambience;
	}

	private static final DataAccess<UUID, StoaPotionEffect> DATA_ACCESS = new StoaPotionEffect();

	public static StoaPotionEffect get(UUID id)
	{
		return DATA_ACCESS.getDirect(id);
	}

	public static StoaPotionEffect of(PotionEffect potion)
	{
		StoaPotionEffect effect = new StoaPotionEffect();
		effect.id = UUID.randomUUID();
		effect.type = potion.getType().getName();
		effect.duration = potion.getDuration();
		effect.amplifier = potion.getAmplifier();
		effect.ambience = potion.isAmbient();
		effect.save();
		return effect;
	}

	/**
	 * Returns a built PotionEffect.
	 */
	public PotionEffect getBukkitPotionEffect()
	{
		return new PotionEffect(getType(), getDuration(), getAmplifier(), isAmbient());
	}
}
