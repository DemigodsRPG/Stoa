package com.demigodsrpg.stoa.entity.player.attribute;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.data.DataAccess;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class Death extends DataAccess<UUID, Death>
{
	private UUID id;
	private long deathTime;
	private UUID killed, attacking;

	private Death()
	{
	}

	public static Death create(Participant killed)
	{
		Death death = new Death();
		death.deathTime = System.currentTimeMillis();
		death.id = UUID.randomUUID();
		death.killed = killed.getRelatedCharacter().getId();
		death.save();
		return death;
	}

	public static Death create(Participant killed, Participant attacking)
	{
		Death death = new Death();
		death.deathTime = System.currentTimeMillis();
		death.id = UUID.randomUUID();
		death.killed = killed.getRelatedCharacter().getId();
		death.attacking = attacking.getRelatedCharacter().getId();
		death.save();
		return death;
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static Death of(UUID id, ConfigurationSection conf)
	{
		Death death = new Death();
		death.id = id;
		death.deathTime = conf.getLong("deathTime");
		death.killed = UUID.fromString(conf.getString("killed"));
		if(conf.isString("attacking")) death.attacking = UUID.fromString(conf.getString("attacking"));
		return death;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("deathTime", deathTime);
		map.put("killed", killed.toString());
		if(attacking != null) map.put("attacking", attacking.toString());
		return map;
	}

	public UUID getId()
	{
		return id;
	}

	public long getDeathTime()
	{
		return deathTime;
	}

	public UUID getKilled()
	{
		return killed;
	}

	public UUID getAttacking()
	{
		return attacking;
	}

	private static final DataAccess<UUID, Death> DATA_ACCESS = new Death();

	public static Death get(UUID id)
	{
		return DATA_ACCESS.getDirect(id);
	}

	public static Collection<Death> all()
	{
		return DATA_ACCESS.allDirect();
	}

	public static Set<Death> getRecentDeaths(int seconds)
	{
		final long time = System.currentTimeMillis() - (seconds * 1000);
		return Sets.newHashSet(Iterables.filter(Iterables.concat(Collections2.transform(Stoa.getOnlineCharacters(), new Function<StoaCharacter, Collection<Death>>()
		{
			@Override
			public Collection<Death> apply(StoaCharacter character)
			{
				try
				{
					return character.getDeaths();
				}
				catch(java.lang.Exception ignored)
				{
				}
				return null;
			}
		})), new Predicate<Death>()
		{
			@Override
			public boolean apply(Death death)
			{
				return death.getDeathTime() >= time;
			}
		}));
	}

	public static Collection<Death> getRecentDeaths(StoaCharacter character, int seconds)
	{
		final long time = System.currentTimeMillis() - (seconds * 1000);
		return Collections2.filter(character.getDeaths(), new Predicate<Death>()
		{
			@Override
			public boolean apply(Death death)
			{
				return death.getDeathTime() >= time;
			}
		});
	}
}
