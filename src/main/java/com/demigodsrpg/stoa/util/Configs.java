package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaPlugin;
import org.bukkit.configuration.Configuration;

import java.util.List;

/**
 * Module to load configuration settings from any passed in plugin's config.yml.
 */
public class Configs
{
	/**
	 * Constructor to create a new Configs for the given plugin's <code>instance</code>.
	 *
	 * @param instance The plugin instance the Configs attaches to.
	 * @param copyDefaults Boolean for copying the default config.yml found inside this demigods over the config file utilized by this library.
	 */
	static
	{
		Configuration config = StoaPlugin.getInst().getConfig();
		config.options().copyDefaults(true);
		StoaPlugin.getInst().saveConfig();
	}

	/**
	 * Retrieve the Integer setting for String <code>id</code>.
	 *
	 * @param id The String key for the setting.
	 * @return Integer setting.
	 */
	public static int getSettingInt(String id)
	{
		if(StoaPlugin.getInst().getConfig().isInt(id)) return StoaPlugin.getInst().getConfig().getInt(id);
		else return -1;
	}

	/**
	 * Retrieve the String setting for String <code>id</code>.
	 *
	 * @param id The String key for the setting.
	 * @return String setting.
	 */
	public static String getSettingString(String id)
	{
		if(StoaPlugin.getInst().getConfig().isString(id)) return StoaPlugin.getInst().getConfig().getString(id);
		else return null;
	}

	/**
	 * Retrieve the Boolean setting for String <code>id</code>.
	 *
	 * @param id The String key for the setting.
	 * @return Boolean setting.
	 */
	public static boolean getSettingBoolean(String id)
	{
		return !StoaPlugin.getInst().getConfig().isBoolean(id) || StoaPlugin.getInst().getConfig().getBoolean(id);
	}

	/**
	 * Retrieve the Float setting for String <code>id</code>.
	 *
	 * @param id The String key for the setting.
	 * @return Float setting.
	 */
	public static float getSettingFloat(String id)
	{
		String floatValue = "-1F";
		if(StoaPlugin.getInst().getConfig().isString(id)) floatValue = StoaPlugin.getInst().getConfig().getString(id);
		try
		{
			return Float.valueOf(floatValue);
		}
		catch(Exception ignored)
		{
		}
		return -1F;
	}

	/**
	 * Retrieve the Double setting for String <code>id</code>.
	 *
	 * @param id The String key for the setting.
	 * @return Double setting.
	 */
	public static double getSettingDouble(String id)
	{
		if(StoaPlugin.getInst().getConfig().isDouble(id)) return StoaPlugin.getInst().getConfig().getDouble(id);
		else return -1;
	}

	/**
	 * Retrieve the List<String> setting for String <code>id</code>.
	 *
	 * @param id The String key for the setting.
	 * @return List<String> setting.
	 */
	public static List<String> getSettingList(String id)
	{
		if(StoaPlugin.getInst().getConfig().isList(id)) return StoaPlugin.getInst().getConfig().getStringList(id);
		return null;
	}

	public static void addToSettingList(String id, String data)
	{
		List<String> list = getSettingList(id);
		list.add(data);
		StoaPlugin.getInst().getConfig().set(id, list);
		StoaPlugin.getInst().saveConfig();
	}

	public static void removeFromSettingList(String id, String data)
	{
		List<String> list = getSettingList(id);
		list.remove(data);
		StoaPlugin.getInst().getConfig().set(id, list);
		StoaPlugin.getInst().saveConfig();
	}
}
