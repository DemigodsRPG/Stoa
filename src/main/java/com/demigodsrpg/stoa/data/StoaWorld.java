package com.demigodsrpg.stoa.data;

import com.censoredsoftware.library.data.yaml.SimpleYamlFile;
import com.demigodsrpg.stoa.data.file.DemigodsWorldFile;
import com.demigodsrpg.stoa.location.StoaLocation;
import com.demigodsrpg.stoa.structure.StoaStructure;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class StoaWorld extends SimpleYamlFile<StoaWorld>
{
	private final String worldName, worldDataFolder;
	private final DemigodsWorldFile<UUID, StoaLocation> location;
	private final DemigodsWorldFile<UUID, StoaStructure> structure;

	StoaWorld(final String worldName, String worldDataFolder)
	{
		this.worldName = worldName;
		this.worldDataFolder = worldDataFolder;

		location = new DemigodsWorldFile<UUID, StoaLocation>("l", ".stoa", this.worldDataFolder)
		{
			@Override
			public StoaLocation valueFromData(UUID uuid, ConfigurationSection conf)
			{
				return new StoaLocation(uuid, conf, worldName);
			}

			@Override
			public UUID keyFromString(String stringId)
			{
				return UUID.fromString(stringId);
			}
		};

		structure = new DemigodsWorldFile<UUID, StoaStructure>("s", ".stoa", this.worldDataFolder)
		{
			@Override
			public StoaStructure valueFromData(UUID uuid, ConfigurationSection conf)
			{
				return new StoaStructure(uuid, conf, worldName);
			}

			@Override
			public UUID keyFromString(String stringId)
			{
				return UUID.fromString(stringId);
			}
		};
	}

	public World getBukkitWorld()
	{
		return Bukkit.getWorld(worldName);
	}

	public String getName()
	{
		return worldName;
	}

	public StoaWorld valueFromData(ConfigurationSection conf)
	{
		// TODO
		return this;
	}

	@Override
	public String getDirectoryPath()
	{
		return worldDataFolder;
	}

	@Override
	public String getFullFileName()
	{
		return "b.stoa";
	}

	@Override
	public void loadDataFromFile() // TODO Put this in the parent class.
	{
		getCurrentFileData();
	}

	@Override
	public String getId()
	{
		return worldName;
	}

	public Map<String, Object> serialize()
	{
		// TODO
		return Maps.newHashMap();
	}

	// -- UTILITY METHODS -- //

	public static StoaWorld of(World world)
	{
		return WorldDataManager.getWorld(world.getName());
	}

	public static StoaWorld of(Location location)
	{
		return of(location.getWorld());
	}

	// -- DATA TYPES -- //

	public void loadData()
	{
		loadDataFromFile();
		location.loadDataFromFile();
		structure.loadDataFromFile();
	}

	public boolean saveData()
	{
		location.saveDataToFile();
		structure.saveDataToFile();
		return saveDataToFile();
	}

	DemigodsWorldFile<UUID, StoaLocation> locations()
	{
		return location;
	}

	DemigodsWorldFile<UUID, StoaStructure> structures()
	{
		return structure;
	}
}
