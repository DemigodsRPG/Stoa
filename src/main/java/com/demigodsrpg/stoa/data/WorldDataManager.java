package com.demigodsrpg.stoa.data;

import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.data.file.FileDataManager;
import com.demigodsrpg.stoa.location.StoaLocation;
import com.demigodsrpg.stoa.structure.StoaStructure;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("ALL")
public class WorldDataManager implements Listener
{
	static final ConcurrentMap<String, StoaWorld> WORLDS = Maps.newConcurrentMap();

	WorldDataManager()
	{
		Bukkit.getPluginManager().registerEvents(this, StoaPlugin.getInst());
	}

	// -- WORLD DATA -- //

	void addWorld(World world)
	{
		StoaWorld stoaWorld = new StoaWorld(world.getName(), FileDataManager.SAVE_PATH + "worlds/" + world.getName() + "/");
		stoaWorld.loadData();
		WORLDS.put(world.getName(), stoaWorld);
	}

	void removeWorld(String name)
	{
		WORLDS.remove(name);
	}

	public static StoaWorld getWorld(String name)
	{
		if(name == null || !WORLDS.containsKey(name)) return null;
		return WORLDS.get(name);
	}

	public static Collection<StoaWorld> getWorlds()
	{
		return WORLDS.values();
	}

	// Prevent accidental double init.
	private static boolean didInit = false;

	public void init()
	{
		// Check if init has happened already...
		if(didInit) throw new RuntimeException("Data tried to initialize more than once.");

		// Load worlds
		for(World world : Bukkit.getWorlds())
			addWorld(world);

		// Let the plugin know that this has finished.
		didInit = true;
	}

	protected void save()
	{
		for(StoaWorld world : WORLDS.values())
			world.saveData();
	}

	protected void flushData() throws AccessDeniedException
	{
		WORLDS.clear();
	}

	protected <V extends WorldDataAccess<K, V>, K> V getFor(Class<V> clazz, StoaWorld world, K key)
	{
		if(getMapFor(clazz, world).containsKey(key)) return (V) (WorldDataAccess) getMapFor(clazz, world).get(key);
		return null;
	}

	protected <K, V extends WorldDataAccess<K, V>> Collection<V> getAllOf(Class<V> clazz, StoaWorld world)
	{
		return getMapFor(clazz, world).values();
	}

	protected <K, V extends WorldDataAccess<K, V>> ConcurrentMap<K, V> getMapFor(Class<V> clazz, StoaWorld world)
	{
		if(clazz == StoaLocation.class) return (ConcurrentMap) world.locations().getLoadedData();
		else if(clazz == StoaStructure.class) return (ConcurrentMap) world.structures().getLoadedData();
		return null;
	}

	protected <K, V extends WorldDataAccess<K, V>> void putFor(Class<V> clazz, StoaWorld world, K key, V value)
	{
		getMapFor(clazz, world).put(key, value);
	}

	protected <K, V extends WorldDataAccess<K, V>> void removeFor(Class<V> clazz, StoaWorld world, K key)
	{
		getMapFor(clazz, world).remove(key);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldLoad(WorldLoadEvent event)
	{
		addWorld(event.getWorld());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldUnload(WorldUnloadEvent event)
	{
		removeWorld(event.getWorld().getName());
	}
}
