package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.entity.player.attribute.Skill;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.SkillModel;
import com.iciql.Db;

public class SkillUtil {
    public static void cleanSkills() {
        SkillModel alias = new SkillModel();
        Db db = StoaServer.openDb();

        // Clean skills
        for (SkillModel skill : db.from(alias).select()) {
            try {
                // Attempt to find the value of the skill name
                Skill.Type.valueOf(skill.name.toUpperCase());
            } catch (Exception ignored) {
                // There was an error. Catch it and remove the skill.
                db.delete(skill);
            }
        }

        db.close();
    }

    /**
     * Processes a completed battle and distributes appropriate skill points.
     */
    public static void processBattle(Battle battle) {
        // Loop through all participants and apply appropriate updates
        for (Participant participant : battle.getParticipants()) {
            // Get related character
            CharacterModel character = participant.getCharacter();

            // Define all variables used for skill point calculation
            int mvpBonus = battle.getMVPs().contains(participant.getCharacter().uuid) ? Configs.getSettingInt("bonuses.mvp_skill_points") : 1;
            int kills = battle.getKills(participant);
            int deaths = battle.getDeaths(participant);

            // Calculate skill points
            int skillPoints = (int) Math.ceil(Configs.getSettingDouble("multipliers.skill_points") * ((kills + 1) - (deaths / 2))) + mvpBonus;

            // Apply the points and notify the player
            character.getMeta().addSkillPoints(skillPoints);

            if (character.getBukkitOfflinePlayer().isOnline()) {
                for (String string : English.NOTIFICATION_SKILL_POINTS_RECEIVED.getLines())
                    character.getBukkitOfflinePlayer().getPlayer().sendMessage(string.replace("{skillpoints}", "" + skillPoints));
            }
        }
    }
}
