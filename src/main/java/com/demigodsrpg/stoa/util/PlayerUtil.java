package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.PlayerModel;
import com.google.common.collect.Iterables;
import com.iciql.Db;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static PlayerModel fromName(String playerName) {
        PlayerModel alias = new PlayerModel();
        Db db = StoaServer.openDb();
        try {
            return Iterables.getFirst(db.from(alias).
                    where(alias.playerName).
                    is(playerName).
                    select(), null);
        } finally {
            db.close();
        }
    }

    public static PlayerModel fromPlayer(Player player) {
        PlayerModel alias = new PlayerModel();

        String mojangId = player.getUniqueId().toString();
        PlayerModel playerModel;

        Db db = StoaServer.openDb();

        if (fromId(mojangId) == null) {
            playerModel = new PlayerModel(player);
            db.insert(playerModel);
        } else {
            playerModel = Iterables.getFirst(db.from(alias).
                    where(alias.mojangAccount).
                    is(mojangId).
                    select(), null);
        }

        db.close();

        return playerModel;
    }

    public static PlayerModel fromId(String mojangId) {
        PlayerModel alias = new PlayerModel();
        Db db = StoaServer.openDb();
        try {
            return Iterables.getFirst(db.from(alias).
                    where(alias.mojangAccount).
                    is(mojangId).
                    select(), null);
        } finally {
            db.close();
        }
    }

    /**
     * Returns true if the <code>player</code> is currently immortal.
     *
     * @param player the player to check.
     * @return boolean
     */
    public static boolean isImmortal(Player player) {
        CharacterModel character = fromPlayer(player).getCharacter();
        return character != null && character.getUsable() && character.getActive();
    }

    /**
     * Returns true if <code>player</code> has a character with the name <code>charName</code>.
     *
     * @param player   the player to check.
     * @param charName the charName to check with.
     * @return boolean
     */
    public static boolean hasCharName(Player player, String charName) {
        return fromPlayer(player).hasCharName(charName);
    }
}
