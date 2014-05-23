package com.demigodsrpg.stoa.entity.player;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.demigodsrpg.stoa.data.DataAccess;
import com.demigodsrpg.stoa.util.ChatRecorder;
import com.demigodsrpg.stoa.util.Configs;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StoaPlayer extends DataAccess<UUID, StoaPlayer>
{
	private UUID mojangAccount;
	private String playerName;
	private String mortalName, mortalListName;
	private boolean canPvp;
	private Long lastLoginTime, lastLogoutTime;
	private String currentDeityName;
	private int characterSlots;
	private UUID current;
	private UUID previous;
	private UUID mortalInventory, mortalEnderInventory;
	private ChatRecorder chatRecording;

	public StoaPlayer()
	{
		characterSlots = Configs.getSettingInt("character.default_character_slots");
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static StoaPlayer of(UUID mojangAccount, ConfigurationSection conf)
	{
		StoaPlayer player = new StoaPlayer();
		player.mojangAccount = mojangAccount;
		player.playerName = conf.getString("playerName");
		if(conf.isString("mortalName")) player.mortalName = conf.getString("mortalName");
		if(conf.isString("mortalListName")) player.mortalListName = conf.getString("mortalListName");
		if(conf.isBoolean("canPvp")) player.canPvp = conf.getBoolean("canPvp");
		if(conf.isLong("lastLoginTime")) player.lastLoginTime = conf.getLong("lastLoginTime");
		if(conf.isLong("lastLogoutTime")) player.lastLogoutTime = conf.getLong("lastLogoutTime");
		if(conf.getString("currentDeityName") != null) player.currentDeityName = conf.getString("currentDeityName");
		if(conf.isInt("characterSlots")) player.characterSlots = conf.getInt("characterSlots");
		if(conf.getString("current") != null) player.current = UUID.fromString(conf.getString("current"));
		if(conf.getString("previous") != null) player.previous = UUID.fromString(conf.getString("previous"));
		if(conf.getString("mortalInventory") != null) player.mortalInventory = UUID.fromString(conf.getString("mortalInventory"));
		if(conf.getString("mortalEnderInventory") != null) player.mortalEnderInventory = UUID.fromString(conf.getString("mortalEnderInventory"));

		return player;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("playerName", playerName);
		map.put("characterSlots", characterSlots);
		try
		{
			map.put("canPvp", canPvp);
			map.put("lastLoginTime", lastLoginTime);
			map.put("lastLogoutTime", lastLogoutTime);
		}
		catch(Exception ignored)
		{
		}
		if(mortalName != null) map.put("mortalName", mortalName);
		if(mortalListName != null) map.put("mortalListName", mortalListName);
		if(currentDeityName != null) map.put("currentDeityName", currentDeityName);
		if(current != null) map.put("current", current.toString());
		if(previous != null) map.put("previous", previous.toString());
		if(mortalInventory != null) map.put("mortalInventory", mortalInventory.toString());
		if(mortalEnderInventory != null) map.put("mortalEnderInventory", mortalEnderInventory.toString());
		return map;
	}

}
