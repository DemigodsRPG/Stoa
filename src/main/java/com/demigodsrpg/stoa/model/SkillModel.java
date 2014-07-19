package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.util.CharacterUtil;
import com.iciql.Iciql;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class SkillModel {
    @Iciql.IQColumn
    public String id;
    @Iciql.IQColumn
    public String characterId;
    @Iciql.IQColumn
    public Type type;
    @Iciql.IQColumn
    public Integer points;
    @Iciql.IQColumn
    public Integer level;

    @Iciql.IQEnum
    public enum Type {
        /*
         * OFFENSE
         */
        OFFENSE("Offense", "Offensive power.", new Permission("demigods.skill.offense", "Allows the player to obtain the Offense skill type.", PermissionDefault.TRUE), true, true),
        /*
         * DEFENSE
         */
        DEFENSE("Defense", "Defensive power.", new Permission("demigods.skill.defense", "Allows the player to obtain the Defense skill type.", PermissionDefault.TRUE), true, true),
        /*
         * SUPPORT
         */
        SUPPORT("Support", "Support power.", new Permission("demigods.skill.support", "Allows the player to obtain the Support skill type.", PermissionDefault.TRUE), true, true),
        /*
         * ULTIMATE
         */
        ULTIMATE("Ultimate", "Ultimate power.", new Permission("demigods.skill.ultimate", "Allows the player to obtain the Ultimate skill type.", PermissionDefault.TRUE), true, true),
        /*
         * PASSIVE
         */
        PASSIVE("Passive", "Passive power.", new Permission("demigods.skill.passive", "Cannot be levelled.", PermissionDefault.TRUE), true, false),
        /*
         * FAVOR REGEN
         */
        FAVOR_REGEN("Favor Regen", "Favor regeneration bonus.", new Permission("demigods.skill.favorregen", "Allows the player to obtain the Favor Regeneration skill type.", PermissionDefault.TRUE), true, true);

        private String name;
        private String description;
        private Permission permission;
        private boolean isDefault, levelable;

        private Type(String name, String description, Permission permission, boolean isDefault, boolean levelable) {
            this.name = name;
            this.description = description;
            this.permission = permission;
            this.isDefault = isDefault;
            this.levelable = levelable;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Permission getPermission() {
            return permission;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public boolean isLevelable() {
            return levelable;
        }
    }

    public String getId() {
        return id;
    }

    public CharacterModel getCharacter() {
        return CharacterUtil.fromId(characterId);
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public boolean hasMetCap() {
        return getLevel() >= getCharacter().getIndividualSkillCap();
    }

    public int getLevel() {
        return (level > 0) ? level : 1;
    }

    public void addLevels(int levels) {
        setLevel(getLevel() + levels);
    }

    public void addPoints(int points) {
        // Add points 1 at a time
        for (int i = 0; i < points; i++) {
            // Adding 1 point at a time
            this.points++;

            if (getPoints() >= getRequiredPointsForLevel(getLevel() + 1)) {
                // If they've met the max level cap then stop the addition
                if (getLevel() + 1 >= getCharacter().getIndividualSkillCap()) return;

                // Add a level
                addLevels(1);

                // Reset points
                setPoints(0);
            }
        }
    }

    public int getRequiredPoints() {
        return getRequiredPointsForLevel(getLevel() + 1) - getPoints();
    }

    public int getRequiredPointsForLevel(int level) {
        switch (getType()) {
            case OFFENSE:
            case DEFENSE:
            case SUPPORT:
            case ULTIMATE:
            case FAVOR_REGEN:
                return (int) Math.ceil((level * Math.pow(level, 1.4)) + 5);
        }

        return -1;
    }
}
