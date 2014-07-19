package com.demigodsrpg.stoa.data;

import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.model.StructureModel;

import javax.annotation.Nullable;

public interface InteractFunction<T> {
    T apply(@Nullable StructureModel data, @Nullable StoaCharacter character);
}