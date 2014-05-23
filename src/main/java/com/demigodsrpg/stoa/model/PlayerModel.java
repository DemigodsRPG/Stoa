package com.demigodsrpg.stoa.model;

import com.censoredsoftware.library.mcidprovider.McIdProvider;
import com.censoredsoftware.shaded.com.iciql.Iciql;
import com.demigodsrpg.stoa.util.ChatRecorder;
import com.demigodsrpg.stoa.util.Configs;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;

@Iciql.IQTable(name = "players")
public class PlayerModel implements Model
{
	// -- DEFAULT CONSTRUCTOR -- //
	public PlayerModel()
	{
	}

	// -- PRACTICAL STATIC CONSTRUCTOR -- //
	public static PlayerModel from(Player player)
	{
		PlayerModel model = new PlayerModel();

		// Set ID
		model.mojangAccount = McIdProvider.getId(player.getName()).toString();

		// Defaults
		model.playerName = player.getName();
		model.mortalName = player.getDisplayName();
		model.mortalListName = player.getPlayerListName();
		model.canPvp = false;
		model.characterSlots = Configs.getSettingInt("character.default_character_slots");
		model.lastLoginTime = Timestamp.from(Instant.now());

		// Don't set character, inventory, or last logout time data yet
		return model;
	}

	// -- MODEL META -- //
	@Iciql.IQColumn(name = "id", primaryKey = true)
	public String mojangAccount;

	// -- DATA -- //
	@Iciql.IQColumn
	public String playerName;
	@Iciql.IQColumn
	public String mortalName;
	@Iciql.IQColumn
	public String mortalListName;
	@Iciql.IQColumn
	public Boolean canPvp;
	@Iciql.IQColumn
	public Integer characterSlots;
	@Iciql.IQColumn
	public Timestamp lastLoginTime;
	@Iciql.IQColumn
	public Timestamp lastLogoutTime;

	// -- FOREIGN DATA -- //
	@Iciql.IQColumn
	public String currentCharacterId;
	@Iciql.IQColumn
	public String mortalInventoryId;
	@Iciql.IQColumn
	public String mortalEnderChestId;

	// -- TRANSIENT -- //
	public transient ChatRecorder chatRecorder;

	// -- INTERFACE METHODS -- //
	@Override
	public String id()
	{
		return mojangAccount;
	}

	@Override
	public String name()
	{
		return "PLAYER";
	}
}
