package com.demigodsrpg.stoa.data;

import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.StructureModel;

import javax.annotation.Nullable;

public interface InteractFunction<T> {
    T apply(@Nullable StructureModel data, @Nullable CharacterModel character);
}