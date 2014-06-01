package com.demigodsrpg.stoa.data;

import com.censoredsoftware.library.data.ServerData;
import com.censoredsoftware.library.util.Threads;
import com.censoredsoftware.library.util.Times;
import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.entity.player.StoaPlayer;
import com.demigodsrpg.stoa.entity.player.attribute.Notification;
import com.demigodsrpg.stoa.structure.StoaStructureType;
import com.demigodsrpg.stoa.util.Configs;
import com.demigodsrpg.stoa.util.MessageUtil;
import com.demigodsrpg.stoa.util.ZoneUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings("deprecation")
public class TaskManager
{
	private static final boolean SAVE_ALERT = Configs.getSettingBoolean("saving.console_alert");
	private static final BukkitRunnable SYNC, ASYNC, SAVE, FAVOR;

	static
	{
		SYNC = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				// Update online players
				for(Player player : Bukkit.getOnlinePlayers())
				{
					if(ZoneUtil.inNoStoaZone(player.getLocation())) continue;
					StoaPlayer.of(player).updateCanPvp();
				}

				// Update Battles
				Battle.updateBattles();

				// Update Battle Particles
				Battle.updateBattleParticles();
			}
		};
		ASYNC = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				// Update Timed Data
				ServerData.clearExpired(DataManager.DATA_MANAGER);

				// Update Notifications
				Notification.updateNotifications();
			}
		};
		SAVE = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				// Save time for reference after saving
				long time = System.currentTimeMillis();

				// Save data
				DataManager.saveAllData();

				// Send the save message to the console
				if(SAVE_ALERT) MessageUtil.info(Bukkit.getOnlinePlayers().length + " of " + Stoa.getServer().getAllPlayers().size() + " total players saved in " + Times.getSeconds(time) + " seconds.");
			}
		};
		FAVOR = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				// Update Favor
				StoaCharacter.updateFavor();

				// Update Sanctity
				StoaStructureType.Util.updateSanctity();
			}
		};
	}

	public static void startThreads()
	{
		BukkitScheduler scheduler = Bukkit.getScheduler();

		// Start sync demigods runnable
		scheduler.scheduleSyncRepeatingTask(StoaPlugin.getInst(), SYNC, 20, 20);
		MessageUtil.sendDebug("Main Demigods SYNC runnable enabled...");

		// Start async demigods runnable
		scheduler.scheduleAsyncRepeatingTask(StoaPlugin.getInst(), ASYNC, 20, 20);
		MessageUtil.sendDebug("Main Demigods ASYNC runnable enabled...");

		// Start favor runnable
		scheduler.scheduleAsyncRepeatingTask(StoaPlugin.getInst(), FAVOR, 20, (Configs.getSettingInt("regeneration_rates.favor") * 20));
		MessageUtil.sendDebug("Favor regeneration runnable enabled...");

		// Start saving runnable TODO Should we move this?
		scheduler.scheduleAsyncRepeatingTask(StoaPlugin.getInst(), SAVE, 20, (Configs.getSettingInt("saving.freq") * 20));

		// Enable Deity runnables
		for(Deity deity : Stoa.getMythos().getDeities())
			for(Ability ability : deity.getAbilities())
				if(ability.getRunnable() != null) scheduler.scheduleSyncRepeatingTask(StoaPlugin.getInst(), ability.getRunnable(), ability.getDelay(), ability.getRepeat());

		// Triggers
		Threads.registerSyncAsyncRunnables(StoaPlugin.getInst(), Stoa.getMythos().getSyncAsyncTasks());
	}

	public static void stopThreads()
	{
		StoaPlugin.getInst().getServer().getScheduler().cancelTasks(StoaPlugin.getInst());
		Threads.stopHooker(StoaPlugin.getInst());
	}
}
