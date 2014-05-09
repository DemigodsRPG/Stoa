package com.demigodsrpg.stoa.entity.player;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.censoredsoftware.library.data.ServerData;
import com.censoredsoftware.library.messages.CommonSymbol;
import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.data.DataAccess;
import com.demigodsrpg.stoa.data.DataManager;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.entity.player.attribute.Death;
import com.demigodsrpg.stoa.entity.player.attribute.Skill;
import com.demigodsrpg.stoa.entity.player.attribute.StoaCharacterMeta;
import com.demigodsrpg.stoa.entity.player.attribute.StoaPotionEffect;
import com.demigodsrpg.stoa.event.StoaChatEvent;
import com.demigodsrpg.stoa.inventory.StoaEnderInventory;
import com.demigodsrpg.stoa.inventory.StoaPlayerInventory;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.location.StoaLocation;
import com.demigodsrpg.stoa.structure.StoaStructure;
import com.demigodsrpg.stoa.structure.StoaStructureType;
import com.demigodsrpg.stoa.util.Configs;
import com.demigodsrpg.stoa.util.Messages;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StoaCharacter extends DataAccess<UUID, StoaCharacter> implements Participant
{
	private UUID id;
	private String name;
	private UUID mojangAccount;
	private boolean alive;
	private double health;
	private Integer hunger;
	private Float experience;
	private Integer level;
	private Integer killCount;
	private UUID location;
	private UUID bedSpawn;
	private GameMode gameMode;
	private String deity;
	private Set<String> minorDeities;
	private boolean active;
	private boolean usable;
	private UUID meta;
	private UUID inventory, enderInventory;
	private Set<String> potionEffects;
	private Set<String> deaths;

	private StoaCharacter(Object ignored)
	{
	}

	public StoaCharacter()
	{
		deaths = Sets.newHashSet();
		potionEffects = Sets.newHashSet();
		minorDeities = Sets.newHashSet();
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static StoaCharacter of(UUID id, ConfigurationSection conf)
	{
		StoaCharacter character = new StoaCharacter(null);
		character.id = id;
		character.name = conf.getString("name");
		character.mojangAccount = UUID.fromString(conf.getString("mojangAccount"));
		if(conf.isBoolean("alive")) character.alive = conf.getBoolean("alive");
		character.health = conf.getDouble("health");
		character.hunger = conf.getInt("hunger");
		character.experience = Float.valueOf(conf.getString("experience"));
		character.level = conf.getInt("level");
		character.killCount = conf.getInt("killCount");
		if(conf.isString("location"))
		{
			character.location = UUID.fromString(conf.getString("location"));
			try
			{
				StoaLocation.get(character.location);
			}
			catch(Exception errored)
			{
				character.location = null;
			}
		}
		if(conf.getString("bedSpawn") != null)
		{
			character.bedSpawn = UUID.fromString(conf.getString("bedSpawn"));
			try
			{
				StoaLocation.get(character.bedSpawn);
			}
			catch(Exception errored)
			{
				character.bedSpawn = null;
			}
		}
		if(conf.getString("gameMode") != null) character.gameMode = GameMode.SURVIVAL;
		character.deity = conf.getString("deity");
		character.active = conf.getBoolean("active");
		character.usable = conf.getBoolean("usable");
		character.meta = UUID.fromString(conf.getString("meta"));
		if(conf.isList("minorDeities")) character.minorDeities = Sets.newHashSet(conf.getStringList("minorDeities"));
		if(conf.isString("inventory")) character.inventory = UUID.fromString(conf.getString("inventory"));
		if(conf.isString("enderInventory")) character.enderInventory = UUID.fromString(conf.getString("enderInventory"));
		if(conf.isList("deaths")) character.deaths = Sets.newHashSet(conf.getStringList("deaths"));
		if(conf.isList("potionEffects")) character.potionEffects = Sets.newHashSet(conf.getStringList("potionEffects"));
		return character;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = Maps.newHashMap();
		try
		{
			map.put("name", name);
			map.put("mojangAccount", mojangAccount.toString());
			map.put("alive", alive);
			map.put("health", health);
			map.put("hunger", hunger);
			map.put("experience", experience);
			map.put("level", level);
			map.put("killCount", killCount);
			if(location != null) map.put("location", location.toString());
			if(bedSpawn != null) map.put("bedSpawn", bedSpawn.toString());
			if(gameMode != null) map.put("gameMode", gameMode.name());
			map.put("deity", deity);
			if(minorDeities != null) map.put("minorDeities", Lists.newArrayList(minorDeities));
			map.put("active", active);
			map.put("usable", usable);
			map.put("meta", meta.toString());
			if(inventory != null) map.put("inventory", inventory.toString());
			if(enderInventory != null) map.put("enderInventory", enderInventory.toString());
			if(deaths != null) map.put("deaths", Lists.newArrayList(deaths));
			if(potionEffects != null) map.put("potionEffects", Lists.newArrayList(potionEffects));
		}
		catch(Exception ignored)
		{
		}
		return map;
	}

	void generateId()
	{
		id = UUID.randomUUID();
	}

	void setName(String name)
	{
		this.name = name;
	}

	void setDeity(Deity deity)
	{
		this.deity = deity.getName();
	}

	public void setMinorDeities(Set<String> set)
	{
		this.minorDeities = set;
	}

	public void addMinorDeity(Deity deity)
	{
		this.minorDeities.add(deity.getName());
	}

	public void removeMinorDeity(Deity deity)
	{
		this.minorDeities.remove(deity.getName());
	}

	void setMojangAccount(StoaPlayer player)
	{
		this.mojangAccount = player.getMojangAccount();
	}

	public void setActive(boolean option)
	{
		this.active = option;
		save();
	}

	public void saveInventory()
	{
		this.inventory = StoaPlayerInventory.create(this).getId();
		this.enderInventory = StoaEnderInventory.create(this).getId();
		save();
	}

	public void setAlive(boolean alive)
	{
		this.alive = alive;
		save();
	}

	public void setHealth(double health)
	{
		this.health = health;
	}

	public void setHunger(int hunger)
	{
		this.hunger = hunger;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public void setExperience(float exp)
	{
		this.experience = exp;
	}

	public void setLocation(Location location)
	{
		this.location = StoaLocation.of(location).getId();
	}

	public void setBedSpawn(Location location)
	{
		this.bedSpawn = StoaLocation.of(location).getId();
	}

	public void setGameMode(GameMode gameMode)
	{
		this.gameMode = gameMode;
	}

	public void setMeta(StoaCharacterMeta meta)
	{
		this.meta = meta.getId();
	}

	public void setUsable(boolean usable)
	{
		this.usable = usable;
	}

	public void setPotionEffects(Collection<PotionEffect> potions)
	{
		if(potions != null)
		{
			if(potionEffects == null) potionEffects = Sets.newHashSet();

			for(PotionEffect potion : potions)
				potionEffects.add(StoaPotionEffect.of(potion).getId().toString());
		}
	}

	private Set<PotionEffect> getPotionEffects()
	{
		if(potionEffects == null) potionEffects = Sets.newHashSet();

		Set<PotionEffect> set = new HashSet<>();
		for(String stringId : potionEffects)
		{
			try
			{
				PotionEffect potion = StoaPotionEffect.get(UUID.fromString(stringId)).getBukkitPotionEffect();
				if(potion != null)
				{
					StoaPotionEffect.get(UUID.fromString(stringId)).remove();
					set.add(potion);
				}
			}
			catch(Exception ignored)
			{
			}
		}

		potionEffects.clear(); // METHOD MUST BE PRIVATE IF WE DO THIS HERE
		return set;
	}

	public Collection<StoaPotionEffect> getRawPotionEffects()
	{
		if(potionEffects == null) potionEffects = Sets.newHashSet();
		return Collections2.transform(potionEffects, new Function<String, StoaPotionEffect>()
		{
			@Override
			public StoaPotionEffect apply(String s)
			{
				try
				{
					return StoaPotionEffect.get(UUID.fromString(s));
				}
				catch(Exception ignored)
				{
				}
				return null;
			}
		});
	}

	public StoaPlayerInventory getInventory()
	{
		if(StoaPlayerInventory.get(inventory) == null) inventory = StoaPlayerInventory.createEmpty().getId();
		return StoaPlayerInventory.get(inventory);
	}

	public StoaEnderInventory getDemigodsEnderInventory()
	{
		if(StoaEnderInventory.get(enderInventory) == null) enderInventory = StoaEnderInventory.create(this).getId();
		return StoaEnderInventory.get(enderInventory);
	}

	public StoaCharacterMeta getMeta()
	{
		return StoaCharacterMeta.get(meta);
	}

	public OfflinePlayer getBukkitOfflinePlayer()
	{
		return Bukkit.getOfflinePlayer(getPlayerName());
	}

	public StoaPlayer getDemigodsPlayer()
	{
		return StoaPlayer.get(mojangAccount);
	}

	public Player getBukkitPlayer()
	{
		return getBukkitOfflinePlayer().getPlayer();
	}

	public String getName()
	{
		return name;
	}

	public boolean isActive()
	{
		return active;
	}

	public Location getLocation()
	{
		if(location == null) return null;
		return StoaLocation.get(location).getBukkitLocation();
	}

	public Location getBedSpawn()
	{
		if(bedSpawn == null) return null;
		return StoaLocation.get(bedSpawn).getBukkitLocation();
	}

	public GameMode getGameMode()
	{
		return gameMode;
	}

	public Location getCurrentLocation()
	{
		if(getBukkitOfflinePlayer().isOnline()) return getBukkitOfflinePlayer().getPlayer().getLocation();
		return getLocation();
	}

	@Override
	public StoaCharacter getRelatedCharacter()
	{
		return this;
	}

	@Override
	public LivingEntity getEntity()
	{
		return getBukkitOfflinePlayer().getPlayer();
	}

	public UUID getMojangAccount()
	{
		return mojangAccount;
	}

	public String getPlayerName()
	{
		return StoaPlayer.get(mojangAccount).getPlayerName();
	}

	public Integer getLevel()
	{
		return level;
	}

	public boolean isAlive()
	{
		return alive;
	}

	public Double getHealth()
	{
		return health;
	}

	public Double getMaxHealth()
	{
		return getDeity().getMaxHealth();
	}

	public Integer getHunger()
	{
		return hunger;
	}

	public Float getExperience()
	{
		return experience;
	}

	public boolean isDeity(String deityName)
	{
		return getDeity().getName().equalsIgnoreCase(deityName);
	}

	public Deity getDeity()
	{
		return Stoa.getMythos().getDeity(this.deity);
	}

	public Collection<Deity> getMinorDeities()
	{
		return Collections2.transform(minorDeities, new Function<String, Deity>()
		{
			@Override
			public Deity apply(String deity)
			{
				return Stoa.getMythos().getDeity(deity);
			}
		});
	}

	public Alliance getAlliance()
	{
		return getDeity().getAlliance();
	}

	public int getKillCount()
	{
		return killCount;
	}

	public void setKillCount(int amount)
	{
		killCount = amount;
		save();
	}

	public void addKill()
	{
		killCount += 1;
		save();
	}

	public int getDeathCount()
	{
		return deaths.size();
	}

	public void addDeath()
	{
		if(deaths == null) deaths = Sets.newHashSet();
		deaths.add(Death.create(this).getId().toString());
		save();
	}

	public void addDeath(StoaCharacter attacker)
	{
		deaths.add(Death.create(this, attacker).getId().toString());
		save();
	}

	public Collection<Death> getDeaths()
	{
		if(deaths == null) deaths = Sets.newHashSet();
		return Collections2.transform(deaths, new Function<String, Death>()
		{
			@Override
			public Death apply(String s)
			{
				try
				{
					return Death.get(UUID.fromString(s));
				}
				catch(Exception ignored)
				{
				}
				return null;
			}
		});
	}

	public Collection<StoaStructure> getOwnedStructures()
	{
		return StoaStructure.find(new Predicate<StoaStructure>()
		{
			@Override
			public boolean apply(StoaStructure data)
			{
				return data.getOwner().equals(getId());
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
		int favorRegenSkill = getMeta().getSkill(Skill.Type.FAVOR_REGEN) != null ? 4 * getMeta().getSkill(Skill.Type.FAVOR_REGEN).getLevel() : 0;
		int regenRate = (int) Math.ceil(Configs.getSettingDouble("multipliers.favor") * (getDeity().getFavorRegen() + favorRegenSkill));
		if(regenRate < 30) regenRate = 30;
		return regenRate;
	}

	public void setCanPvp(boolean pvp)
	{
		StoaPlayer.of(getBukkitOfflinePlayer()).setCanPvp(pvp);
	}

	@Override
	public boolean canPvp()
	{
		return StoaPlayer.get(getMojangAccount()).canPvp();
	}

	public boolean isUsable()
	{
		return usable;
	}

	public void updateUseable()
	{
		usable = Stoa.getMythos().getDeity(this.deity) != null && Stoa.getMythos().getDeity(this.deity).getFlags().contains(Deity.Flag.PLAYABLE);
	}

	public UUID getId()
	{
		return id;
	}

	public boolean alliedTo(Participant participant)
	{
		return getAlliance().equals(participant.getRelatedCharacter().getAlliance());
	}

	public Collection<StoaTameable> getPets()
	{
		return StoaTameable.findByOwner(id);
	}

	@Override
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

	public void sendAllianceMessage(String message)
	{
		StoaChatEvent chatEvent = new StoaChatEvent(message, Stoa.getServer().getOnlineCharactersWithAlliance(getAlliance()));
		Bukkit.getPluginManager().callEvent(chatEvent);
		if(!chatEvent.isCancelled()) for(Player player : chatEvent.getRecipients())
			player.sendMessage(message);
	}

	public void chatWithAlliance(String message)
	{
		sendAllianceMessage(" " + ChatColor.GRAY + getAlliance() + "s " + ChatColor.DARK_GRAY + "" + CommonSymbol.BLACK_FLAG + " " + getDeity().getColor() + name + ChatColor.GRAY + ": " + ChatColor.RESET + message);
		Messages.info("[" + getAlliance() + "]" + name + ": " + message);
	}

	public void applyToPlayer(final Player player)
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

	// -- STATIC GETTERS/SETTERS -- //

	private static final DataAccess<UUID, StoaCharacter> DATA_ACCESS = new StoaCharacter(null);

	public static StoaCharacter get(UUID id)
	{
		return DATA_ACCESS.getDirect(id);
	}

	public static Collection<StoaCharacter> all()
	{
		return DATA_ACCESS.allDirect();
	}

	// -- UTILITY METHODS -- //

	public static StoaCharacter of(OfflinePlayer player)
	{
		return StoaPlayer.of(player).getCharacter();
	}

	public static StoaCharacter create(StoaPlayer player, String chosenDeity, String chosenName, boolean switchCharacter)
	{
		// Switch to new character
		StoaCharacter character = create(player, chosenName, chosenDeity);
		if(switchCharacter) player.switchCharacter(character);
		return character;
	}

	public static StoaCharacter create(StoaPlayer player, String charName, String charDeity)
	{
		if(!charExists(charName))
		{
			// Create the DemigodsCharacter
			return create(player, charName, Stoa.getMythos().getDeity(charDeity));
		}
		return null;
	}

	private static StoaCharacter create(final StoaPlayer player, final String charName, final Deity deity)
	{
		StoaCharacter character = new StoaCharacter();
		character.generateId();
		character.setAlive(true);
		character.setMojangAccount(player);
		character.setName(charName);
		character.setDeity(deity);
		character.setMinorDeities(new HashSet<String>(0));
		character.setUsable(true);
		character.setHealth(deity.getMaxHealth());
		character.setHunger(20);
		character.setExperience(0);
		character.setLevel(0);
		character.setKillCount(0);
		character.setLocation(player.getBukkitOfflinePlayer().getPlayer().getLocation());
		character.setMeta(StoaCharacterMeta.create(character));
		character.save();

		// Log the creation
		Messages.info(English.LOG_CHARACTER_CREATED.getLine().replace("{character}", charName).replace("{id}", character.getId().toString()).replace("{deity}", deity.getName()));

		return character;
	}

	public static void updateUsableCharacters()
	{
		for(StoaCharacter character : all())
			character.updateUseable();
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
			StoaCharacter character = StoaCharacter.of(player);
			if(character != null) character.getMeta().addFavor(character.getFavorRegen());
		}
	}
}
