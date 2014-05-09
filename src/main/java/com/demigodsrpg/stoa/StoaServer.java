package com.demigodsrpg.stoa;

import com.demigodsrpg.stoa.data.DataManager;
import com.demigodsrpg.stoa.data.StoaWorld;
import com.demigodsrpg.stoa.data.TaskManager;
import com.demigodsrpg.stoa.data.WorldDataManager;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.entity.player.StoaPlayer;
import com.demigodsrpg.stoa.entity.player.attribute.Skill;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.mythos.Mythos;
import com.demigodsrpg.stoa.mythos.MythosSet;
import com.demigodsrpg.stoa.structure.StoaStructureType;
import com.demigodsrpg.stoa.util.Messages;
import com.demigodsrpg.stoa.util.Zones;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.MetricsLite;

import java.util.*;

public class StoaServer
{
	// Mythos
	private final Mythos mythos;

	StoaServer()
	{
		mythos = loadMythos();
	}

	public Mythos getMythos()
	{
		return mythos;
	}

	// Load everything else.
	protected boolean init()
	{
		// Initialize metrics
		try
		{
			(new MetricsLite(StoaPlugin.getInst())).start();
		}
		catch(Exception ignored)
		{
			// ignored
		}

		try
		{
			if(getMythos() == null)
			{
				Messages.severe("Demigods was unable to load a Mythos.");
				Messages.severe("Please install a Mythos plugin or place the default Demigods-Greek.jar into the plugins\\Demigods\\addons directory.");
				return false;
			}

			if(!StoaPlugin.getInst().getServer().getOnlineMode())
			{
				Messages.warning("We depend on Mojang's auth servers for player id.");
				Messages.warning("Players who are not premium may be kicked from the game.");
			}

			// Check for world load errors
			if(loadWorlds() > 0)
			{
				Messages.warning("Demigods was unable to confirm any worlds.");
				Messages.warning("This may be caused by misspelled world names.");
				Messages.warning("Multi-world plugins can cause this message, and in that case this may be a false alarm.");
			}

			// Load the data
			DataManager.initAllData();

			// Load listeners, commands, permissions, and the scoreboard
			loadListeners();
			loadPermissions(true);

			// Update characters
			StoaCharacter.updateUsableCharacters();

			// Start threads
			TaskManager.startThreads();

			// Regenerate structures (on a delay)
			Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable()
			{
				@Override
				public void run()
				{
					StoaStructureType.Util.regenerateStructures();
				}
			}, 120);

			if(isRunningSpigot())
			{
				Messages.info(("Spigot found, extra API features enabled."));
			}
			else Messages.warning(("Without Spigot, some features may not work."));

			// Clean skills
			for(StoaCharacter character : StoaCharacter.all())
				character.getMeta().cleanSkills();

			return true;
		}
		catch(Exception errored)
		{
			errored.printStackTrace();
		}
		return false;
	}

	protected Mythos loadMythos()
	{
		ServicesManager servicesManager = StoaPlugin.getInst().getServer().getServicesManager();
		Collection<RegisteredServiceProvider<Mythos>> mythosProviders = servicesManager.getRegistrations(Mythos.class);
		if(Iterables.any(mythosProviders, new Predicate<RegisteredServiceProvider<Mythos>>()
		{
			@Override
			public boolean apply(RegisteredServiceProvider<Mythos> mythosProvider)
			{
				return mythosProvider.getProvider().isPrimary();
			}
		}))
		{
			// Decide the primary Mythos
			Mythos reiningMythos = null;
			int reiningPriority = 5;

			Set<Mythos> workingSet = Sets.newHashSet();
			for(RegisteredServiceProvider<Mythos> mythosProvider : mythosProviders)
			{
				Mythos mythos = mythosProvider.getProvider();
				workingSet.add(mythos);
				Messages.info("The " + mythos.getTitle() + " Mythos has enabled!");
				Messages.info("-> Created by " + mythos.getAuthor() + ".");
				Messages.info("-> " + mythos.getTagline());
				if(!mythosProvider.getProvider().isPrimary()) continue;
				if(mythosProvider.getPriority().ordinal() < reiningPriority) // not really sure how Bukkit handles priority, presuming the same way as EventPriority
				{
					reiningMythos = mythos;
					reiningPriority = mythosProvider.getPriority().ordinal();
				}
			}

			if(reiningMythos != null)
			{
				workingSet.remove(reiningMythos);
				if(reiningMythos.useBaseGame() || reiningMythos.allowSecondary() && !workingSet.isEmpty()) reiningMythos = new MythosSet(reiningMythos, reiningMythos.allowSecondary() ? workingSet : new HashSet<Mythos>());
				reiningMythos.lock();
				return reiningMythos;
			}
		}
		return null;
	}

	protected int loadWorlds()
	{
		return Zones.init();
	}

	protected void loadListeners()
	{
		PluginManager register = Bukkit.getServer().getPluginManager();

		// Mythos
		for(Listener listener : getMythos().getListeners())
			register.registerEvents(listener, StoaPlugin.getInst());

		// Abilities
		for(Ability ability : Ability.Util.getLoadedAbilities())
			if(ability.getListener() != null) register.registerEvents(ability.getListener(), StoaPlugin.getInst());

		// Structures
		for(StoaStructureType structureType : Collections2.filter(getMythos().getStructures(), new Predicate<StoaStructureType>()
		{
			@Override
			public boolean apply(StoaStructureType structureType)
			{
				return structureType.getUniqueListener() != null;
			}
		}))
			if(structureType.getUniqueListener() != null) register.registerEvents(structureType.getUniqueListener(), StoaPlugin.getInst());

		// Divine Items
		for(DivineItem divineItem : getMythos().getDivineItems())
		{
			if(divineItem.getUniqueListener() != null) register.registerEvents(divineItem.getUniqueListener(), StoaPlugin.getInst());
			if(divineItem.getRecipe() != null) StoaPlugin.getInst().getServer().addRecipe(divineItem.getRecipe());
		}

		// Quit reason.
		// TODO Bukkit.getServer().getLogger().addHandler(new QuitReasonHandler());
	}

	protected void loadPermissions(final boolean load)
	{
		final PluginManager register = Bukkit.getServer().getPluginManager();

		// Mythos
		for(Permission permission : getMythos().getPermissions())
		{
			// catch errors to avoid any possible buggy permissions
			try
			{
				for(Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
					registerPermission(register, new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE), load);
				registerPermission(register, permission, load);
			}
			catch(Exception ignored)
			{
				// ignored
			}
		}

		// Alliances, Deities, and Abilities
		for(final Alliance alliance : getMythos().getAlliances())
		{
			// catch errors to avoid any possible buggy permissions
			try
			{
				registerPermission(register, new Permission(alliance.getPermission(), "The permission to use the " + alliance.getName() + " alliance.", alliance.getPermissionDefault(), new HashMap<String, Boolean>()
				{
					{
						for(Deity deity : Alliance.Util.getLoadedDeitiesInAlliance(alliance))
						{
							registerPermission(register, new Permission(deity.getPermission(), alliance.getPermissionDefault()), load);
							put(deity.getPermission(), deity.getPermissionDefault().equals(PermissionDefault.NOT_OP) ? alliance.getPermissionDefault().equals(PermissionDefault.TRUE) : deity.getPermissionDefault().equals(PermissionDefault.TRUE));
						}
					}
				}), load);
			}
			catch(Exception ignored)
			{
				// ignored
			}
		}

		// Skill types
		for(Skill.Type skill : Skill.Type.values())
			registerPermission(register, skill.getPermission(), load);
	}

	void uninit()
	{
		if(StoaPlugin.getReady())
		{
			// Save all the data.
			DataManager.saveAllData();
		}

		// Cancel all threads, event calls, and unregister permissions.
		try
		{
			TaskManager.stopThreads();
			HandlerList.unregisterAll(StoaPlugin.getInst());
			unloadPermissions();
		}
		catch(Exception ignored)
		{
			// ignored
		}
	}

	void registerPermission(PluginManager register, Permission permission, boolean load)
	{
		if(load) register.addPermission(permission);
		else register.removePermission(permission);
	}

	void unloadPermissions()
	{
		loadPermissions(false);
	}

	public static boolean isRunningSpigot()
	{
		try
		{
			Bukkit.getServer().getWorlds().get(0).spigot();
			return true;
		}
		catch(Exception ignored)
		{
			// ignored
		}
		return false;
	}

	// -- PLAYER -- //

	public Collection<StoaPlayer> getAllPlayers()
	{
		return StoaPlayer.all();
	}

	public Collection<StoaPlayer> getOnlinePlayers()
	{
		return Collections2.filter(StoaPlayer.all(), new Predicate<StoaPlayer>()
		{
			@Override
			public boolean apply(StoaPlayer player)
			{
				return player.getBukkitOfflinePlayer().isOnline();
			}
		});
	}

	// -- MORTAL -- //

	public Collection<OfflinePlayer> getMortals()
	{
		return Collections2.transform(Collections2.filter(StoaPlayer.all(), new Predicate<StoaPlayer>()
		{
			@Override
			public boolean apply(StoaPlayer player)
			{
				StoaCharacter character = player.getCharacter();
				return character == null || !character.isUsable() || !character.isActive();
			}
		}), new Function<StoaPlayer, OfflinePlayer>()
		{
			@Override
			public OfflinePlayer apply(StoaPlayer player)
			{
				return player.getBukkitOfflinePlayer();
			}
		});
	}

	public Set<Player> getOnlineMortals()
	{
		return Sets.filter(Sets.newHashSet(Bukkit.getOnlinePlayers()), new Predicate<Player>()
		{
			@Override
			public boolean apply(Player player)
			{
				StoaCharacter character = StoaPlayer.of(player).getCharacter();
				return character == null || !character.isUsable() || !character.isActive();
			}
		});
	}

	// -- CHARACTER -- //

	public StoaCharacter getCharacter(final String name)
	{
		try
		{
			return Iterators.find(StoaCharacter.all().iterator(), new Predicate<StoaCharacter>()
			{
				@Override
				public boolean apply(StoaCharacter loaded)
				{
					return loaded.getName().equalsIgnoreCase(name);
				}
			});
		}
		catch(Exception ignored)
		{
		}
		return null;
	}

	public Collection<StoaCharacter> getActiveCharacters()
	{
		return Collections2.filter(StoaCharacter.all(), new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isUsable() && character.isActive();
			}
		});
	}

	public Collection<StoaCharacter> getUsableCharacters()
	{
		return Collections2.filter(StoaCharacter.all(), new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isUsable();
			}
		});
	}

	public Collection<StoaCharacter> getOnlineCharactersWithDeity(final String deity)
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isActive() && character.getBukkitOfflinePlayer().isOnline() && character.getDeity().getName().equalsIgnoreCase(deity);
			}
		});
	}

	public Collection<StoaCharacter> getOnlineCharactersWithAbility(final String abilityName)
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				if(character.isActive() && character.getBukkitOfflinePlayer().isOnline())
				{
					for(Ability abilityToCheck : character.getDeity().getAbilities())
						if(abilityToCheck.getName().equalsIgnoreCase(abilityName)) return true;
				}
				return false;
			}
		});
	}

	public Collection<StoaCharacter> getOnlineCharactersWithAlliance(final Alliance alliance)
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isActive() && character.getBukkitOfflinePlayer().isOnline() && character.getAlliance().equals(alliance);
			}
		});
	}

	public Collection<StoaCharacter> getOnlineCharactersWithoutAlliance(final Alliance alliance)
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isActive() && character.getBukkitOfflinePlayer().isOnline() && !character.getAlliance().equals(alliance);
			}
		});
	}

	public Collection<StoaCharacter> getOnlineCharactersBelowAscension(final int ascension)
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isActive() && character.getBukkitOfflinePlayer().isOnline() && character.getMeta().getAscensions() < ascension;
			}
		});
	}

	public Collection<StoaCharacter> getOnlineCharacters()
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isActive() && character.getBukkitOfflinePlayer().isOnline();
			}
		});
	}

	public Collection<StoaCharacter> getAllCharactersWithDeity(final String deity)
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isActive() && character.getDeity().getName().equalsIgnoreCase(deity);
			}
		});
	}

	public Collection<StoaCharacter> getAllCharactersWithAlliance(final Alliance alliance)
	{
		return getCharactersWithPredicate(new Predicate<StoaCharacter>()
		{
			@Override
			public boolean apply(StoaCharacter character)
			{
				return character.isActive() && character.getAlliance().equals(alliance);
			}
		});
	}

	public Collection<StoaCharacter> getCharactersWithPredicate(Predicate<StoaCharacter> predicate)
	{
		return Collections2.filter(getUsableCharacters(), predicate);
	}

	// -- WORLD -- //

	public StoaWorld getWorld(String name)
	{
		return WorldDataManager.getWorld(name);
	}

	public Collection<StoaWorld> getWorlds()
	{
		return WorldDataManager.getWorlds();
	}
}
