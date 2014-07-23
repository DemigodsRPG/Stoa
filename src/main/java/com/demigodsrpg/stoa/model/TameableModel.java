package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.util.CharacterUtil;
import com.iciql.Db;
import com.iciql.Iciql;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.UUID;

@Iciql.IQTable(name = "dg_tameables")
public class TameableModel implements Participant {
    @Iciql.IQColumn(name = "entity_uuid", primaryKey = true)
    public String entityUUID;
    @Iciql.IQEnum
    @Iciql.IQColumn(name = "entity_type")
    public EntityType entityType;
    @Iciql.IQColumn(name = "animal_tamer")
    public String animalTamer;
    @Iciql.IQColumn(name = "can_pvp")
    public Boolean canPvp;
    @Iciql.IQColumn(name = "owner_id")
    public String ownerId;

    public TameableModel() {
    }

    public TameableModel(Tameable tameable, String ownerId) {
        if (!(tameable instanceof LivingEntity)) throw new IllegalArgumentException("Pet must be alive.");
        setTamable((LivingEntity) tameable);
        setOwnerId(ownerId);
    }

    public void remove() {
        Db db = StoaServer.openDb();
        db.delete(this);
        getEntity().remove();
        db.close();
    }

    public void setTamable(LivingEntity tameable) {
        this.entityType = tameable.getType();
        this.entityUUID = tameable.getUniqueId().toString();
    }

    public boolean canPvp() {
        return canPvp;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getAnimalTamer() {
        return animalTamer;
    }

    public UUID getEntityUUID() {
        return UUID.fromString(entityUUID);
    }

    public LivingEntity getEntity() {
        for (World world : Bukkit.getServer().getWorlds()) {
            for (Entity pet : world.getLivingEntities()) {
                if (!(pet instanceof Tameable)) continue;
                if (pet.getUniqueId().toString().equals(this.entityUUID)) return (LivingEntity) pet;
            }
        }
        remove();
        return null;
    }

    @Override
    public boolean hasCharacter() {
        return true;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.animalTamer = ownerId;
        this.ownerId = ownerId;
    }

    public String getId() {
        return this.entityUUID;
    }

    @Override
    public CharacterModel getCharacter() {
        return null;
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        return null;
    }

    public Location getCurrentLocation() {
        try {
            return getEntity().getLocation();
        } catch (Exception ignored) {
        }
        return null;
    }

    public void disown() {
        if (this.getEntity() == null) return;
        ((Tameable) this.getEntity()).setOwner(new AnimalTamer() {
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

    public CharacterModel getOwner() {
        CharacterModel owner = CharacterUtil.fromId(ownerId);
        if (owner == null) {
            disown();
            remove();
            return null;
        } else if (!owner.getUsable()) return null;
        return owner;
    }

    public void setOwner(CharacterModel owner) {
        setOwnerId(owner.playerId);
    }

    public Deity getDeity() {
        if (getOwner() == null) {
            disown();
            remove();
            return null;
        } else if (!getOwner().getUsable()) return null;
        return getOwner().getDeity();
    }
}
