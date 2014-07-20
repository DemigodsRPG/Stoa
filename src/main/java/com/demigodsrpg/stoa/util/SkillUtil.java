package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.model.CharacterModel;
import org.bukkit.OfflinePlayer;

public class SkillUtil {
    /**
     * Processes a completed battle and distributes appropriate skill points.
     */
    public static void processBattle(Battle battle) {
        // Loop through all participants and apply appropriate updates
        for (Participant participant : battle.getParticipants()) {
            // Get related character
            CharacterModel character = participant.getCharacter();

            // Define all variables used for skill point calculation
            int mvpBonus = battle.getMVPs().contains(participant.getCharacter()) ? StoaPlugin.config().getInt("bonuses.mvp_skill_points") : 1;
            int kills = battle.getKills().get(participant);
            int deaths = battle.getDeaths().get(participant);

            // Calculate skill points
            int skillPoints = (int) Math.ceil(StoaPlugin.config().getDouble("multipliers.skill_points") * ((kills + 1) - (deaths / 2))) + mvpBonus;

            // Apply the points and notify the player
            character.addSkillPoints(skillPoints);

            OfflinePlayer player = PlayerUtil.fromId(character.getPlayerId()).getOfflinePlayer();
            if (player.isOnline()) {
                for (String string : English.NOTIFICATION_SKILL_POINTS_RECEIVED.getLines())
                    player.getPlayer().sendMessage(string.replace("{skillpoints}", "" + skillPoints));
            }
        }
    }
}
