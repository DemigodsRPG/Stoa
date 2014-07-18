package com.demigodsrpg.stoa.data;

import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.model.StoaStructureModel;

import javax.annotation.Nullable;

public interface InteractFunction<T> {
    T apply(@Nullable StoaStructureModel data, @Nullable StoaCharacter character);
}