package com.demigodsrpg.stoa.controller;

import com.censoredsoftware.library.data.ServerData;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.data.DataManager;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.entity.player.attribute.Notification;
import com.demigodsrpg.stoa.inventory.StoaEnderInventory;
import com.demigodsrpg.stoa.inventory.StoaPlayerInventory;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.PlayerModel;
import com.demigodsrpg.stoa.util.ChatRecorder;
import com.demigodsrpg.stoa.util.Configs;
import com.demigodsrpg.stoa.util.Messages;
import com.demigodsrpg.stoa.util.Zones;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PlayerController extends Controller<PlayerModel> implements Participant<PlayerModel>
{
	public PlayerController setPlayerName(String playerName)
	{
		model.playerName = playerName;
		return this;
	}

	public PlayerController setMortalName(String name)
	{
		model.mortalName = name;
		return this;
	}

	public PlayerController setMortalListName(String name)
	{
		model.mortalListName = name;
		return this;
	}

	public PlayerController resetCurrent()
	{
		model.currentCharacterId = null;

		if(getEntity() instanceof Player)
		{
			Player player = (Player) getEntity();
			player.setDisplayName(player.getName());
			player.setPlayerListName(player.getName());
			player.setMaxHealth(20.0);
		}

		return this;
	}

	public PlayerController setCanPvp(boolean pvp)
	{
		model.canPvp = pvp;
		return this;
	}

	public PlayerController updateCanPvp()
	{
		if(!(getEntity() instanceof Player)) return this;

		// Define variables
		final Player player = (Player) getEntity();
		final boolean inNoPvpZone = Zones.inNoPvpZone(player.getLocation());

		if(getCharacter() != null && Battle.isInBattle(getCharacter())) return this;

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
		return this;
	}

	public PlayerController setLastLoginTime(Long time)
	{
		model.lastLoginTime = new Timestamp(time);
		return this;
	}

	public PlayerController setLastLogoutTime(long time)
	{
		model.lastLogoutTime = new Timestamp(time);
		return this;
	}

	public PlayerController setCharacterSlots(int slots)
	{
		model.characterSlots = slots;
		return this;
	}

	public PlayerController addCharacterSlot()
	{
		model.characterSlots += 1;
		return this;
	}

	public PlayerController removeCharacterSlot()
	{
		model.characterSlots -= 1;
		return this;
	}

	public PlayerController setToMortal()
	{
		Player player = (Player) getEntity();
		saveCurrentCharacter();
		player.setMaxHealth(20.0);
		player.setHealth(20.0);
		player.setFoodLevel(20);
		player.setExp(0);
		player.setLevel(0);
		player.setGameMode(GameMode.SURVIVAL);
		for(PotionEffect potion : player.getActivePotionEffects())
			player.removePotionEffect(potion.getType());
		player.setDisplayName(model.mortalName);
		player.setPlayerListName(model.mortalListName);
		setMortalName(null);
		setMortalListName(null);
		applyMortalInventory();
		return this;
	}

	public PlayerController saveMortalInventory(Player player)
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
		model.mortalInventoryId = mortalInventory.getId().toString();

		// Enderchest
		StoaEnderInventory enderInventory = new StoaEnderInventory();
		Inventory enderChest = player.getEnderChest();
		enderInventory.generateId();
		enderInventory.setItems(enderChest);
		enderInventory.save();
		model.mortalEnderChestId = enderInventory.getId().toString();

		return this;
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

	@Override
	public Controller<PlayerModel> control(String modelId)
	{
		return control(modelId, new PlayerModel());
	}

	@Override
	public PlayerController refresh()
	{
		model = Iterables.getFirst(DB.from(model).where(model.mojangAccount).is(model.id()).select(), model);
		return this;
	}

	@Override
	public CharacterModel getCharacter()
	{
		if(model.currentCharacterId == null) return null;
		return new CharacterController().control(model.currentCharacterId).getModel();
	}

	@Override
	public Boolean canPvp()
	{
		return model.canPvp;
	}

	@Override
	public Location getCurrentLocation()
	{
		if(getEntity() == null) return null;
		return getEntity().getLocation();
	}

	@Override
	public LivingEntity getEntity()
	{
		return Bukkit.getPlayer(model.mojangAccount);
	}
}
