package com.demigodsrpg.stoa.entity.player;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.censoredsoftware.library.data.ServerData;
import com.censoredsoftware.library.mcidprovider.McIdProvider;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.controller.ChatRecorder;
import com.demigodsrpg.stoa.data.DataAccess;
import com.demigodsrpg.stoa.data.DataManager;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.entity.player.attribute.Notification;
import com.demigodsrpg.stoa.inventory.StoaEnderInventory;
import com.demigodsrpg.stoa.inventory.StoaPlayerInventory;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.location.StoaRegion;
import com.demigodsrpg.stoa.util.Configs;
import com.demigodsrpg.stoa.util.Messages;
import com.demigodsrpg.stoa.util.Zones;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.minecraft.util.io.netty.util.concurrent.BlockingOperationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StoaPlayer extends DataAccess<UUID, StoaPlayer>
{
	private UUID mojangAccount;
	private String playerName;
	private String mortalName, mortalListName;
	private boolean canPvp;
	private Long lastLoginTime, lastLogoutTime;
	private String currentDeityName;
	private int characterSlots;
	private UUID current;
	private UUID previous;
	private UUID mortalInventory, mortalEnderInventory;
	private ChatRecorder chatRecording;

	public StoaPlayer()
	{
		characterSlots = Configs.getSettingInt("character.default_character_slots");
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static StoaPlayer of(UUID mojangAccount, ConfigurationSection conf)
	{
		StoaPlayer player = new StoaPlayer();
		player.mojangAccount = mojangAccount;
		player.playerName = conf.getString("playerName");
		if(conf.isString("mortalName")) player.mortalName = conf.getString("mortalName");
		if(conf.isString("mortalListName")) player.mortalListName = conf.getString("mortalListName");
		if(conf.isBoolean("canPvp")) player.canPvp = conf.getBoolean("canPvp");
		if(conf.isLong("lastLoginTime")) player.lastLoginTime = conf.getLong("lastLoginTime");
		if(conf.isLong("lastLogoutTime")) player.lastLogoutTime = conf.getLong("lastLogoutTime");
		if(conf.getString("currentDeityName") != null) player.currentDeityName = conf.getString("currentDeityName");
		if(conf.isInt("characterSlots")) player.characterSlots = conf.getInt("characterSlots");
		if(conf.getString("current") != null) player.current = UUID.fromString(conf.getString("current"));
		if(conf.getString("previous") != null) player.previous = UUID.fromString(conf.getString("previous"));
		if(conf.getString("mortalInventory") != null) player.mortalInventory = UUID.fromString(conf.getString("mortalInventory"));
		if(conf.getString("mortalEnderInventory") != null) player.mortalEnderInventory = UUID.fromString(conf.getString("mortalEnderInventory"));
		return player;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("playerName", playerName);
		map.put("characterSlots", characterSlots);
		try
		{
			map.put("canPvp", canPvp);
			map.put("lastLoginTime", lastLoginTime);
			map.put("lastLogoutTime", lastLogoutTime);
		}
		catch(Exception ignored)
		{
		}
		if(mortalName != null) map.put("mortalName", mortalName);
		if(mortalListName != null) map.put("mortalListName", mortalListName);
		if(currentDeityName != null) map.put("currentDeityName", currentDeityName);
		if(current != null) map.put("current", current.toString());
		if(previous != null) map.put("previous", previous.toString());
		if(mortalInventory != null) map.put("mortalInventory", mortalInventory.toString());
		if(mortalEnderInventory != null) map.put("mortalEnderInventory", mortalEnderInventory.toString());
		return map;
	}

	public void setPlayerName(String player)
	{
		this.playerName = player;
	}

	void setMojangAccount(UUID account)
	{
		this.mojangAccount = account;
	}

	public void setMortalName(String name)
	{
		mortalName = name;
	}

	public String getMortalName()
	{
		return mortalName != null ? mortalName : playerName;
	}

	public void setMortalListName(String name)
	{
		mortalListName = name;
	}

	public String getMortalListName()
	{
		return mortalListName != null ? mortalListName : playerName;
	}

	public void resetCurrent()
	{
		this.current = null;
		this.currentDeityName = null;

		if(getBukkitOfflinePlayer().isOnline())
		{
			getBukkitOfflinePlayer().getPlayer().setDisplayName(getBukkitOfflinePlayer().getName());
			getBukkitOfflinePlayer().getPlayer().setPlayerListName(getBukkitOfflinePlayer().getName());
			getBukkitOfflinePlayer().getPlayer().setMaxHealth(20.0);
		}
	}

	public void setCanPvp(boolean pvp)
	{
		this.canPvp = pvp;
		save();
	}

	public void updateCanPvp()
	{
		if(!getBukkitOfflinePlayer().isOnline()) return;

		// Define variables
		final Player player = getBukkitOfflinePlayer().getPlayer();
		final boolean inNoPvpZone = Zones.inNoPvpZone(player.getLocation());

		if(getCharacter() != null && Battle.isInBattle(getCharacter())) return;

		if(!canPvp() && !inNoPvpZone)
		{
			setCanPvp(true);
			player.sendMessage(ChatColor.GRAY + English.UNSAFE_FROM_PVP.getLine());
		}
		else if(!inNoPvpZone)
		{
			setCanPvp(true);
			ServerData.remove(DataManager.DATA_MANAGER, player.getName(), "pvp_cooldown");
		}
		else if(canPvp() && !ServerData.exists(DataManager.DATA_MANAGER, player.getName(), "pvp_cooldown"))
		{
			int delay = Configs.getSettingInt("zones.pvp_area_delay_time");
			ServerData.put(DataManager.DATA_MANAGER, player.getName(), "pvp_cooldown", true, delay, TimeUnit.SECONDS);

			Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if(Zones.inNoPvpZone(player.getLocation()))
					{
						if(getCharacter() != null && Battle.isInBattle(getCharacter())) return;
						setCanPvp(false);
						player.sendMessage(ChatColor.GRAY + English.SAFE_FROM_PVP.getLine());
					}
				}
			}, (delay * 20));
		}
	}

	// TODO Fix this so it doesn't run on the main thread.
	public OfflinePlayer getBukkitOfflinePlayer() throws BlockingOperationException
	{
		return Bukkit.getOfflinePlayer(playerName);
	}

	public void setLastLoginTime(Long time)
	{
		this.lastLoginTime = time;
		save();
	}

	public Long getLastLoginTime()
	{
		return this.lastLoginTime;
	}

	public void setLastLogoutTime(long time)
	{
		this.lastLogoutTime = time;
		save();
	}

	public Long getLastLogoutTime()
	{
		return this.lastLogoutTime;
	}

	public void setCharacterSlots(int slots)
	{
		characterSlots = slots;
	}

	public void addCharacterSlot()
	{
		characterSlots += 1;
	}

	public void removeCharacterSlot()
	{
		characterSlots -= 1;
	}

	public int getCharacterSlots()
	{
		return characterSlots;
	}

	public void setToMortal()
	{
		Player player = getBukkitOfflinePlayer().getPlayer();
		saveCurrentCharacter();
		player.setMaxHealth(20.0);
		player.setHealth(20.0);
		player.setFoodLevel(20);
		player.setExp(0);
		player.setLevel(0);
		player.setGameMode(GameMode.SURVIVAL);
		for(PotionEffect potion : player.getActivePotionEffects())
			player.removePotionEffect(potion.getType());
		player.setDisplayName(getMortalName());
		player.setPlayerListName(getMortalListName());
		setMortalName(null);
		setMortalListName(null);
		applyMortalInventory();
	}

	public void saveMortalInventory(Player player)
	{
		// Player inventory
		StoaPlayerInventory mortalInventory = new StoaPlayerInventory();
		PlayerInventory inventory = player.getInventory();
		mortalInventory.generateId();
		if(inventory.getHelmet() != null) mortalInventory.setHelmet(inventory.getHelmet());
		if(inventory.getChestplate() != null) mortalInventory.setChestplate(inventory.getChestplate());
		if(inventory.getLeggings() != null) mortalInventory.setLeggings(inventory.getLeggings());
		if(inventory.getBoots() != null) mortalInventory.setBoots(inventory.getBoots());
		mortalInventory.setItems(inventory);
		mortalInventory.save();
		this.mortalInventory = mortalInventory.getId();

		// Enderchest
		StoaEnderInventory enderInventory = new StoaEnderInventory();
		Inventory enderChest = player.getEnderChest();
		enderInventory.generateId();
		enderInventory.setItems(enderChest);
		enderInventory.save();
		this.mortalEnderInventory = enderInventory.getId();

		save();
	}

	public void saveCurrentCharacter()
	{
		// Update the current character
		final Player player = getBukkitOfflinePlayer().getPlayer();
		final StoaCharacter character = getCharacter();

		if(character != null)
		{
			// Set to inactive and update previous
			character.setActive(false);
			this.previous = character.getId();

			// Set the values
			character.setHealth(player.getHealth() >= character.getMaxHealth() ? character.getMaxHealth() : player.getHealth());
			character.setHunger(player.getFoodLevel());
			character.setLevel(player.getLevel());
			character.setExperience(player.getExp());
			character.setLocation(player.getLocation());
			Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if(player.getBedSpawnLocation() != null) character.setBedSpawn(player.getBedSpawnLocation());
				}
			}, 1);
			character.setGameMode(player.getGameMode());
			character.setPotionEffects(player.getActivePotionEffects());
			character.saveInventory();

			// Disown pets
			StoaTameable.disownPets(character.getName());

			// Save it
			character.save();
		}
	}

	public void switchCharacter(final StoaCharacter newChar)
	{
		final Player player = getBukkitOfflinePlayer().getPlayer();

		if(!newChar.getPlayerName().equals(this.playerName))
		{
			player.sendMessage(ChatColor.RED + "You can't do that.");
			return;
		}

		// Save the current character
		saveCurrentCharacter();

		// Set new character to active and other info
		this.current = newChar.getId();
		currentDeityName = newChar.getDeity().getName();

		// Apply the new character
		newChar.applyToPlayer(player);

		// Teleport them
		try
		{
			player.teleport(newChar.getLocation());
		}
		catch(Exception e)
		{
			Messages.warning("There was a problem while teleporting a player to their character.");
		}

		// Save instances
		save();
		newChar.save();
	}

	public boolean canPvp()
	{
		return this.canPvp;
	}

	public String getPlayerName()
	{
		return playerName;
	}

	public UUID getMojangAccount()
	{
		return mojangAccount;
	}

	public String getCurrentDeityName()
	{
		return currentDeityName;
	}

	public StoaRegion getRegion()
	{
		if(getBukkitOfflinePlayer().isOnline()) return StoaRegion.at(getBukkitOfflinePlayer().getPlayer().getLocation());
		return StoaRegion.at(getCharacter().getLocation());
	}

	public boolean isACharacter()
	{
		return getCharacter() != null;
	}

	public StoaCharacter getCharacter()
	{
		if(current == null) return null;
		StoaCharacter character = StoaCharacter.get(current);
		if(character != null && character.isUsable()) return character;
		return null;
	}

	public StoaCharacter getPreviousCharacter()
	{
		if(previous == null) return null;
		return StoaCharacter.get(previous);
	}

	public Set<StoaCharacter> getCharacters()
	{
		return Sets.newHashSet(Collections2.filter(StoaCharacter.all(), new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character != null && character.getMojangAccount().equals(mojangAccount) && character.isUsable();
			}
		}));
	}

	public Set<StoaCharacter> getUsableCharacters()
	{
		return Sets.filter(getCharacters(), new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isUsable();
			}
		});
	}

	public StoaPlayerInventory getMortalInventory()
	{
		return StoaPlayerInventory.get(mortalInventory);
	}

	public StoaEnderInventory getMortalEnderInventory()
	{
		return StoaEnderInventory.get(mortalEnderInventory);
	}

	public void applyMortalInventory()
	{
		if(getMortalInventory() == null) mortalInventory = StoaPlayerInventory.createEmpty().getId();
		if(getMortalEnderInventory() == null) mortalEnderInventory = StoaEnderInventory.createEmpty().getId();
		getMortalInventory().setToPlayer(getBukkitOfflinePlayer().getPlayer());
		getMortalEnderInventory().setToPlayer(getBukkitOfflinePlayer().getPlayer());
		mortalInventory = null;
		mortalEnderInventory = null;
	}

	public boolean canMakeCharacter()
	{
		return getUsableCharacters().size() < getCharacterSlots();
	}

	public boolean canUseCurrent()
	{
		if(getCharacter() == null || !getCharacter().isUsable())
		{
			getBukkitOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Your current character was unable to init!");
			getBukkitOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Please contact the server administrator immediately.");
			return false;
		}
		else return getBukkitOfflinePlayer().isOnline();
	}

	public void remove()
	{
		// First we need to kick the player if they're online
		if(getBukkitOfflinePlayer().isOnline()) getBukkitOfflinePlayer().getPlayer().kickPlayer(ChatColor.RED + "Your player save has been cleared.");

		// Remove characters
		for(StoaCharacter character : getCharacters())
			character.remove();

		// Now we clear the DemigodsPlayer save itself
		super.remove();
	}

	@Override
	public UUID getId()
	{
		return getMojangAccount();
	}

	public void sendNotification(Notification notification)
	{
		if(getCharacter() != null) Notification.sendNotification(getCharacter(), notification);
	}

	/**
	 * Starts recording recording the <code>player</code>'s chat.
	 */
	public void startRecording()
	{
		chatRecording = ChatRecorder.Util.startRecording(getBukkitOfflinePlayer().getPlayer());
	}

	/**
	 * Stops recording and sends all messages that have been recorded thus far to the player.
	 *
	 * @param display if true, the chat will be sent to the player
	 */
	public List<String> stopRecording(boolean display)
	{
		Player player = getBukkitOfflinePlayer().getPlayer();
		// Handle recorded chat
		if(chatRecording != null && chatRecording.isRecording())
		{
			// Send held back chat
			List<String> messages = chatRecording.stop();
			if(messages.size() > 0 && display)
			{
				player.sendMessage(" ");
				if(messages.size() == 1)
				{
					player.sendMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + English.HELD_BACK_MESSAGE.getLine());
				}
				else
				{
					player.sendMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + English.HELD_BACK_MESSAGES.getLine().replace("{size}", "" + messages.size()));
				}
				for(String message : messages)
					player.sendMessage(message);
			}

			return messages;
		}
		return null;
	}

	public static StoaPlayer create(final OfflinePlayer player)
	{
		StoaPlayer playerSave = new StoaPlayer();
		playerSave.setMojangAccount(McIdProvider.getId(player.getName()));
		playerSave.setPlayerName(player.getName());
		playerSave.setLastLoginTime(player.getLastPlayed());
		playerSave.setCanPvp(true);
		playerSave.save();

		// Log the creation
		Messages.info(English.LOG_PLAYER_CREATED.getLine().replace("{player}", player.getName()).replace("{id}", McIdProvider.getId(player.getName()).toString()));

		return playerSave;
	}

	public static StoaPlayer of(final OfflinePlayer player)
	{
		UUID id = McIdProvider.getId(player.getName());
		if(id == null) throw new NullPointerException(player.getName() + " is not a premium player.");
		StoaPlayer found = get(id);
		if(found == null) return create(player);
		return found;
	}

	public static StoaPlayer getFromName(final String playerName)
	{
		try
		{
			return Iterables.find(all(), new Predicate<StoaPlayer>()
			{
				@Override
				public boolean apply(StoaPlayer stoaPlayer)
				{
					return stoaPlayer.getPlayerName().equals(playerName);
				}
			});
		}
		catch(NoSuchElementException ignored)
		{
		}
		return null;
	}

	private static final DataAccess<UUID, StoaPlayer> DATA_ACCESS = new StoaPlayer();

	public static StoaPlayer get(UUID mojangAccount)
	{
		return DATA_ACCESS.getDirect(mojangAccount);
	}

	public static Collection<StoaPlayer> all()
	{
		return DATA_ACCESS.allDirect();
	}

	/**
	 * Returns true if the <code>player</code> is currently immortal.
	 *
	 * @param player the player to check.
	 * @return boolean
	 */
	public static boolean isImmortal(Player player)
	{
		StoaCharacter character = of(player).getCharacter();
		return character != null && character.isUsable() && character.isActive();
	}

	/**
	 * Returns true if <code>player</code> has a character with the name <code>charName</code>.
	 *
	 * @param player   the player to check.
	 * @param charName the charName to check with.
	 * @return boolean
	 */
	public static boolean hasCharName(Player player, String charName)
	{
		for(StoaCharacter character : of(player).getCharacters())
			if(character.getName().equalsIgnoreCase(charName)) return true;
		return false;
	}
}
