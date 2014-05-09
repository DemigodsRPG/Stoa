package com.demigodsrpg.stoa.data.file;

import com.censoredsoftware.library.data.DataSerializable;
import com.censoredsoftware.library.data.yaml.DataProvidedYamlFile;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.data.DataManager;
import com.demigodsrpg.stoa.data.DataType;
import com.demigodsrpg.stoa.language.English;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * This is the data management file for Demigods.
 */
@SuppressWarnings("unchecked")
public class FileDataManager extends DataManager
{
	// -- VARIABLES -- //

	// Data Folder
	public static final String SAVE_PATH = StoaPlugin.getInst().getDataFolder() + "/data/"; // Don't change this.

	// -- YAML FILES -- //

	ConcurrentMap<Class, DataProvidedYamlFile> yamlFiles;

	// -- UTIL METHODS -- //

	// Prevent accidental double init.
	private static boolean didInit = false;

	@Override
	protected boolean preInit()
	{
		return true;
	}

	@Override
	public void init()
	{
		// Check if init has happened already...
		if(didInit) throw new RuntimeException("Data tried to initialize more than once.");

		// Create/Load YAML files.
		yamlFiles = Maps.newConcurrentMap();
		for(DataType type : DataType.values())
		{
			DataProvidedYamlFile file = new DataProvidedYamlFile(type, SAVE_PATH, type.getAbbreviation(), ".stoa");
			file.loadDataFromFile();
			yamlFiles.put(type.getDataClass(), file);
		}

		// Let the plugin know that this has finished.
		didInit = true;
	}

	@Override
	public void save()
	{
		for(DataProvidedYamlFile data : yamlFiles.values())
			data.saveDataToFile();
	}

	@Override
	public void flushData()
	{
		// Kick everyone
		for(Player player : Bukkit.getOnlinePlayers())
			player.kickPlayer(ChatColor.GREEN + English.DATA_RESET_KICK.getLine());

		// Clear the data
		for(DataProvidedYamlFile data : yamlFiles.values())
			data.getLoadedData().clear();
		TEMP_DATA.purge();

		save();

		// Reload the PLUGIN
		Bukkit.getServer().getPluginManager().disablePlugin(StoaPlugin.getInst());
		Bukkit.getServer().getPluginManager().enablePlugin(StoaPlugin.getInst());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V extends DataSerializable<K>, I> I getFor(final Class<V> clazz, final K key)
	{
		if(getFile(clazz).getLoadedData().containsKey(key)) return (I) getFile(clazz).getLoadedData().get(key);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V extends DataSerializable<K>, I> Collection<I> getAllOf(final Class<V> clazz)
	{
		return (Collection<I>) (Collection) getFile(clazz).getLoadedData().values();
	}

	@Override
	public <K, V extends DataSerializable<K>, I> ConcurrentMap<K, I> getMapFor(final Class<V> clazz)
	{
		return (ConcurrentMap<K, I>) getFile(clazz).getLoadedData();
	}

	@Override
	public <K, V extends DataSerializable<K>> void putFor(final Class<V> clazz, final K key, final V value)
	{
		getFile(clazz).getLoadedData().put(key, value);
	}

	@Override
	public <K, V extends DataSerializable<K>> void removeFor(final Class<V> clazz, final K key)
	{
		getFile(clazz).getLoadedData().remove(key);
	}

	@SuppressWarnings("unchecked")
	private <K, V extends DataSerializable<K>, I> DataProvidedYamlFile<K, V, I> getFile(Class<V> clazz)
	{
		if(yamlFiles.containsKey(clazz)) return yamlFiles.get(clazz);
		throw new UnsupportedOperationException("Demigods wants a data type that does not exist.");
	}
}
