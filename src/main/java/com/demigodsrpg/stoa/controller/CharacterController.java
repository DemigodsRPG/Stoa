package com.demigodsrpg.stoa.controller;

import com.censoredsoftware.library.data.ServerData;
import com.censoredsoftware.library.messages.CommonSymbol;
import com.censoredsoftware.shaded.com.iciql.Db;
import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.data.DataManager;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.entity.player.StoaPlayer;
import com.demigodsrpg.stoa.entity.player.attribute.Skill;
import com.demigodsrpg.stoa.entity.player.attribute.StoaPotionEffect;
import com.demigodsrpg.stoa.event.StoaChatEvent;
import com.demigodsrpg.stoa.inventory.StoaEnderInventory;
import com.demigodsrpg.stoa.inventory.StoaPlayerInventory;
import com.demigodsrpg.stoa.location.StoaLocation;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.PlayerModel;
import com.demigodsrpg.stoa.structure.StoaStructure;
import com.demigodsrpg.stoa.structure.StoaStructureType;
import com.demigodsrpg.stoa.util.Configs;
import com.demigodsrpg.stoa.util.Messages;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CharacterController extends Controller<CharacterModel> implements Participant<CharacterModel>
{
	public static CharacterController fromName(String characterName)
	{
		CharacterModel alias = new CharacterModel();
		Db db = openDb();
		CharacterModel model = Iterables.getFirst(db.from(alias).where(alias.name).is(characterName).select(), null);
		db.close();
		if(model == null) return null;
		return fromId(model.uuid);
	}

	public static CharacterController fromId(String characterId)
	{
		return new CharacterController().control(characterId);
	}

	public static CharacterController fromPlayer(Player player)
	{
		return PlayerController.fromName(player.getName()).getCharacter();
	}

	public CharacterController setName(String name)
	{
		model.name = name;
	}

	public CharacterController setDeity(Deity deity)
	{
		model.deity = deity.getName();
	}

	public CharacterController setMinorDeities(Set<String> set)
	{
		this.minorDeities = set;
	}

	public CharacterController addMinorDeity(Deity deity)
	{
		this.minorDeities.add(deity.getName());
	}

	public CharacterController removeMinorDeity(Deity deity)
	{
		this.minorDeities.remove(deity.getName());
	}

	public CharacterController setPlayer(PlayerModel player)
	{
		model.playerId = player.id();
	}

	public CharacterController setActive(boolean option)
	{
		model.active = option;
		return this;
	}

	public CharacterController saveInventory()
	{
		model.inventoryId = StoaPlayerInventory.create(this).getId();
		model.enderInventory = StoaEnderInventory.create(this).getId();
		return this;
	}

	public CharacterController setAlive(boolean alive)
	{
		model.alive = alive;
		return this;
	}

	public CharacterController setHealth(double health)
	{
		model.health = health;
	}

	public CharacterController setHunger(int hunger)
	{
		model.hunger = hunger;
	}

	public CharacterController setLevel(int level)
	{
		model.level = level;
	}

	public CharacterController setExperience(float exp)
	{
		model.experience = exp;
	}

	public CharacterController setLocation(Location location)
	{
		model.locationId = StoaLocation.of(location).getId();
	}

	public CharacterController setBedSpawn(Location location)
	{
		model.bedSpawnId = StoaLocation.of(location).getId();
	}

	public CharacterController setGameMode(GameMode gameMode)
	{
		model.gameMode = gameMode;
	}

	public CharacterController setUsable(boolean usable)
	{
		model.usable = usable;
	}

	public CharacterController setPotionEffects(Collection<PotionEffect> potions)
	{

	}

	public Set<PotionEffect> getPotionEffects()
	{

	}

	public StoaPlayerInventory getInventory()
	{

	}

	public StoaEnderInventory getDemigodsEnderInventory()
	{

	}

	public Collection<Deity> getMinorDeities()
	{

	}

	public Deity getDeity()
	{
		return Stoa.getMythos().getDeity(model.deity);
	}

	public Alliance getAlliance()
	{
		return getDeity().getAlliance();
	}

	public CharacterController setKillCount(int amount)
	{
		model.killCount = amount;
		return this;
	}

	public CharacterController addKill()
	{
		model.killCount += 1;
		return this;
	}

	public int getDeathCount()
	{

	}

	public CharacterController addDeath()
	{

	}

	public CharacterController addDeath(CharacterModel attacker)
	{

	}

	public Collection<StoaStructure> getOwnedStructures()
	{
		return StoaStructure.find(new Predicate<StoaStructure>()
		{
			@Override
			public boolean apply(StoaStructure data)
			{
				return data.getOwner().equals(model.id());
			}
		});
	}

	public Collection<StoaStructure> getOwnedStructures(final String type)
	{
		return StoaStructure.find(new Predicate<StoaStructure>()
		{
			@Override
			public boolean apply(StoaStructure data)
			{
				return data.getTypeName().equals(type) && data.getOwner().equals(getId());
			}
		});
	}

	public int getFavorRegen()
	{
		int favorRegenSkill = getSkill(Skill.Type.FAVOR_REGEN) != null ? 4 * getSkill(Skill.Type.FAVOR_REGEN).getLevel() : 0;
		int regenRate = (int) Math.ceil(Configs.getSettingDouble("multipliers.favor") * (getDeity().getFavorRegen() + favorRegenSkill));
		if(regenRate < 30) regenRate = 30;
		return regenRate;
	}

	public void setCanPvp(boolean pvp)
	{
		PlayerController.fromId(model.playerId).setCanPvp(pvp).open().update().relinquish();
	}

	public void updateUseable()
	{
		model.usable = getDeity() != null && getDeity().getFlags().contains(Deity.Flag.PLAYABLE);
	}

	public boolean alliedTo(Participant participant)
	{
		return getAlliance().equals(participant.getCharacter().getAlliance());
	}

	public Collection<StoaTameable> getPets()
	{
		return StoaTameable.findByOwner(model.id());
	}

	public void remove()
	{
		// Define the DemigodsPlayer
		StoaPlayer stoaPlayer = StoaPlayer.of(getBukkitOfflinePlayer());

		// Switch the player to mortal
		if(getBukkitOfflinePlayer().isOnline() && stoaPlayer.getCharacter().getName().equalsIgnoreCase(getName())) stoaPlayer.setToMortal();

		// Remove the data
		if(stoaPlayer.getCharacter() != null && stoaPlayer.getCharacter().getName().equalsIgnoreCase(getName())) stoaPlayer.resetCurrent();
		for(StoaStructure structure : StoaStructureType.Util.getStructuresWithFlag(StoaStructureType.Flag.DELETE_WITH_OWNER))
			if(structure.hasOwner() && structure.getOwner().equals(getId())) structure.remove();
		for(StoaPotionEffect potion : getRawPotionEffects())
			potion.remove();
		getInventory().remove();
		getDemigodsEnderInventory().remove();
		getMeta().remove();
		super.remove();
	}

	public List<Ability> getAbilities()
	{
		List<Ability> list = Lists.newArrayList();

		list.addAll(getDeity().getAbilities());

		for(Deity minorDeity : getMinorDeities())
			list.addAll(minorDeity.getAbilities());

		return list;
	}

	public CharacterController sendAllianceMessage(String message)
	{
		StoaChatEvent chatEvent = new StoaChatEvent(message, Stoa.getServer().getOnlineCharactersWithAlliance(getAlliance()));
		Bukkit.getPluginManager().callEvent(chatEvent);
		if(!chatEvent.isCancelled()) for(Player player : chatEvent.getRecipients())
			player.sendMessage(message);
	}

	public CharacterController chatWithAlliance(String message)
	{
		sendAllianceMessage(" " + ChatColor.GRAY + getAlliance() + "s " + ChatColor.DARK_GRAY + "" + CommonSymbol.BLACK_FLAG + " " + getDeity().getColor() + name + ChatColor.GRAY + ": " + ChatColor.RESET + message);
		Messages.info("[" + getAlliance() + "]" + model.name + ": " + message);
	}

	public CharacterController applyToPlayer(final Player player)
	{
		// Define variables
		StoaPlayer playerSave = StoaPlayer.of(player);

		// Set character to active
		setActive(true);

		if(playerSave.getMortalInventory() != null)
		{
			playerSave.setMortalName(player.getDisplayName());
			playerSave.setMortalListName(player.getPlayerListName());
		}

		// Update their inventory
		if(playerSave.getCharacters().size() == 1) saveInventory();
		else player.getEnderChest().clear();
		getInventory().setToPlayer(player);
		getDemigodsEnderInventory().setToPlayer(player);

		// Update health, experience, and name
		player.setDisplayName(getDeity().getColor() + getName());
		player.setPlayerListName(getDeity().getColor() + getName());
		player.setMaxHealth(getMaxHealth());
		player.setHealth(getHealth() >= getMaxHealth() ? getMaxHealth() : getHealth());
		player.setFoodLevel(getHunger());
		player.setExp(getExperience());
		player.setLevel(getLevel());
		for(PotionEffect potion : player.getActivePotionEffects())
			player.removePotionEffect(potion.getType());
		Set<PotionEffect> potionEffects = getPotionEffects();
		if(!potionEffects.isEmpty()) player.addPotionEffects(potionEffects);
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(getBedSpawn() != null) player.setBedSpawnLocation(getBedSpawn());
			}
		}, 1);
		if(gameMode != null) player.setGameMode(gameMode);

		// Set player display name
		player.setDisplayName(getDeity().getColor() + getName());
		player.setPlayerListName(getDeity().getColor() + getName());

		// Re-own pets
		StoaTameable.reownPets(player, this);
	}

	public static void updateUsableCharacters()
	{
		CharacterController alias = new CharacterController();
		Db db = openDb();
		for(CharacterController character : db.from(alias).select())
			character.updateUseable();
		db.close();
	}

	public static boolean charExists(String name)
	{
		return Stoa.getServer().getCharacter(name) != null;
	}

	public static boolean isCooledDown(StoaCharacter character, String abilityName)
	{
		return !ServerData.exists(DataManager.DATA_MANAGER, character.getName(), abilityName + "_cooldown");
	}

	public static void setCooldown(StoaCharacter character, String abilityName, int cooldown)
	{
		ServerData.put(DataManager.DATA_MANAGER, character.getName(), abilityName + "_cooldown", true, cooldown, TimeUnit.SECONDS);
	}

	public static Long getCooldown(StoaCharacter character, String abilityName)
	{
		return ServerData.find(DataManager.DATA_MANAGER, character.getName(), abilityName + "_cooldown").getExpiration();
	}

	/**
	 * Updates favor for all online characters.
	 */
	public static void updateFavor()
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			CharacterController character = CharacterController.fromId().of(player);
			if(character != null) character.getMeta().addFavor(character.getFavorRegen());
		}
	}

	@Override
	public CharacterController control(String modelId)
	{
		return (CharacterController) control(modelId, new CharacterModel());
	}

	@Override
	public CharacterController refresh()
	{
		model = Iterables.getFirst(DB.from(model).where(model.uuid).is(model.id()).select(), model);
		return this;
	}

	@Override
	public CharacterController getCharacter()
	{
		return this;
	}

	@Override
	public boolean hasCharacter()
	{
		return true;
	}

	@Override
	public boolean canPvp()
	{
		return PlayerController.fromId(model.playerId).canPvp();
	}

	@Override
	public Location getCurrentLocation()
	{
		return PlayerController.fromId(model.playerId).getCurrentLocation();
	}

	@Override
	public Player getEntity()
	{
		return PlayerController.fromId(model.playerId).getEntity();
	}
}
