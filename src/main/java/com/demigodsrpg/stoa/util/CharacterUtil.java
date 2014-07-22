package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.google.common.collect.Iterables;
import com.iciql.Db;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CharacterUtil {
    private CharacterUtil() {
    }

    public static CharacterModel fromName(String characterName) {
        CharacterModel alias = new CharacterModel();
        Db db = StoaServer.openDb();
        CharacterModel model = Iterables.getFirst(db.from(alias).where(alias.name).is(characterName).select(), null);
        db.close();
        if (model == null) return null;
        return fromId(model.uuid);
    }

    public static CharacterModel fromId(String characterId) {
        CharacterModel alias = new CharacterModel();
        Db db = StoaServer.openDb();

        try {
            return db.from(alias).where(alias.uuid).is(characterId).selectFirst();
        } finally {
            db.close();
        }
    }

    public static CharacterModel currentFromPlayer(Player player) {
        return PlayerUtil.fromName(player.getName()).getCharacter();
    }

    public static void updateUsableCharacters() {
        CharacterModel alias = new CharacterModel();
        Db db = StoaServer.openDb();
        for (CharacterModel character : db.from(alias).select())
            character.updateUseable();
        db.close();
    }

    public static boolean charExists(String name) {
        return Stoa.getServer().getCharacter(name) != null;
    }

    public static boolean isCooledDown(CharacterModel character, String abilityName) {
        return !ServerDataUtil.exists(character.name, abilityName + "_cooldown");
    }

    public static void setCooldown(CharacterModel character, String abilityName, int cooldown) {
        ServerDataUtil.put(true, cooldown, TimeUnit.SECONDS, character.name, abilityName + "_cooldown");
    }

    public static Long getCooldown(CharacterModel character, String abilityName) {
        return ServerDataUtil.get(character.name, abilityName + "_cooldown").expire.getTime();
    }

    /**
     * Updates favor for all online characters.
     */
    public static void updateFavor() {
        for (CharacterModel character : Stoa.getOnlineCharacters())
            character.addFavor(character.getDeity().getFavorRegen());
    }
}
