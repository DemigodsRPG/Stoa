package com.demigodsrpg.stoa.battle;

import com.demigodsrpg.stoa.controller.CharacterController;
import com.demigodsrpg.stoa.model.Model;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface Participant<T extends Model>
{
	boolean hasCharacter();

	boolean canPvp();

	Location getCurrentLocation();

	LivingEntity getEntity();

	CharacterController getCharacter();

	T getModel();
}
