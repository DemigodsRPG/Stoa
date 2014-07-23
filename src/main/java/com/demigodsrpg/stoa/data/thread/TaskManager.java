package com.demigodsrpg.stoa.data.thread;

import com.censoredsoftware.library.util.ThreadUtil;
import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings("deprecation")
public class TaskManager {
    private static final boolean SAVE_ALERT = StoaPlugin.config().getBoolean("saving.console_alert");
    private static final BukkitRunnable SYNC, ASYNC, FAVOR;

    static {
        SYNC = new BukkitRunnable() {
            @Override
            public void run() {
                // Update online players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ZoneUtil.inNoStoaZone(player.getLocation())) continue;
                    PlayerUtil.fromPlayer(player).updateCanPvp();
                }

                // Update Battles
                BattleUtil.updateBattles();

                // Update Battle Particles
                BattleUtil.updateBattleParticles();
            }
        };
        ASYNC = new BukkitRunnable() {
            @Override
            public void run() {
                // Update Timed Data
                ServerDataUtil.clearExpired();

                // Update Notifications
                // Notification.updateNotifications();
            }
        };
        FAVOR = new BukkitRunnable() {
            @Override
            public void run() {
                // Update Favor
                CharacterUtil.updateFavor();

                // Update Sanctity
                StructureUtil.updateSanctity();
            }
        };
    }

    public static void startThreads() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        // Start sync demigods runnable
        scheduler.scheduleSyncRepeatingTask(StoaPlugin.getInst(), SYNC, 20, 20);
        MessageUtil.sendDebug("Main Demigods SYNC runnable enabled...");

        // Start async demigods runnable
        scheduler.scheduleAsyncRepeatingTask(StoaPlugin.getInst(), ASYNC, 20, 20);
        MessageUtil.sendDebug("Main Demigods ASYNC runnable enabled...");

        // Start favor runnable
        scheduler.scheduleAsyncRepeatingTask(StoaPlugin.getInst(), FAVOR, 20, (StoaPlugin.config().getInt("regeneration_rates.favor") * 20));
        MessageUtil.sendDebug("Favor regeneration runnable enabled...");

        // Enable Deity runnables
        for (Deity deity : Stoa.getMythos().getDeities())
            for (Ability ability : deity.getAbilities())
                if (ability.getRunnable() != null)
                    scheduler.scheduleSyncRepeatingTask(StoaPlugin.getInst(), ability.getRunnable(), ability.getDelay(), ability.getRepeat());

        // Triggers
        ThreadUtil.registerSyncAsyncRunnables(StoaPlugin.getInst(), Stoa.getMythos().getSyncAsyncTasks());
    }

    public static void stopThreads() {
        StoaPlugin.getInst().getServer().getScheduler().cancelTasks(StoaPlugin.getInst());
        ThreadUtil.stopHooker(StoaPlugin.getInst());
    }
}
