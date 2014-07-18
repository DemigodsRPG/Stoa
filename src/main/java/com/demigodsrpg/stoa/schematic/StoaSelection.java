/*
 * Copyright 2014 Alex Bennett & Alexander Chauncey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demigodsrpg.stoa.schematic;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

@SuppressWarnings("deprecation")
public class StoaSelection {
    private int X, Y, Z, XX, YY, ZZ;
    private int eX, eY, eZ, eXX, eYY, eZZ;
    private boolean cuboid;
    private boolean exclude;
    private boolean excludeSelection;
    private List<StoaMaterialData> blockData;

    /**
     * Constructor for a Selection (non-cuboid), useful for getting 1 location back.
     *
     * @param X The relative X coordinate of the schematic from the reference location.
     * @param Y The relative Y coordinate of the schematic from the reference location.
     * @param Z The relative Z coordinate of the schematic from the reference location.
     */
    public StoaSelection(int X, int Y, int Z) {
        this.X = this.XX = X;
        this.Y = this.YY = Y;
        this.Z = this.ZZ = Z;
        this.cuboid = false;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList();
    }

    /**
     * Constructor for a Selection (cuboid), useful for getting only locations back.
     *
     * @param X  The relative X coordinate of the schematic from the reference location.
     * @param Y  The relative Y coordinate of the schematic from the reference location.
     * @param Z  The relative Z coordinate of the schematic from the reference location.
     * @param XX The second relative X coordinate of the schematic from the reference location, creating a cuboid.
     * @param YY The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
     * @param ZZ The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
     */
    public StoaSelection(int X, int Y, int Z, int XX, int YY, int ZZ) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.XX = XX;
        this.YY = YY;
        this.ZZ = ZZ;
        this.cuboid = true;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList();
    }

    /**
     * Constructor for a Selection (non-cuboid).
     *
     * @param X        The relative X coordinate of the schematic from the reference location.
     * @param Y        The relative Y coordinate of the schematic from the reference location.
     * @param Z        The relative Z coordinate of the schematic from the reference location.
     * @param material The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, Material material) {
        this.X = this.XX = X;
        this.Y = this.YY = Y;
        this.Z = this.ZZ = Z;
        this.cuboid = false;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList(new StoaMaterialData(material));
    }

    /**
     * Constructor for a Selection (cuboid).
     *
     * @param X        The relative X coordinate of the schematic from the reference location.
     * @param Y        The relative Y coordinate of the schematic from the reference location.
     * @param Z        The relative Z coordinate of the schematic from the reference location.
     * @param XX       The second relative X coordinate of the schematic from the reference location, creating a cuboid.
     * @param YY       The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
     * @param ZZ       The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
     * @param material The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, int XX, int YY, int ZZ, Material material) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.XX = XX;
        this.YY = YY;
        this.ZZ = ZZ;
        this.cuboid = true;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList(new StoaMaterialData(material));
    }

    /**
     * Constructor for a Selection (non-cuboid).
     *
     * @param X        The relative X coordinate of the schematic from the reference location.
     * @param Y        The relative Y coordinate of the schematic from the reference location.
     * @param Z        The relative Z coordinate of the schematic from the reference location.
     * @param material The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, Material material, byte data) {
        this.X = this.XX = X;
        this.Y = this.YY = Y;
        this.Z = this.ZZ = Z;
        this.cuboid = false;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList(new StoaMaterialData(material, data));
    }

    /**
     * Constructor for a Selection (cuboid).
     *
     * @param X        The relative X coordinate of the schematic from the reference location.
     * @param Y        The relative Y coordinate of the schematic from the reference location.
     * @param Z        The relative Z coordinate of the schematic from the reference location.
     * @param XX       The second relative X coordinate of the schematic from the reference location, creating a cuboid.
     * @param YY       The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
     * @param ZZ       The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
     * @param material The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, int XX, int YY, int ZZ, Material material, byte data) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.XX = XX;
        this.YY = YY;
        this.ZZ = ZZ;
        this.cuboid = true;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList(new StoaMaterialData(material, data));
    }

    /**
     * Constructor for a Selection (non-cuboid).
     *
     * @param X         The relative X coordinate of the schematic from the reference location.
     * @param Y         The relative Y coordinate of the schematic from the reference location.
     * @param Z         The relative Z coordinate of the schematic from the reference location.
     * @param blockData The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, StoaMaterialData blockData) {
        this.X = this.XX = X;
        this.Y = this.YY = Y;
        this.Z = this.ZZ = Z;
        this.cuboid = false;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList(blockData);
    }

    /**
     * Constructor for a Selection (cuboid).
     *
     * @param X         The relative X coordinate of the schematic from the reference location.
     * @param Y         The relative Y coordinate of the schematic from the reference location.
     * @param Z         The relative Z coordinate of the schematic from the reference location.
     * @param XX        The second relative X coordinate of the schematic from the reference location, creating a cuboid.
     * @param YY        The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
     * @param ZZ        The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
     * @param blockData The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, int XX, int YY, int ZZ, StoaMaterialData blockData) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.XX = XX;
        this.YY = YY;
        this.ZZ = ZZ;
        this.cuboid = true;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = Lists.newArrayList(blockData);
    }

    /**
     * Constructor for a Selection (non-cuboid).
     *
     * @param X         The relative X coordinate of the schematic from the reference location.
     * @param Y         The relative Y coordinate of the schematic from the reference location.
     * @param Z         The relative Z coordinate of the schematic from the reference location.
     * @param blockData The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, List<StoaMaterialData> blockData) {
        this.X = this.XX = X;
        this.Y = this.YY = Y;
        this.Z = this.ZZ = Z;
        this.cuboid = false;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = blockData;
    }

    /**
     * Constructor for a Selection (cuboid).
     *
     * @param X         The relative X coordinate of the schematic from the reference location.
     * @param Y         The relative Y coordinate of the schematic from the reference location.
     * @param Z         The relative Z coordinate of the schematic from the reference location.
     * @param XX        The second relative X coordinate of the schematic from the reference location, creating a cuboid.
     * @param YY        The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
     * @param ZZ        The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
     * @param blockData The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, int XX, int YY, int ZZ, List<StoaMaterialData> blockData) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.XX = XX;
        this.YY = YY;
        this.ZZ = ZZ;
        this.cuboid = true;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = blockData;
    }

    /**
     * Constructor for a Selection (non-cuboid).
     *
     * @param X        The relative X coordinate of the schematic from the reference location.
     * @param Y        The relative Y coordinate of the schematic from the reference location.
     * @param Z        The relative Z coordinate of the schematic from the reference location.
     * @param material The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, StoaMaterialData.Preset material) {
        this.X = this.XX = X;
        this.Y = this.YY = Y;
        this.Z = this.ZZ = Z;
        this.cuboid = false;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = material.getData();
    }

    /**
     * Constructor for a Selection (cuboid).
     *
     * @param X        The relative X coordinate of the schematic from the reference location.
     * @param Y        The relative Y coordinate of the schematic from the reference location.
     * @param Z        The relative Z coordinate of the schematic from the reference location.
     * @param XX       The second relative X coordinate of the schematic from the reference location, creating a cuboid.
     * @param YY       The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
     * @param ZZ       The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
     * @param material The StoaMaterialData objects of this schematic.
     */
    public StoaSelection(int X, int Y, int Z, int XX, int YY, int ZZ, StoaMaterialData.Preset material) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.XX = XX;
        this.YY = YY;
        this.ZZ = ZZ;
        this.cuboid = true;
        this.exclude = false;
        this.excludeSelection = false;
        this.blockData = material.getData();
    }

    /**
     * Excluding for a Selection (non-cuboid).
     *
     * @param X The relative X coordinate of the schematic from the reference location.
     * @param Y The relative Y coordinate of the schematic from the reference location.
     * @param Z The relative Z coordinate of the schematic from the reference location.
     * @return This schematic.
     */
    public StoaSelection exclude(int X, int Y, int Z) {
        this.eX = this.eXX = X;
        this.eY = this.eYY = Y;
        this.eZ = this.eZZ = Z;
        this.exclude = true;
        return this;
    }

    /**
     * Excluding for a Selection (cuboid).
     *
     * @param X  The relative X coordinate of the schematic from the reference location.
     * @param Y  The relative Y coordinate of the schematic from the reference location.
     * @param Z  The relative Z coordinate of the schematic from the reference location.
     * @param XX The second relative X coordinate of the schematic from the reference location, creating a cuboid.
     * @param YY The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
     * @param ZZ The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
     * @return This schematic.
     */
    public StoaSelection exclude(int X, int Y, int Z, int XX, int YY, int ZZ) {
        this.eX = X;
        this.eY = Y;
        this.eZ = Z;
        this.eXX = XX;
        this.eYY = YY;
        this.eZZ = ZZ;
        this.exclude = true;
        this.excludeSelection = true;
        return this;
    }

    /**
     * Get the material of the object (a random material is chosen based on the configured odds).
     * <p/>
     * TODO This method needs work, I'm not sure this is the more efficient way to do what we want.
     *
     * @return A material.
     */
    public StoaMaterialData getStructureStoaMaterialData() {
        final int roll = generateIntRange(1, 100);
        Collection<StoaMaterialData> check = Collections2.filter(blockData, new Predicate<StoaMaterialData>() {
            @Override
            public boolean apply(StoaMaterialData blockData) {
                return blockData.getOdds() >= roll;
            }
        });
        if (check.isEmpty()) return getStructureStoaMaterialData();
        return Lists.newArrayList(check).get(generateIntRange(0, check.size() - 1));
    }

    /**
     * Generates an integer with a value between <code>min</code> and <code>max</code>.
     *
     * @param min the minimum value of the integer.
     * @param max the maximum value of the integer.
     * @return Integer
     */
    private static int generateIntRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    /**
     * Get the block locations in this object.
     *
     * @param reference The reference location.
     * @return A set of locations.
     */
    public Set<Location> getBlockLocations(final Location reference) {
        if (cuboid) {
            if (exclude) {
                if (excludeSelection)
                    return Sets.difference(rangeLoop(reference, X, XX, Y, YY, Z, ZZ), rangeLoop(reference, eX, eXX, eY, eYY, eZ, eZZ));
                return Sets.difference(rangeLoop(reference, X, XX, Y, YY, Z, ZZ), Sets.newHashSet(getLocation(reference, eX, eY, eZ)));
            }
            return rangeLoop(reference, X, XX, Y, YY, Z, ZZ);
        }
        return Sets.newHashSet(getLocation(reference, X, Y, Z));
    }

    /**
     * Generate this schematic.
     *
     * @param reference The reference Location.
     */
    public void generate(Location reference) {
        if (blockData.isEmpty()) return;
        for (Location location : getBlockLocations(reference)) {
            StoaMaterialData data = getStructureStoaMaterialData();
            location.getBlock().setTypeIdAndData(data.getMaterial().getId(), data.getData(), data.getPhysics());
        }
    }

    /**
     * Get a relative location, based on the <code>X</code>, <code>Y</code>, <code>Z</code> coordinates relative to the object's central location.
     *
     * @param X Relative X coordinate.
     * @param Y Relative Y coordinate.
     * @param Z Relative Z coordinate.
     * @return New relative location.
     */
    public Location getLocation(Location reference, int X, int Y, int Z) {
        return reference.clone().add(X, Y, Z);
    }

    /**
     * Get a cuboid selection as a HashSet.
     *
     * @param reference The reference location.
     * @param X         The relative X coordinate.
     * @param XX        The second relative X coordinate.
     * @param Y         The relative Y coordinate.
     * @param YY        The second relative Y coordinate.
     * @param Z         The relative Z coordinate.
     * @param ZZ        The second relative Z coordinate.
     * @return The HashSet collection of a cuboid selection.
     */
    public Set<Location> rangeLoop(final Location reference, final int X, final int XX, final int Y, final int YY, final int Z, final int ZZ) {
        Set<Location> set = new HashSet<Location>();
        for (int x : Ranges.closed(X < XX ? X : XX, X < XX ? XX : X).asSet(DiscreteDomains.integers()))
            for (int y : Ranges.closed(Y < YY ? Y : YY, Y < YY ? YY : Y).asSet(DiscreteDomains.integers()))
                for (int z : Ranges.closed(Z < ZZ ? Z : ZZ, Z < ZZ ? ZZ : Z).asSet(DiscreteDomains.integers()))
                    set.add(getLocation(reference, x, y, z));
        return set;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("X", X).add("Y", Y).add("Z", Z).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(X, Y, Z);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof StoaSelection && Objects.equal(this, object);
    }

    public String nonCuboidSingleStoaMaterialDataToString() {
        return X + "~and~" + Y + "~and~" + Z + "~and~" + blockData.get(0).getMaterial().name();
    }

    public static StoaSelection nonCuboidSingleStoaMaterialDataFromString(String string) {
        try {
            String[] args = string.split("~and~");
            if (args.length != 4) return null;
            return new StoaSelection(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]), Material.valueOf(args[3]));
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Collect a cuboid of selections from the point of view of referenceA, centered around referenceB.
     *
     * @param referenceA Point of view for the selections.
     * @param referenceB Center of the cuboid.
     * @param radius     Radius of cuboid.
     * @return List of Selections.
     */
    public static List<StoaSelection> getCuboid(Location referenceA, Location referenceB, int radius) {
        List<StoaSelection> selections = Lists.newArrayList();
        int X = referenceB.getBlockX() - radius, Y = referenceB.getBlockY() - radius, Z = referenceB.getBlockZ() - radius, XX = referenceB.getBlockX() + radius, YY = referenceB.getBlockY() + radius, ZZ = referenceB.getBlockZ() + radius;
        int differenceX = referenceB.getBlockX() - referenceA.getBlockX();
        int differenceY = referenceB.getBlockY() - referenceA.getBlockY();
        int differenceZ = referenceB.getBlockZ() - referenceA.getBlockZ();
        for (int x : Ranges.closed(X < XX ? X : XX, X < XX ? XX : X).asSet(DiscreteDomains.integers()))
            for (int y : Ranges.closed(Y < YY ? Y : YY, Y < YY ? YY : Y).asSet(DiscreteDomains.integers()))
                for (int z : Ranges.closed(Z < ZZ ? Z : ZZ, Z < ZZ ? ZZ : Z).asSet(DiscreteDomains.integers()))
                    selections.add(new StoaSelection(x + differenceX, y + differenceY, z + differenceZ, referenceA.getWorld().getBlockAt(x, y, z).getType()));
        return selections;
    }
}
