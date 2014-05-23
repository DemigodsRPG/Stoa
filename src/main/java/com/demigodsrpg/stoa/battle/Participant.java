package com.demigodsrpg.stoa.battle;

import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.Model;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface Participant<T extends Model>
{
	T getModel();

	CharacterModel getCharacter();

	Boolean canPvp();

	Location getCurrentLocation();

	LivingEntity getEntity();
}
