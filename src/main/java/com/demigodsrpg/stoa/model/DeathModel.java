package com.demigodsrpg.stoa.model;

import com.censoredsoftware.shaded.com.iciql.Iciql;
import com.demigodsrpg.stoa.battle.Participant;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Iciql.IQTable(name = "deaths")
public class DeathModel implements Model
{
	// -- DEFAULT CONSTRUCTOR -- //
	public DeathModel()
	{
	}

	// -- PRACTICAL STATIC CONSTRUCTOR -- //
	public static DeathModel from(Participant victim, Optional<Participant> killer)
	{
		DeathModel model = new DeathModel();

		// Set data and foreign keys
		model.deathTime = Timestamp.from(Instant.now());
		model.victimId = victim.getCharacter().id();

		// Optional data
		if(killer.isPresent())
		{
			model.killerType = killer.get().getModel().name();
			model.killerId = killer.get().getModel().id();
		}

		return model;
	}

	// -- MODEL META -- //
	@Iciql.IQColumn(primaryKey = true)
	public String id = UUID.randomUUID().toString();

	// -- DATA -- //
	@Iciql.IQColumn
	public Timestamp deathTime;
	@Iciql.IQColumn
	public String killerType;

	// -- FOREIGN DATA -- //
	@Iciql.IQColumn
	public String victimId;
	@Iciql.IQColumn
	public String killerId;

	// -- INTERFACE METHODS -- //
	@Override
	public String id()
	{
		return id;
	}

	@Override
	public String name()
	{
		return "DEATH";
	}
}
