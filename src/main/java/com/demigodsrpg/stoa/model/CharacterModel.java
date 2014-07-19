package com.demigodsrpg.stoa.model;

import com.censoredsoftware.library.messages.CommonSymbol;
import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.event.StoaChatEvent;
import com.demigodsrpg.stoa.util.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.iciql.Db;
import com.iciql.Iciql;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Iciql.IQTable(name = "dg_characters")
public class CharacterModel implements Participant {
    // -- DEFAULT CONSTRUCTOR -- //
    public CharacterModel() {
    }

    // -- PRACTICAL CONSTRUCTOR -- //
    public CharacterModel(final PlayerModel player, final String charName, final Deity deity) {
        // Set name
        name = charName;

        // Defaults
        this.deity = deity.getName();
        alive = true;
        active = false;
        usable = true;
        hunger = 20;
        level = 0;
        killCount = 0;
        health = deity.getMaxHealth();
        experience = 0.0F;
        gameMode = GameMode.SURVIVAL;

        // Additional Defaults
        favor = deity.getMaxFavor();
        maxFavor = deity.getMaxFavor();
        skillPoints = StoaPlugin.config().getInt("skillPoints");

        // Foreign Keys
        playerId = player.mojangAccount;

        // Don't set inventories or locations yet
    }

    // -- MODEL META -- //
    @Iciql.IQColumn(name = "id", primaryKey = true)
    public String uuid = UUID.randomUUID().toString();

    // -- DATA -- //
    @Iciql.IQColumn
    public String name;
    @Iciql.IQColumn
    public String deity;
    @Iciql.IQColumn
    public Boolean alive;
    @Iciql.IQColumn
    public Boolean active;
    @Iciql.IQColumn
    public Boolean usable;
    @Iciql.IQColumn
    public Integer hunger;
    @Iciql.IQColumn
    public Integer level;
    @Iciql.IQColumn(name = "kill_count")
    public Integer killCount;
    @Iciql.IQColumn
    public Double health;
    @Iciql.IQColumn
    public Float experience;
    @Iciql.IQEnum
    @Iciql.IQColumn(name = "game_mode")
    public GameMode gameMode;

    // -- ADDITIONAL DATA -- //
    @Iciql.IQColumn
    public Integer favor;
    @Iciql.IQColumn(name = "max_favor")
    public Integer maxFavor;
    @Iciql.IQColumn(name = "skill_points")
    public Integer skillPoints;

    // private Set<UUID> notifications;
    // private Map<String, UUID> binds;
    // private Map<String, UUID> skillData;
    // private Map<String, UUID> warps;
    // private Map<String, UUID> invites;

    // -- FOREIGN DATA -- //
    @Iciql.IQColumn(nullable = false, name = "player_id")
    public String playerId;
    @Iciql.IQColumn(name = "last_location")
    public String lastLocation;
    @Iciql.IQColumn
    public String bedSpawn;

    public String getId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeity(String deity) {
        this.deity = deity;
    }

    public Boolean getAlive() {
        return alive;
    }

    public void setAlive(Boolean alive) {
        this.alive = alive;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getUsable() {
        return usable;
    }

    public void setUsable(Boolean usable) {
        this.usable = usable;
    }

    public Integer getHunger() {
        return hunger;
    }

    public void setHunger(Integer hunger) {
        this.hunger = hunger;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getKillCount() {
        return killCount;
    }

    public void addKill() {
        killCount++;
    }

    public int getDeathCount() {
        return getDeaths().size();
    }

    public void addDeath() {
        DeathModel death = new DeathModel(this);
        Db db = StoaServer.openDb();
        db.insert(death);
        db.close();
    }

    public void addDeath(CharacterModel attacker) {
        DeathModel death = new DeathModel(this, attacker);
        Db db = StoaServer.openDb();
        db.insert(death);
        db.close();
    }

    public List<DeathModel> getDeaths() {
        DeathModel alias = new DeathModel();
        Db db = StoaServer.openDb();

        try {
            return db.from(alias).where(alias.victimId).is(uuid).select();
        } finally {
            db.close();
        }
    }

    public Double getHealth() {
        return health;
    }

    public void setHealth(Double health) {
        this.health = health;
    }

    public Float getExperience() {
        return experience;
    }

    public void setExperience(Float experience) {
        this.experience = experience;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Integer getFavor() {
        return favor;
    }

    public void setFavor(Integer favor) {
        this.favor = favor;
    }

    public void addFavor(Integer add) {
        this.favor += add;
    }

    public void removeFavor(Integer remove) {
        this.favor -= remove;
    }

    public Integer getMaxFavor() {
        return maxFavor;
    }

    public void setMaxFavor(Integer maxFavor) {
        this.maxFavor = maxFavor;
    }

    public Integer getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(Integer skillPoints) {
        this.skillPoints = skillPoints;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public PlayerInventoryModel getInventory() {
        return InventoryUtil.playerInvFromOwnerId(uuid);
    }

    public EnderChestInventoryModel getEnderInventory() {
        return InventoryUtil.enderInvFromOwnerId(uuid);
    }

    public Location getLastLocation() {
        return LocationUtil.locationFromString(lastLocation);
    }

    public void setLastLocation(Location location) {
        this.lastLocation = LocationUtil.stringFromLocation(location);
    }

    public Location getBedSpawn() {
        return LocationUtil.locationFromString(bedSpawn);
    }

    public void setBedSpawn(Location bedSpawn) {
        this.bedSpawn = LocationUtil.stringFromLocation(bedSpawn);
    }

    public Set<PotionEffect> getPotionEffects() {
        // TODO
        return null;
    }

    public Collection<Deity> getMinorDeities() {
        // TODO
        return null;
    }

    public Deity getDeity() {
        return Stoa.getMythos().getDeity(deity);
    }

    public Alliance getAlliance() {
        return getDeity().getAlliance();
    }

    public List<StructureModel> getOwnedStructures() {
        StructureModel alias = new StructureModel();
        Db db = StoaServer.openDb();

        try {
            return db.from(alias).where(alias.getOwnerId()).is(uuid).select();
        } finally {
            db.close();
        }
    }

    public List<StructureModel> getOwnedStructures(final String type) {
        StructureModel alias = new StructureModel();
        Db db = StoaServer.openDb();

        try {
            return db.from(alias).where(alias.getOwnerId()).is(uuid).and(alias.typeName).is(type).select();
        } finally {
            db.close();
        }
    }

    public int getFavorRegen() {
        SkillModel alias = new SkillModel();
        Db db = StoaServer.openDb();
        SkillModel found = Iterables.getFirst(db.from(alias).where(alias.type).is(SkillModel.Type.FAVOR_REGEN).and(alias.characterId).is(uuid).select(), null);
        int favorRegenSkill = found != null ? 4 * found.level : 0;
        int regenRate = (int) Math.ceil(StoaPlugin.getInst().getConfig().getDouble("multipliers.favor") * (getDeity().getFavorRegen() + favorRegenSkill));
        if (regenRate < 30) regenRate = 30;
        return regenRate;
    }

    public void setCanPvp(boolean pvp) {
        Db db = StoaServer.openDb();
        PlayerModel player = PlayerUtil.fromId(playerId);
        player.setCanPvp(pvp);
        db.update(player);
        db.close();
    }

    public List<SkillModel> getSkills() {
        SkillModel alias = new SkillModel();
        Db db = StoaServer.openDb();

        try {
            return db.from(alias).where(alias.characterId).is(uuid).select();
        } finally {
            db.close();
        }
    }

    public int getIndividualSkillCap() {
        int total = 0;
        for (SkillModel skill : getSkills())
            total += skill.getLevel();
        return getOverallSkillCap() - total;
    }

    public int getOverallSkillCap() {
        // This is done this way so it can easily be manipulated later
        return StoaPlugin.config().getInt("caps.skills");
    }

    public void updateUseable() {
        usable = getDeity() != null && getDeity().getFlags().contains(Deity.Flag.PLAYABLE);
    }

    public boolean alliedTo(Participant participant) {
        return getAlliance().equals(participant.getCharacter().getAlliance());
    }

    public Collection<StoaTameable> getPets() {
        return StoaTameable.findByOwner(uuid);
    }

    public void remove() {
        Db db = StoaServer.openDb();

        // Define controller
        PlayerModel player = PlayerUtil.fromId(playerId);

        // Switch the player to mortal
        if (getEntity() != null && player.currentCharacterId.equals(uuid)) {
            player.setToMortal();
            player.resetCurrent();
        }

        for (StructureModel structure : StructureUtil.getStructuresWithFlag(StructureModel.Flag.DELETE_WITH_OWNER))
            if (structure.getOwnerId() != null && structure.getOwnerId().equals(uuid)) structure.remove();
        // for(StoaPotionEffect potion : getPotionEffects())
        // 	potion.remove();

        db.delete(getInventory());
        db.delete(getEnderInventory());
        db.delete(this);
    }

    public List<Ability> getAbilities() {
        List<Ability> list = Lists.newArrayList();

        list.addAll(getDeity().getAbilities());

        for (Deity minorDeity : getMinorDeities())
            list.addAll(minorDeity.getAbilities());

        return list;
    }

    public CharacterModel sendAllianceMessage(String message) {
        StoaChatEvent chatEvent = new StoaChatEvent(message, Stoa.getServer().getOnlineCharactersWithAlliance(getAlliance()));
        Bukkit.getPluginManager().callEvent(chatEvent);
        if (!chatEvent.isCancelled()) for (Player player : chatEvent.getRecipients())
            player.sendMessage(message);
        return this;
    }

    public CharacterModel chatWithAlliance(String message) {
        sendAllianceMessage(" " + ChatColor.GRAY + getAlliance() + "s " + ChatColor.DARK_GRAY + "" + CommonSymbol.BLACK_FLAG + " " + getDeity().getColor() + name + ChatColor.GRAY + ": " + ChatColor.RESET + message);
        MessageUtil.info("[" + getAlliance() + "]" + name + ": " + message);
        return this;
    }

    public void saveInventory() {
        Db db = StoaServer.openDb();

        PlayerInventoryModel inventoryModel = getInventory();
        PlayerInventory inventory = getEntity().getInventory();

        inventoryModel.setArmor(inventory.getArmorContents());
        inventoryModel.setContents(inventory.getContents());

        db.update(inventoryModel);

        EnderChestInventoryModel enderChestInventoryModel = getEnderInventory();
        Inventory enderChestInventory = getEntity().getEnderChest();

        enderChestInventoryModel.setContents(enderChestInventory.getContents());

        db.update(enderChestInventoryModel);

        db.close();
    }

    public CharacterModel applyToPlayer(final Player player) {
        // Define variables
        PlayerModel playerSave = PlayerUtil.fromId(playerId);

        // Set character to active
        setActive(true);

        if (playerSave.getMortalInventory() != null) {
            playerSave.setMortalName(player.getDisplayName());
            playerSave.setMortalListName(player.getPlayerListName());
        }

        // Update their inventory
        if (playerSave.getCharacters().size() == 1) saveInventory();
        else player.getEnderChest().clear();
        getInventory().setToPlayer(player);
        getEnderInventory().setToPlayer(player);

        // Update health, experience
        player.setMaxHealth(getDeity().getMaxHealth());
        player.setHealth(health >= getDeity().getMaxHealth() ? getDeity().getMaxHealth() : health);
        player.setFoodLevel(hunger);
        player.setExp(experience);
        player.setLevel(level);
        for (PotionEffect potion : player.getActivePotionEffects())
            player.removePotionEffect(potion.getType());
        Set<PotionEffect> potionEffects = getPotionEffects();
        if (!potionEffects.isEmpty()) player.addPotionEffects(potionEffects);
        /*
        Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(getBedSpawn() != null) player.setBedSpawnLocation(getBedSpawn());
			}
		}, 1);
		*/
        if (gameMode != null) player.setGameMode(gameMode);

        // Set player display name
        player.setDisplayName(getDeity().getColor() + name);
        player.setPlayerListName(getDeity().getColor() + name); // FIXME

        // Re-own pets
        // StoaTameable.reownPets(player, this);
        return this;
    }

    @Override
    public CharacterModel getCharacter() {
        return this;
    }

    @Override
    public boolean hasCharacter() {
        return true;
    }

    @Override
    public boolean canPvp() {
        return PlayerUtil.fromId(playerId).canPvp();
    }

    @Override
    public Location getCurrentLocation() {
        return PlayerUtil.fromId(playerId).getCurrentLocation();
    }

    @Override
    public Player getEntity() {
        return PlayerUtil.fromId(playerId).getEntity();
    }
}
