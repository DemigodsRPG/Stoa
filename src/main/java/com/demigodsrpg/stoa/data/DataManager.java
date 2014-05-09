package com.demigodsrpg.stoa.data;

import com.censoredsoftware.library.data.DataManagerInterface;
import com.censoredsoftware.library.data.DataSerializable;
import com.censoredsoftware.library.data.TempDataManager;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.data.file.FileDataManager;

import java.nio.file.AccessDeniedException;

public abstract class DataManager implements DataManagerInterface
{
	// TODO Should we let people register these as a service, just like Mythos?

	public static final DataManager DATA_MANAGER = findManager();
	public static final TempDataManager TEMP_DATA = new TempDataManager();

	private static DataManager findManager()
	{
		// Get the correct data manager.
		String saveMethod = StoaPlugin.getInst().getConfig().getString("saving.method", "file");
		switch(saveMethod.toLowerCase())
		{
			case "file":
			{
				StoaPlugin.getInst().getLogger().info("Enabling file save method.");
				return trainManager(FileDataManager.class);
			}
		}
		StoaPlugin.getInst().getLogger().severe("\"" + saveMethod + "\" is not a valid save method.");
		StoaPlugin.getInst().getLogger().severe("Defaulting to file save method.");
		return trainManager(FileDataManager.class);
	}

	private static DataManager trainManager(Class<? extends DataManager> manager)
	{
		try
		{
			return manager.newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	static final WorldDataManager WORLD_DATA_MANAGER = new WorldDataManager();

	protected abstract boolean preInit();

	public abstract <K, V extends DataSerializable<K>> void putFor(Class<V> clazz, K key, V value);

	public abstract <K, V extends DataSerializable<K>> void removeFor(Class<V> clazz, K key);

	public static void initAllData()
	{
		WORLD_DATA_MANAGER.init();
		DATA_MANAGER.init();
	}

	public static void saveAllData()
	{
		WORLD_DATA_MANAGER.save();
		DATA_MANAGER.save();
	}

	public static void flushAllData() throws AccessDeniedException
	{
		WORLD_DATA_MANAGER.flushData();
		DATA_MANAGER.flushData();
	}
}
