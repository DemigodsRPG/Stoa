package com.demigodsrpg.stoa.structure;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.data.StoaWorld;
import com.demigodsrpg.stoa.data.WorldDataAccess;
import com.demigodsrpg.stoa.data.WorldDataManager;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.location.StoaLocation;
import com.demigodsrpg.stoa.location.StoaRegion;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class StoaStructure extends WorldDataAccess<UUID, StoaStructure>
{
	private UUID id;
	private String type, world;
	private UUID referenceLocation, optionalLocation;
	private List<String> flags;
	private String region;
	private String design;
	private Float corruption, sanctity;
	private Boolean active;
	private UUID owner;
	private Map<String, Long> corruptors, sanctifiers;
	private String permission;
	private Integer ascensions;

	public StoaStructure()
	{
	}

	public StoaStructure(UUID id, ConfigurationSection conf, String... args)
	{
		this.id = id;
		this.world = args[0];
		type = conf.getString("type");
		referenceLocation = UUID.fromString(conf.getString("referenceLocation"));
		if(conf.isSet("optionalLocation")) optionalLocation = UUID.fromString(conf.getString("optionalLocation"));
		flags = conf.getStringList("flags");
		region = conf.getString("region");
		design = conf.getString("design");
		if(conf.contains("corruption"))
		{
			try
			{
				corruption = Float.valueOf(conf.getString("corruption"));
			}
			catch(Exception ignored)
			{
			}
		}
		if(conf.contains("sanctity"))
		{
			try
			{
				sanctity = Float.valueOf(conf.getString("sanctity"));
			}
			catch(Exception ignored)
			{
			}
		}
		if(conf.isString("permission")) permission = conf.getString("permission");
		if(conf.isInt("ascensions")) ascensions = conf.getInt("ascensions");
		if(conf.getString("active") != null) active = conf.getBoolean("active");
		if(conf.getString("owner") != null) owner = UUID.fromString(conf.getString("owner"));
		if(conf.isConfigurationSection("corruptors"))
		{
			corruptors = Maps.newHashMap(Maps.transformValues(conf.getConfigurationSection("corruptors").getValues(false), new Function<Object, Long>()
			{
				@Override
				public Long apply(Object o)
				{
					try
					{
						return Long.parseLong(o.toString());
					}
					catch(Exception ignored)
					{
					}
					return null;
				}
			}));
		}
		if(conf.isConfigurationSection("sanctifiers"))
		{
			sanctifiers = Maps.newHashMap(Maps.transformValues(conf.getConfigurationSection("sanctifiers").getValues(false), new Function<Object, Long>()
			{
				@Override
				public Long apply(Object o)
				{
					try
					{
						return Long.parseLong(o.toString());
					}
					catch(Exception ignored)
					{
					}
					return null;
				}
			}));
		}
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("type", type);
		map.put("referenceLocation", referenceLocation.toString());
		if(optionalLocation != null) map.put("optionalLocation", optionalLocation.toString());
		map.put("flags", flags);
		map.put("region", region);
		map.put("design", design);
		if(sanctity != null) map.put("sanctity", sanctity.toString());
		if(active != null) map.put("active", active);
		if(owner != null) map.put("owner", owner.toString());
		if(corruptors != null && !corruptors.isEmpty()) map.put("corruptors", corruptors);
		if(sanctifiers != null && !sanctifiers.isEmpty()) map.put("sanctifiers", sanctifiers);
		if(permission != null) map.put("permission", permission);
		if(ascensions != null) map.put("ascensions", ascensions);
		return map;
	}

	public void generateId()
	{
		id = UUID.randomUUID();
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setSanctity(float sanctity)
	{
		this.sanctity = sanctity;
	}

	public void corrupt(StoaCharacter character, float amount)
	{
		if(getType().corrupt(this, character))
		{
			addCorruptor(character.getId());
			corrupt(amount);
			if(getCorruption() >= getSanctity() && getType().kill(this, character)) kill(character);
		}
	}

	public void corrupt(float amount)
	{
		if(corruption == null || corruption < 0F) corruption = 0F;
		corruption = corruption + amount;
		save();
	}

	public void kill(StoaCharacter character)
	{
		if(getType().kill(this, character)) remove();
	}

	public void setDesign(String name)
	{
		this.design = name;
	}

	public void setLocation(Location reference)
	{
		StoaLocation dLocation = StoaLocation.track(reference);
		this.referenceLocation = dLocation.getId();
		setRegion(dLocation.getRegion());
		this.world = reference.getWorld().getName();
	}

	public void setSecondaryLocation(Location secondary)
	{
		StoaLocation dLocation = StoaLocation.track(secondary);
		this.optionalLocation = dLocation.getId();
	}

	public void setOwner(UUID id)
	{
		this.owner = id;
		addSanctifier(id);
	}

	public void sanctify(StoaCharacter character, float amount)
	{
		if(getType().sanctify(this, character))
		{
			addSanctifier(character.getId());
			sanctify(amount);
		}
	}

	public void sanctify(float amount)
	{
		if(sanctity == null || sanctity < 0F) sanctity = 0F;
		sanctity = sanctity + amount;
		save();
	}

	public void setCorruptors(Map<String, Long> corruptors)
	{
		this.corruptors = corruptors;
	}

	public void addCorruptor(UUID id)
	{
		if(corruptors == null) corruptors = Maps.newHashMap();
		corruptors.put(id.toString(), System.currentTimeMillis());
		save();
	}

	public void removeCorruptor(UUID id)
	{
		corruptors.remove(id.toString());
	}

	public void setSanctifiers(Map<String, Long> sanctifiers)
	{
		this.sanctifiers = sanctifiers;
	}

	public void addSanctifier(UUID id)
	{
		if(sanctifiers == null) sanctifiers = Maps.newHashMap();
		sanctifiers.put(id.toString(), System.currentTimeMillis());
		save();
	}

	public void removeSanctifier(UUID id)
	{
		sanctifiers.remove(id.toString());
	}

	public void setActive(Boolean bool)
	{
		this.active = bool;
	}

	public void setPermission(String permission)
	{
		this.permission = permission;
	}

	public void setRequiredAscensions(int ascensions)
	{
		this.ascensions = ascensions;
	}

	public Location getBukkitLocation()
	{
		return StoaLocation.get(getWorld(), referenceLocation).getBukkitLocation();
	}

	public Location getSecondaryBukkitLocation()
	{
		if(optionalLocation == null) return null;
		return StoaLocation.get(getWorld(), optionalLocation).getBukkitLocation();
	}

	public String getPermission()
	{
		return permission;
	}

	public Integer getRequiredAscensions()
	{
		return ascensions;
	}

	public Set<Location> getClickableBlocks()
	{
		return getType().getDesign(design).getClickableBlocks(getBukkitLocation());
	}

	public Set<Location> getBukkitLocations()
	{
		return getType().getDesign(design).getSchematic(this).getLocations(getBukkitLocation());
	}

	public StoaStructureType getType()
	{
		for(StoaStructureType structureType : Stoa.getMythos().getStructures())
			if(type.equals(structureType.getName())) return structureType;
		return null;
	}

	public Boolean hasOwner()
	{
		return this.owner != null;
	}

	public Float getCorruption()
	{
		if(corruption == null || corruption <= 0F) corruption = 0F;
		return corruption;
	}

	public Float getSanctity()
	{
		if(sanctity == null || sanctity <= 0F) sanctity = getType().getDefSanctity();
		return sanctity;
	}

	public String getOwner()
	{
		return this.owner;
	}

	public Boolean hasMembers()
	{
		return this.sanctifiers != null && !sanctifiers.isEmpty();
	}

	public Collection<UUID> getCorruptors()
	{
		return Collections2.transform(corruptors.keySet(), new Function<String, UUID>()
		{
			@Override
			public UUID apply(String s)
			{
				return UUID.fromString(s);
			}
		});
	}

	public Collection<UUID> getSanctifiers()
	{
		return Collections2.transform(sanctifiers.keySet(), new Function<String, UUID>()
		{
			@Override
			public UUID apply(String s)
			{
				return UUID.fromString(s);
			}
		});
	}

	public String getTypeName()
	{
		return type;
	}

	public Boolean getActive()
	{
		return this.active;
	}

	private void setRegion(StoaRegion region)
	{
		this.region = region.toString();
	}

	public String getRegion()
	{
		return region;
	}

	public void addFlags(Set<StoaStructureType.Flag> flags)
	{
		for(StoaStructureType.Flag flag : flags)
			getRawFlags().add(flag.name());
	}

	public List<String> getRawFlags()
	{
		if(this.flags == null) flags = Lists.newArrayList();
		return this.flags;
	}

	public UUID getId()
	{
		return this.id;
	}

	@Override
	protected StoaWorld getWorld()
	{
		return WorldDataManager.getWorld(world);
	}

	public void generate()
	{
		getType().getDesign(design).getSchematic(this).generate(getBukkitLocation());
	}

	public void remove()
	{
		for(Location location : getBukkitLocations())
			location.getBlock().setType(Material.AIR);
		StoaLocation.get(getWorld(), referenceLocation).remove();
		super.remove();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("id", this.id).toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(Object other)
	{
		return other != null && other instanceof StoaStructure && ((StoaStructure) other).getId() == getId();
	}

	private static final WorldDataAccess<UUID, StoaStructure> DATA_ACCESS = new StoaStructure();

	public static StoaStructure get(StoaWorld world, UUID id)
	{
		return DATA_ACCESS.getDirect(world, id);
	}

	public static Collection<StoaStructure> all(StoaWorld world)
	{
		return DATA_ACCESS.getAll(world);
	}

	public static Collection<StoaStructure> find(StoaWorld world, Predicate<StoaStructure> predicate)
	{
		return DATA_ACCESS.getAllWith(world, predicate);
	}

	public static Collection<StoaStructure> all()
	{
		return DATA_ACCESS.getAll();
	}

	public static Collection<StoaStructure> find(Predicate<StoaStructure> predicate)
	{
		return DATA_ACCESS.getAllWith(predicate);
	}
}
