package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.TameableModel;
import com.iciql.Db;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class TameableUtil {
    public static List<TameableModel> findByType(final EntityType type) {
        TameableModel alias = new TameableModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.entityType).is(type).select();
        } finally {
            db.close();
        }
    }

    public static Collection<TameableModel> findByTamer(final String animalTamer) {
        TameableModel alias = new TameableModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.animalTamer).is(animalTamer).select();
        } finally {
            db.close();
        }
    }

    public static TameableModel findByUUID(final UUID uniqueId) {
        String id = uniqueId.toString();
        TameableModel alias = new TameableModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.entityUUID).is(id).selectFirst();
        } finally {
            db.close();
        }
    }

    public static Collection<TameableModel> findByOwner(final String ownerId) {
        TameableModel alias = new TameableModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.ownerId).is(ownerId).select();
        } finally {
            db.close();
        }
    }

    public static TameableModel fromEntity(LivingEntity tameable) {
        if (!(tameable instanceof Tameable)) throw new IllegalArgumentException("LivingEntity not tamable.");
        return findByUUID(tameable.getUniqueId());
    }

    public static void disownPets(String animalTamer) {
        for (TameableModel wrapper : findByTamer(animalTamer)) {
            if (wrapper.getEntity() == null) continue;
            ((Tameable) wrapper.getEntity()).setOwner(new AnimalTamer() {
                @Override
                public String getName() {
                    return "Disowned";
                }

                @Override
                public UUID getUniqueId() {
                    return null;
                }
            });
        }
    }

    public static void reownPets(AnimalTamer tamer, CharacterModel character) {
        for (TameableModel wrapper : findByTamer(character.getId()))
            if (wrapper.getEntity() != null) ((Tameable) wrapper.getEntity()).setOwner(tamer);
    }
}
