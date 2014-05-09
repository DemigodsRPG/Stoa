package com.demigodsrpg.stoa.data;

import com.censoredsoftware.library.data.DataSerializable;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unchecked")
public abstract class WorldDataAccess<K, V extends WorldDataAccess<K, V>> implements DataSerializable<K>
{
	@SuppressWarnings("RedundantCast")
	private final Class<V> clazz = (Class<V>) ((V) this).getClass();

	public V getDirect(StoaWorld world, K key)
	{
		return DataManager.WORLD_DATA_MANAGER.getFor(clazz, world, key);
	}

	public Collection<V> getAll(StoaWorld world)
	{
		return DataManager.WORLD_DATA_MANAGER.getAllOf(clazz, world);
	}

	public Set<V> getAll()
	{
		Set<V> valueSet = Sets.newHashSet();
		for(StoaWorld world : WorldDataManager.getWorlds())
			valueSet.addAll(DataManager.WORLD_DATA_MANAGER.getAllOf(clazz, world));
		return valueSet;
	}

	public Collection<V> getAllWith(StoaWorld world, Predicate<V> predicate)
	{
		return Collections2.filter(getAll(world), predicate);
	}

	public Set<V> getAllWith(Predicate<V> predicate)
	{
		return Sets.filter(getAll(), predicate);
	}

	public ConcurrentMap<K, V> getMap(StoaWorld world)
	{
		return DataManager.WORLD_DATA_MANAGER.getMapFor(clazz, world);
	}

	/**
	 * @deprecated Only use this is you have to. Returns an immutable view of all of the maps.
	 */
	public ImmutableMap<K, V> getMapsReadOnly()
	{
		ImmutableMap.Builder builder = ImmutableMap.builder();
		for(StoaWorld world : WorldDataManager.getWorlds())
			builder.putAll(DataManager.WORLD_DATA_MANAGER.getMapFor(clazz, world));
		return builder.build();
	}

	public void put(StoaWorld world, K key, V value)
	{
		DataManager.WORLD_DATA_MANAGER.putFor(clazz, world, key, value);
	}

	public void save()
	{
		put(getWorld(), getId(), (V) this);
	}

	public void remove(StoaWorld world, K key)
	{
		DataManager.WORLD_DATA_MANAGER.removeFor(clazz, world, key);
	}

	public void remove()
	{
		remove(getWorld(), getId());
	}

	protected abstract StoaWorld getWorld();
}
