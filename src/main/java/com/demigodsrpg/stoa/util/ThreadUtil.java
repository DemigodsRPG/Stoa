/*
 * Copyright 2014 Alex Bennett & Alexander Chauncey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.data.thread.SyncAsyncRunnable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Create and manage Threads easily with this helpful Util.
 */
@SuppressWarnings("unchecked")
public class ThreadUtil {
    // Private Constants
    private static ConcurrentMap<UUID, StoaRunnable> TASKS = Maps.newConcurrentMap();
    private static ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5);
    private static ConcurrentMap<String, Hooker> HOOKERS = Maps.newConcurrentMap();

    // - TIMED TASKS - //

    /**
     * Start a new timed repeating task for the CensoredRunnable.
     *
     * @param runnable     Special Runnable for use with this class.
     * @param delay        Delay before starting the task.
     * @param milliseconds Time between each run.
     * @return The CensoredRunnable's assigned UUID.
     */
    public static UUID newTimedTask(StoaRunnable runnable, long delay, long milliseconds) {
        UUID id = runnable.getId();
        runnable.setFuture((ScheduledFuture<StoaRunnable>) EXECUTOR.scheduleAtFixedRate(runnable, delay, milliseconds, TimeUnit.MILLISECONDS));
        TASKS.put(id, runnable);
        return id;
    }

    /**
     * Start a new timed delayed task for the CensoredRunnable.
     *
     * @param runnable Special Runnable for use with this class.
     * @param delay    Delay before starting the task.
     * @return The CensoredRunnable's assigned UUID.
     */
    public static UUID newTimedTask(StoaRunnable runnable, long delay) {
        UUID id = runnable.getId();
        runnable.setFuture((ScheduledFuture<StoaRunnable>) EXECUTOR.schedule(runnable, delay, TimeUnit.MILLISECONDS));
        TASKS.put(id, runnable);
        return id;
    }

    /**
     * Get the CensoredRunnable back.
     *
     * @param id The CensoredRunnable's id.
     * @return The CensoredRunnable.
     */
    public static StoaRunnable getTimedTask(UUID id) {
        if (TASKS.containsKey(id)) return TASKS.get(id);
        return null;
    }

    // - HOOKERS - //

    /**
     * Get the Hooker for a plugin.
     *
     * @param plugin The plugin.
     * @return The Hooker.
     */
    public static Hooker getHooker(Plugin plugin) {
        String pluginName = plugin.getName();
        if (!HOOKERS.containsKey(pluginName)) HOOKERS.putIfAbsent(pluginName, new Hooker(plugin));
        return HOOKERS.get(pluginName);
    }

    /**
     * Register a trigger with the plugin's Hooker.
     *
     * @param plugin  The plugin.
     * @param trigger The trigger.
     */
    public static void registerSyncAsyncRunnable(Plugin plugin, SyncAsyncRunnable trigger) {
        getHooker(plugin).addSyncAsyncRunnable(trigger);
    }

    /**
     * Register a collection of triggers with the plugin's Hooker.
     *
     * @param plugin   The plugin.
     * @param triggers The triggers.
     */
    public static void registerSyncAsyncRunnables(Plugin plugin, Collection<SyncAsyncRunnable> triggers) {
        for (SyncAsyncRunnable trigger : triggers)
            getHooker(plugin).addSyncAsyncRunnable(trigger);
    }

    /**
     * Unregister a trigger with the plugin's Hooker.
     *
     * @param plugin  The plugin.
     * @param trigger The trigger.
     */
    public static void unregisterSyncAsyncRunnable(Plugin plugin, SyncAsyncRunnable trigger) {
        getHooker(plugin).removeSyncAsyncRunnable(trigger);
    }

    /**
     * Unregister a collection of triggers with the plugin's Hooker.
     *
     * @param plugin   The plugin.
     * @param triggers The triggers.
     */
    public static void unregisterSyncAsyncRunnables(Plugin plugin, Collection<SyncAsyncRunnable> triggers) {
        for (SyncAsyncRunnable trigger : triggers)
            getHooker(plugin).removeSyncAsyncRunnable(trigger);
    }

    /**
     * Stop the Hooker for a plugin.
     *
     * @param plugin The plugin.
     */
    public static void stopHooker(Plugin plugin) {
        getHooker(plugin).stop();
    }

    // - MISC - //

    /**
     * They're hooks for SyncAsyncRunnables... I swear that's all they are!
     */
    static class Hooker extends StoaRunnable {
        private String pluginName;
        private Set<SyncAsyncRunnable> triggers;
        private int syncId = -1;

        /**
         * Add a trigger.
         *
         * @param trigger The trigger.
         */
        public void addSyncAsyncRunnable(SyncAsyncRunnable trigger) {
            triggers.add(trigger);
            HOOKERS.put(pluginName, this);
        }

        /**
         * Remove a trigger.
         *
         * @param trigger The trigger.
         */
        public void removeSyncAsyncRunnable(SyncAsyncRunnable trigger) {
            triggers.remove(trigger);
            HOOKERS.put(pluginName, this);
        }

        Hooker(Plugin plugin) {
            pluginName = plugin.getName();
            triggers = Sets.newHashSet();
            newTimedTask(this, 100, 50);
            syncId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    for (SyncAsyncRunnable trigger : triggers)
                        trigger.runSync();
                }
            }, 2, 1);
        }

        /**
         * Stop the Hooker.
         */
        public void stop() {
            cancel();
            Bukkit.getScheduler().cancelTask(syncId);
            HOOKERS.remove(pluginName);
        }

        @Override
        public void runIt() {
            for (SyncAsyncRunnable trigger : triggers)
                trigger.runAsync();
        }
    }

    /**
     * According to the JavaDocs for Thread, nobody should ever use the stop() method.
     * It is instead recommended that everybody simply return from the run method.
     * To deal with this, the CensoredRunnable class was created.
     * <p/>
     * Create a new Thread with the newThread() method, passing in one of these, and you're done.
     */
    public static abstract class StoaRunnable implements Runnable {
        private final UUID id;
        private volatile ScheduledFuture<StoaRunnable> future;
        private boolean stop;

        /**
         * Special Runnable for use with com.censoredsoftware.censoredlib.util.Threads.
         */
        public StoaRunnable() {
            id = UUID.randomUUID();
            stop = false;
        }

        @Override
        public final void run() {
            if (stop) return;
            runIt();
        }

        /**
         * Replacement for run().
         */
        public abstract void runIt();

        public final UUID getId() {
            return id;
        }

        final void setFuture(ScheduledFuture<StoaRunnable> future) {
            this.future = future;
        }

        /**
         * "Stops" the Thread related to this Runnable.
         *
         * @return Success.
         */
        public final boolean cancel() {
            if (TASKS.containsKey(id)) TASKS.remove(id);
            future.cancel(false);
            return stop = true;
        }
    }
}
