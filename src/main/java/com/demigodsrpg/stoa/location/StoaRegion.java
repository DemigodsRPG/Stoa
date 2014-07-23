package com.demigodsrpg.stoa.location;

import com.google.common.base.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class StoaRegion {
    public final static int REGION_LENGTH = 8;
    public final static int HALF_REGION_LENGTH = REGION_LENGTH / 2;

    private final int x;
    private final int z;
    private final String world;

    private StoaRegion(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public static StoaRegion at(Location location) {
        return new StoaRegion(getCoordinate(location.getBlockX()), getCoordinate(location.getBlockZ()), location.getWorld().getName());
    }

    public static StoaRegion at(int X, int Z, String world) {
        return new StoaRegion(getCoordinate(X), getCoordinate(Z), world);
    }

    public static int getCoordinate(int number) {
        int temp = number % REGION_LENGTH;
        if (temp >= HALF_REGION_LENGTH) return number + REGION_LENGTH - temp;
        return number - temp;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int lowX() {
        return x - HALF_REGION_LENGTH;
    }

    public int lowZ() {
        return z - HALF_REGION_LENGTH;
    }

    public int highX() {
        return x + HALF_REGION_LENGTH;
    }

    public int highZ() {
        return z + HALF_REGION_LENGTH;
    }

    public String getWorld() {
        return world;
    }

    public Location getCenter() {
        return new Location(Bukkit.getWorld(world), x, 128, z);
    }

    public StoaRegion[] getSurroundingRegions() {
        StoaRegion[] area = new StoaRegion[9];
        area[0] = new StoaRegion(x - REGION_LENGTH, z - REGION_LENGTH, world);
        area[1] = new StoaRegion(x - REGION_LENGTH, z, world);
        area[2] = new StoaRegion(x - REGION_LENGTH, z + REGION_LENGTH, world);
        area[3] = new StoaRegion(x, z - REGION_LENGTH, world);
        area[4] = this;
        area[5] = new StoaRegion(x, z + REGION_LENGTH, world);
        area[6] = new StoaRegion(x + REGION_LENGTH, z - REGION_LENGTH, world);
        area[7] = new StoaRegion(x + REGION_LENGTH, z, world);
        area[8] = new StoaRegion(x + REGION_LENGTH, z + REGION_LENGTH, world);
        return area;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("world", world).add("x", x).add("z", z).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(world, x, z);
    }

    @Override
    public boolean equals(Object object) {
        return Objects.equal(this, object);
    }
}
