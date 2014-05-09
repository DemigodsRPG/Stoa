package com.demigodsrpg.stoa.mythos;

import com.censoredsoftware.library.command.CommandManager;
import com.censoredsoftware.library.data.thread.SyncAsyncRunnable;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.structure.StoaStructureType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.Collections;
import java.util.Set;

public class MythosSet implements Mythos
{
	private final Mythos PRIMARY;
	private final ImmutableSet<Mythos> SET;
	private final String[] INCOMPATIBLE;
	private final ImmutableSet<DivineItem> DIVINE_ITEMS;
	private final ImmutableSet<Alliance> ALLIANCES;
	private final ImmutableSet<Deity> DEITIES;
	private final ImmutableSet<StoaStructureType> STRUCTURES;
	private final ImmutableSet<Listener> LISTENERS;
	private final ImmutableSet<Permission> PERMISSIONS;
	private final ImmutableSet<CommandManager> COMMANDS;
	private final ImmutableSet<SyncAsyncRunnable> TRIGGERS;

	public MythosSet(Mythos primaryMythos, Set<Mythos> mythosSet)
	{
		PRIMARY = primaryMythos;
		mythosSet.add(PRIMARY);
		if(useBaseGame()) mythosSet.add(StoaPlugin.getInst().getBaseGame());
		SET = ImmutableSet.copyOf(Collections2.transform(mythosSet, new Function<Mythos, Mythos>()
		{
			@Override
			public Mythos apply(Mythos mythos)
			{
				if(!mythos.equals(PRIMARY)) mythos.setSecondary();
				return mythos;
			}
		}));

		Set<String> incompatibleSet = Sets.newHashSet();

		Set<DivineItem> divineItems = Sets.newHashSet();
		Set<Alliance> alliance = Sets.newHashSet();
		Set<Deity> deity = Sets.newHashSet();
		Set<StoaStructureType> structureType = Sets.newHashSet();
		Set<Listener> listener = Sets.newHashSet();
		Set<Permission> permission = Sets.newHashSet();
		Set<CommandManager> command = Sets.newHashSet();
		Set<SyncAsyncRunnable> trigger = Sets.newHashSet();

		for(Mythos mythos : SET)
		{
			Collections.addAll(incompatibleSet, mythos.getIncompatible());

			divineItems.addAll(mythos.getDivineItems());
			alliance.addAll(mythos.getAlliances());
			deity.addAll(mythos.getDeities());
			structureType.addAll(mythos.getStructures());
			listener.addAll(mythos.getListeners());
			permission.addAll(mythos.getPermissions());
			command.addAll(mythos.getCommands());
			trigger.addAll(mythos.getSyncAsyncTasks());
		}

		String[] incompatibleWorking = new String[incompatibleSet.size()];
		int count = 0;
		for(String incompatible : incompatibleSet)
		{
			incompatibleWorking[count] = incompatible;
			count++;
		}
		INCOMPATIBLE = incompatibleWorking;

		DIVINE_ITEMS = ImmutableSet.copyOf(divineItems);
		ALLIANCES = ImmutableSet.copyOf(alliance);
		DEITIES = ImmutableSet.copyOf(deity);
		STRUCTURES = ImmutableSet.copyOf(structureType);
		LISTENERS = ImmutableSet.copyOf(listener);
		PERMISSIONS = ImmutableSet.copyOf(permission);
		COMMANDS = ImmutableSet.copyOf(command);
		TRIGGERS = ImmutableSet.copyOf(trigger);
	}

	@Override
	public String getTitle()
	{
		return PRIMARY.getTitle() + " and additional Mythos";
	}

	@Override
	public String getTagline()
	{
		return "A generated Mythos made to combine all secondary Mythos.";
	}

	@Override
	public String getAuthor()
	{
		return "Generated by Demigods";
	}

	@Override
	public Boolean isPrimary()
	{
		return true;
	}

	@Override
	public Boolean allowSecondary()
	{
		return PRIMARY.allowSecondary();
	}

	@Override
	public String[] getIncompatible()
	{
		return INCOMPATIBLE;
	}

	@Override
	public Boolean useBaseGame()
	{
		return PRIMARY.useBaseGame();
	}

	@Override
	public ImmutableCollection<DivineItem> getDivineItems()
	{
		return DIVINE_ITEMS;
	}

	@Override
	public DivineItem getDivineItem(String itemName)
	{
		return Mythos.Util.getDivineItem(this, itemName);
	}

	@Override
	public DivineItem getDivineItem(final ItemStack itemStack)
	{
		return Mythos.Util.getDivineItem(this, itemStack);
	}

	@Override
	public boolean itemHasFlag(ItemStack itemStack, DivineItem.Flag flag)
	{
		return Mythos.Util.itemHasFlag(this, itemStack, flag);
	}

	@Override
	public ImmutableCollection<Alliance> getAlliances()
	{
		return ALLIANCES;
	}

	@Override
	public Alliance getAlliance(final String allianceName)
	{
		return Mythos.Util.getAlliance(this, allianceName);
	}

	@Override
	public ImmutableCollection<Deity> getDeities()
	{
		return DEITIES;
	}

	@Override
	public Deity getDeity(final String deityName)
	{
		return Mythos.Util.getDeity(this, deityName);
	}

	@Override
	public ImmutableCollection<StoaStructureType> getStructures()
	{
		return STRUCTURES;
	}

	@Override
	public StoaStructureType getStructure(final String structureName)
	{
		return Mythos.Util.getStructure(this, structureName);
	}

	@Override
	public Boolean levelSeperateSkills()
	{
		return PRIMARY.levelSeperateSkills();
	}

	@Override
	public ImmutableCollection<Listener> getListeners()
	{
		return LISTENERS;
	}

	@Override
	public ImmutableCollection<Permission> getPermissions()
	{
		return PERMISSIONS;
	}

	@Override
	public ImmutableCollection<CommandManager> getCommands()
	{
		return COMMANDS;
	}

	@Override
	public ImmutableCollection<SyncAsyncRunnable> getSyncAsyncTasks()
	{
		return TRIGGERS;
	}

	@Override
	public void setSecondary()
	{
	}

	@Override
	public void lock()
	{
	}

	public boolean contains(final String mythosTitle)
	{
		return PRIMARY.getTitle().equals(mythosTitle) || Iterables.any(SET, new Predicate<Mythos>()
		{
			@Override
			public boolean apply(Mythos mythos)
			{
				return mythos.getTitle().equals(mythosTitle);
			}
		});
	}
}
