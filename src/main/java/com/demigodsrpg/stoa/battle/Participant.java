package com.demigodsrpg.stoa.battle;

import com.demigodsrpg.stoa.model.CharacterModel;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;

public interface Participant {
    boolean canPvp();

    Location getCurrentLocation();

    LivingEntity getEntity();

    boolean hasCharacter();

    String getId();

    CharacterModel getCharacter();

    OfflinePlayer getOfflinePlayer();
}
