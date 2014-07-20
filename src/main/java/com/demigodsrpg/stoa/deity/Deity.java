package com.demigodsrpg.stoa.deity;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.util.CharacterUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Deity {
    @Override
    String toString();

    String getName();

    Alliance getAlliance();

    String getPermission();

    PermissionDefault getPermissionDefault();

    ChatColor getColor();

    MaterialData getMaterialData();

    Sound getSound();

    Map<Material, Integer> getClaimItems();

    Map<Material, Integer> getForsakeItems();

    String getShortDescription();

    List<String> getLore();

    Set<Flag> getFlags();

    List<Ability> getAbilities();

    int getAccuracy();

    int getFavorRegen();

    int getMaxFavor();

    double getMaxHealth();

    int getFavorBank();

    void updateMood();

    enum Flag {
        PLAYABLE, NON_PLAYABLE, MAJOR_DEITY, MINOR_DEITY, NEUTRAL, DIFFICULT, ALTERNATE_ASCENSION_LEVELING, NO_SHRINE, NO_OBELISK, NO_BATTLE
    }

    enum Mood {
        ECSTATIC, PLEASED, INTERESTED, CALM /* (the default) */, SAD, DEFEATED, ANGRY, ENRAGED, CONFUSED
    }

    interface MoodPack {
        MaterialData getMaterialData();

        Sound getSound();

        Map<Material, Integer> getClaimItems();

        Map<Material, Integer> getForsakeItems();

        Set<Flag> getFlags();

        List<Ability> getAbilities();

        int getAccuracy();

        int getFavorRegen();
    }

    interface MoodManager {
        void set(Mood mood, MoodPack moodPack);

        MoodPack get(Mood mood);
    }

    class Util {
        public static boolean canUseDeity(CharacterModel character, String deity) {
            if (character == null) return false;
            if (!character.getOfflinePlayer().isOnline()) return canUseDeitySilent(character, deity);
            Player player = character.getOfflinePlayer().getPlayer();
            if (!player.hasPermission(Stoa.getMythos().getDeity(deity).getPermission())) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use " + deity + "!");
                return false;
            }
            if (!character.isDeity(deity)) {
                player.sendMessage(ChatColor.RED + "You haven't claimed " + deity + "! You can't do that!");
                return false;
            }
            return true;
        }

        public static boolean canUseDeitySilent(CharacterModel character, String deity) {
            return !(character.getOfflinePlayer().isOnline() && !character.getOfflinePlayer().getPlayer().hasPermission(Stoa.getMythos().getDeity(deity).getPermission())) && character.isDeity(deity);
        }

        public static boolean canUseDeitySilent(Player player, String deityName) {
            String currentDeityName = CharacterUtil.currentFromPlayer(player).getDeity().getName();
            return deityName.equalsIgnoreCase(currentDeityName);
        }
    }
}
