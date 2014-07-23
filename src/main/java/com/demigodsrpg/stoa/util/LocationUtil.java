package com.demigodsrpg.stoa.util;

import com.censoredsoftware.library.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocationUtil {
    private LocationUtil() {
    }

    public static String stringFromLocation(Location location) {
        return location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();
    }

    public static Location locationFromString(String location) {
        String[] part = location.split(";");
        if (Bukkit.getWorld(part[0]) != null) {
            return new Location(Bukkit.getWorld(part[0]), Double.parseDouble(part[1]), Double.parseDouble(part[2]), Double.parseDouble(part[3]), Float.parseFloat(part[4]), Float.parseFloat(part[4]));
        }
        return null;
    }

    /**
     * Randoms a random location with the center being <code>reference</code>.
     * Must be at least <code>min</code> blocks from the center and no more than <code>max</code> blocks away.
     *
     * @param reference the location used as the center for reference.
     * @param min       the minimum number of blocks away.
     * @param max       the maximum number of blocks away.
     * @return the random location generated.
     */
    public static Location randomLocation(Location reference, int min, int max) {
        Location location = reference.clone();
        double randX = RandomUtil.generateIntRange(min, max);
        double randZ = RandomUtil.generateIntRange(min, max);
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
    public static Location randomChunkLocation(Chunk chunk) {
        Location reference = chunk.getBlock(RandomUtil.generateIntRange(1, 16), 64, RandomUtil.generateIntRange(1, 16)).getLocation();
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
    public static Set<Block> getBlocks(Location location, int radius) {
        // Define variables
        Set<Block> blocks = new HashSet<>();
        blocks.add(location.getBlock());

        for (int x = 0; x <= radius; x++)
            blocks.add(location.add(x, 0, x).getBlock());

        return blocks;
    }

    public static Location getFloorBelowLocation(Location location) {
        if (location.getBlock().getType().isSolid()) return location;
        return getFloorBelowLocation(location.getBlock().getRelative(BlockFace.DOWN).getLocation());
    }

    public static List<Location> getCirclePoints(Location center, final double radius, final int points) {
        final World world = center.getWorld();
        final double X = center.getX();
        final double Y = center.getY();
        final double Z = center.getZ();
        List<Location> list = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double x = X + radius * Math.cos((2 * Math.PI * i) / points);
            double z = Z + radius * Math.sin((2 * Math.PI * i) / points);
            list.add(new Location(world, x, Y, z));
        }
        return list;
    }

    public static float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }

    public static double distanceFlat(Location location1, Location location2) {
        if (!location1.getWorld().equals(location2.getWorld())) return Integer.MAX_VALUE;
        Location location3 = location2.clone();
        location3.setY(location1.getY());
        return location1.distance(location3);
    }
}
