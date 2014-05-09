package com.demigodsrpg.stoa.location;

import com.censoredsoftware.library.util.Randoms;
import com.demigodsrpg.stoa.data.StoaWorld;
import com.demigodsrpg.stoa.data.WorldDataAccess;
import com.demigodsrpg.stoa.data.WorldDataManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;

import java.util.*;

public class StoaLocation extends WorldDataAccess<UUID, StoaLocation>
{
	private UUID id;
	private String world;
	private Double X;
	private Double Y;
	private Double Z;
	private Float pitch;
	private Float yaw;
	private String region;

	StoaLocation()
	{
	}

	public StoaLocation(UUID id, ConfigurationSection conf, String... args)
	{
		this.id = id;
		this.world = args[0];
		X = conf.getDouble("X");
		Y = conf.getDouble("Y");
		Z = conf.getDouble("Z");
		pitch = Float.parseFloat(conf.getString("pitch"));
		yaw = Float.parseFloat(conf.getString("yaw"));
		region = conf.getString("region");
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = Maps.newHashMap();
		map.put("X", X);
		map.put("Y", Y);
		map.put("Z", Z);
		map.put("pitch", String.valueOf(pitch));
		map.put("yaw", String.valueOf(yaw));
		map.put("region", region);
		return map;
	}

	public void generateId()
	{
		if(id == null) id = UUID.randomUUID();
	}

	public void setWorld(String world)
	{
		this.world = world;
	}

	public void setX(Double X)
	{
		this.X = X;
	}

	public void setY(Double Y)
	{
		this.Y = Y;
	}

	public void setZ(Double Z)
	{
		this.Z = Z;
	}

	public void setYaw(Float yaw)
	{
		this.yaw = yaw;
	}

	public void setPitch(Float pitch)
	{
		this.pitch = pitch;
	}

	public void setRegion(StoaRegion region)
	{
		this.region = region.toString();
	}

	public double distance(StoaLocation location)
	{
		if(location != null)
		{
			double squared = NumberConversions.square(X - location.X) + NumberConversions.square(Y - location.Y) + NumberConversions.square(Z = location.Z);
			return Math.sqrt(squared);
		}
		throw new NullPointerException("Cannot measure distance to a null location.");
	}

	public double distance(Location location)
	{
		return getBukkitLocation().distance(location);
	}

	public Location getBukkitLocation() throws NullPointerException
	{
		return new Location(Bukkit.getServer().getWorld(this.world), this.X, this.Y, this.Z, this.yaw, this.pitch);
	}

	public UUID getId()
	{
		return this.id;
	}

	public Double getX()
	{
		return this.X;
	}

	public Double getY()
	{
		return this.Y;
	}

	public Double getZ()
	{
		return this.Z;
	}

	public String getWorldName()
	{
		return this.world;
	}

	public StoaWorld getWorld()
	{
		return WorldDataManager.getWorld(world);
	}

	public StoaRegion getRegion()
	{
		return StoaRegion.at(getBukkitLocation());
	}

	@Override
	public String toString()
	{
		return world + "~and~" + X + "~and~" + Y + "~and~" + Z + "~and~" + pitch + "~and~" + yaw;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(world, X, Y, Z, pitch, yaw);
	}

	@Override
	public boolean equals(Object object)
	{
		return object instanceof StoaLocation && (getId().equals(((StoaLocation) object).getId()) || toString().equals(object.toString()));
	}

	// -- GETTERS/SETTERS -- //

	private static final WorldDataAccess<UUID, StoaLocation> DATA_ACCESS = new StoaLocation();

	public static StoaLocation get(StoaWorld world, UUID id)
	{
		return DATA_ACCESS.getDirect(world, id);
	}

	/**
	 * @deprecated Only use when you have to.
	 */
	public static StoaLocation get(UUID id)
	{
		if(DATA_ACCESS.getMapsReadOnly().containsKey(id)) return DATA_ACCESS.getMapsReadOnly().get(id);
		return null;
	}

	public static Collection<StoaLocation> all(StoaWorld world)
	{
		return DATA_ACCESS.getAll(world);
	}

	// -- UTILITY METHODS -- //

	/**
	 * Randoms a random location with the center being <code>reference</code>.
	 * Must be at least <code>min</code> blocks from the center and no more than <code>max</code> blocks away.
	 *
	 * @param reference the location used as the center for reference.
	 * @param min       the minimum number of blocks away.
	 * @param max       the maximum number of blocks away.
	 * @return the random location generated.
	 */
	public static Location randomLocation(Location reference, int min, int max)
	{
		Location location = reference.clone();
		double randX = Randoms.generateIntRange(min, max);
		double randZ = Randoms.generateIntRange(min, max);
		location.add(randX, 0, randZ);
		double highestY = location.clone().getWorld().getHighestBlockYAt(location);
		location.setY(highestY);
		return location;
	}

	/**
	 * Returns a random location within the <code>chunk</code> passed in.
	 *
	 * @param chunk the chunk that we will obtain the location from.
	 * @return the random location generated.
	 */
	public static Location randomChunkLocation(Chunk chunk)
	{
		Location reference = chunk.getBlock(Randoms.generateIntRange(1, 16), 64, Randoms.generateIntRange(1, 16)).getLocation();
		double locX = reference.getX();
		double locY = chunk.getWorld().getHighestBlockYAt(reference);
		double locZ = reference.getZ();
		return new Location(chunk.getWorld(), locX, locY, locZ);
	}

	/**
	 * Returns a set of blocks in a radius of <code>radius</code> at the provided <code>location</code>.
	 *
	 * @param location the center location to getDesign the blocks from.
	 * @param radius   the radius around the center block from which to getDesign the blocks.
	 * @return Set<Block>
	 */
	public static Set<Block> getBlocks(Location location, int radius)
	{
		// Define variables
		Set<Block> blocks = Sets.newHashSet();
		blocks.add(location.getBlock());

		for(int x = 0; x <= radius; x++)
			blocks.add(location.add(x, 0, x).getBlock());

		return blocks;
	}

	public static Location getFloorBelowLocation(Location location)
	{
		if(location.getBlock().getType().isSolid()) return location;
		return getFloorBelowLocation(location.getBlock().getRelative(BlockFace.DOWN).getLocation());
	}

	public static List<Location> getCirclePoints(Location center, final double radius, final int points)
	{
		final World world = center.getWorld();
		final double X = center.getX();
		final double Y = center.getY();
		final double Z = center.getZ();
		List<Location> list = new ArrayList<>();
		for(int i = 0; i < points; i++)
		{
			double x = X + radius * Math.cos((2 * Math.PI * i) / points);
			double z = Z + radius * Math.sin((2 * Math.PI * i) / points);
			list.add(new Location(world, x, Y, z));
		}
		return list;
	}

	public static float toDegree(double angle)
	{
		return (float) Math.toDegrees(angle);
	}

	public static double distanceFlat(Location location1, Location location2)
	{
		if(!location1.getWorld().equals(location2.getWorld())) return Integer.MAX_VALUE;
		Location location3 = location2.clone();
		location3.setY(location1.getY());
		return location1.distance(location3);
	}

	public static String locationToString(Location location)
	{
		StoaLocation stoaLocation = new StoaLocation();
		stoaLocation.generateId();
		stoaLocation.setWorld(location.getWorld().getName());
		stoaLocation.setX(location.getX());
		stoaLocation.setY(location.getY());
		stoaLocation.setZ(location.getZ());
		stoaLocation.setPitch(location.getPitch());
		stoaLocation.setYaw(location.getYaw());
		return stoaLocation.toString();
	}

	public static Location fromString(String string)
	{
		try
		{
			String[] args = string.split("~and~");
			if(args.length != 6) return null;
			StoaLocation location = new StoaLocation();
			location.setWorld(args[0]);
			location.setX(Double.valueOf(args[1]));
			location.setY(Double.valueOf(args[2]));
			location.setZ(Double.valueOf(args[3]));
			location.setPitch(Float.valueOf(args[4]));
			location.setYaw(Float.valueOf(args[5]));
			return location.getBukkitLocation();
		}
		catch(Exception ignored)
		{
		}
		return null;
	}

	public static StoaLocation of(String world, double X, double Y, double Z, float yaw, float pitch)
	{
		final StoaLocation created = new StoaLocation();
		created.setWorld(world);
		created.setX(X);
		created.setY(Y);
		created.setZ(Z);
		created.setYaw(yaw);
		created.setPitch(pitch);
		created.setRegion(StoaRegion.at((int) X, (int) Z, world));
		return Iterables.find(all(WorldDataManager.getWorld(world)), new Predicate<StoaLocation>()
		{
			@Override
			public boolean apply(StoaLocation stoaLocation)
			{
				return stoaLocation.equals(created);
			}
		}, created);
	}

	public static StoaLocation of(Location location)
	{
		return of(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	public static StoaLocation track(StoaLocation location)
	{
		location.generateId();
		location.save();
		return location;
	}

	public static StoaLocation track(Location bukkitLocation)
	{
		return track(of(bukkitLocation));
	}
}
