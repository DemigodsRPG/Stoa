package com.demigodsrpg.stoa.battle;

import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public interface Participant
{
	UUID getId();

	boolean canPvp();

	Location getCurrentLocation();

	StoaCharacter getRelatedCharacter();

	LivingEntity getEntity();
}
