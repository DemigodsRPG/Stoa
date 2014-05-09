package com.demigodsrpg.stoa.data;

import com.censoredsoftware.library.data.DataSerializable;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unchecked")
public abstract class DataAccess<K, V extends DataAccess<K, V>> implements DataSerializable
{
	@SuppressWarnings("RedundantCast")
	private final Class<V> clazz = (Class<V>) ((V) this).getClass();

	/*
	 * Access to Data Object Classes from Data Manager.
	 */

	public abstract K getId();

	/*
	 * Direct access to Data Manager from Data Object Classes.
	 */

	public V getDirect(K key)
	{
		return (V) DataManager.DATA_MANAGER.getFor(clazz, key);
	}

	public Collection<V> allDirect()
	{
		return DataManager.DATA_MANAGER.getAllOf(clazz);
	}

	public Collection<V> allDirectWith(Predicate<V> predicate)
	{
		return Collections2.filter(allDirect(), predicate);
	}

	public ConcurrentMap<K, V> mapDirect()
	{
		return DataManager.DATA_MANAGER.getMapFor(clazz);
	}

	public void putDirect(K key, V value)
	{
		DataManager.DATA_MANAGER.putFor(clazz, key, value);
	}

	public void removeDirect(K key)
	{
		DataManager.DATA_MANAGER.removeFor(clazz, key);
	}

	/*
	 * Convenience methods for Data Object Classes.
	 */

	public void save()
	{
		putDirect(getId(), (V) this);
	}

	public void remove()
	{
		removeDirect(getId());
	}
}
