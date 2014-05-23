package com.demigodsrpg.stoa.model;

import com.censoredsoftware.shaded.com.iciql.Iciql;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.util.Configs;
import org.bukkit.GameMode;

import java.util.UUID;

@Iciql.IQTable(name = "characters")
public class CharacterModel implements Model
{
	// -- DEFAULT CONSTRUCTOR -- //
	public CharacterModel()
	{
	}

	// -- PRACTICAL STATIC CONSTRUCTOR -- //
	public static CharacterModel from(final PlayerModel player, final String charName, final Deity deity)
	{
		CharacterModel model = new CharacterModel();

		// Set name
		model.name = charName;

		// Defaults
		model.deity = deity.getName();
		model.alive = true;
		model.active = false;
		model.usable = true;
		model.hunger = 20;
		model.level = 0;
		model.killCount = 0;
		model.health = deity.getMaxHealth();
		model.experience = 0.0F;
		model.gameMode = GameMode.SURVIVAL;

		// Additional Defaults
		model.favor = deity.getMaxFavor();
		model.maxFavor = deity.getMaxFavor();
		model.skillPoints = Configs.getSettingInt("skillPoints");

		// Foreign Keys
		model.playerId = player.mojangAccount;

		// Don't set inventories or locations yet
		return model;
	}

	// -- MODEL META -- //
	@Iciql.IQColumn(name = "id", primaryKey = true)
	public String uuid = UUID.randomUUID().toString();

	// -- DATA -- //
	@Iciql.IQColumn
	public String name;
	@Iciql.IQColumn
	public String deity;
	@Iciql.IQColumn
	public Boolean alive;
	@Iciql.IQColumn
	public Boolean active;
	@Iciql.IQColumn
	public Boolean usable;
	@Iciql.IQColumn
	public Integer hunger;
	@Iciql.IQColumn
	public Integer level;
	@Iciql.IQColumn
	public Integer killCount;
	@Iciql.IQColumn
	public Double health;
	@Iciql.IQColumn
	public Float experience;
	@Iciql.IQEnum
	@Iciql.IQColumn
	public GameMode gameMode;

	// -- ADDITIONAL DATA -- //
	@Iciql.IQColumn
	public Integer favor;
	@Iciql.IQColumn
	public Integer maxFavor;
	@Iciql.IQColumn
	public Integer skillPoints;

	// private Set<UUID> notifications;
	// private Map<String, UUID> binds;
	// private Map<String, UUID> skillData;
	// private Map<String, UUID> warps;
	// private Map<String, UUID> invites;

	// -- FOREIGN DATA -- //
	@Iciql.IQColumn(nullable = false)
	public String playerId;
	@Iciql.IQColumn
	public String inventoryId;
	@Iciql.IQColumn
	public String enderInventory;
	@Iciql.IQColumn
	public String locationId;
	@Iciql.IQColumn
	public String bedSpawnId;

	// -- INTERFACE METHODS -- //
	@Override
	public String id()
	{
		return uuid;
	}

	@Override
	public String name()
	{
		return "CHARACTER";
	}
}
