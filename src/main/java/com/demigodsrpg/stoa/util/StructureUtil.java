package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.location.StoaRegion;
import com.demigodsrpg.stoa.model.StoaStructureModel;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.iciql.Db;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class StructureUtil {
    public static StoaStructureModel getStructure(final Location location) {
        return getStructureFromBlock(location.getBlock());
    }

    public static StoaStructureModel getStructureFromBlock(final Block block) {
        // Check the meta
        if (block.hasMetadata("stoa.structure")) {
            // Get the center block
            Block center = getCenterBlock(block);

            // Get the owner
            String ownerId = MetaUtil.getMetadata(center, "stoa.structure.owner").asString();

            // Get the type and design names
            String typeName = MetaUtil.getMetadata(center, "stoa.structure.type").asString();
            String designName = MetaUtil.getMetadata(center, "stoa.structure.design").asString();

            // Does the type exist?
            if (typeExists(typeName)) {
                StoaStructureModel.Type type = Stoa.getMythos().getStructure(typeName);

                // Does the design exist?
                if (designExists(type, designName)) {
                    // Get the rest of the data
                    StoaStructureModel.Design design = type.getDesign(designName);
                    return new StoaStructureModel(ownerId, center, type, design);
                }
            }
        }
        return null;
    }

    public static Block getCenterBlock(Block block) {
        if (block.hasMetadata("stoa.structure")) {
            // Get the data
            String world = MetaUtil.getMetadata(block, "stoa.structure.center.world").asString();
            int x = MetaUtil.getMetadata(block, "stoa.structure.center.x").asInt();
            int y = MetaUtil.getMetadata(block, "stoa.structure.center.y").asInt();
            int z = MetaUtil.getMetadata(block, "stoa.structure.center.z").asInt();

            // Create the location and return the block
            if (Bukkit.getWorld(world) != null) {
                Location center = new Location(Bukkit.getWorld(world), x, y, z);
                return center.getBlock();
            }

            // Data is corrupt
            throw new RuntimeException("Corrupt structure block data at (" + block.getX() + "," + block.getY() + "," + block.getZ() + ")");
        }

        // There is no data to find the center block
        return null;
    }

    public static boolean typeExists(String typeName) {
        return Stoa.getMythos().getStructure(typeName) != null;
    }

    public static boolean designExists(StoaStructureModel.Type type, String designName) {
        return type.getDesign(designName) != null;
    }

    public static Set<StoaStructureModel> getStructuresInRegionalArea(Location location) {
        final StoaRegion center = StoaRegion.at(location);
        Set<StoaStructureModel> set = new HashSet<>();
        for (StoaRegion region : center.getSurroundingRegions())
            set.addAll(getStructuresInSingleRegion(region));
        return set;
    }

    public static Collection<StoaStructureModel> getStructuresInSingleRegion(StoaRegion region) {
        StoaStructureModel alias = new StoaStructureModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).
                    where(alias.world).
                    is(region.getWorld()).
                    and(alias.x).
                    between(region.lowX()).
                    and(region.highX()).
                    and(alias.z).
                    between(region.lowZ()).
                    and(region.highZ()).select();
        } finally {
            db.close();
        }
    }

    public static boolean partOfStructureWithType(final Location location, final String type) {
        return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.typeName.equals(type) && save.getLocations().contains(location);
            }
        });
    }

    public static boolean partOfStructureWithAllFlags(final Location location, final StoaStructureModel.Flag... flags) {
        return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.getLocations().contains(location) && save.getFlags().containsAll(Arrays.asList(flags));
            }
        });
    }

    public static boolean partOfStructureWithFlag(final Location location, final StoaStructureModel.Flag... flags) {
        return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                if (save.getLocations().contains(location)) {
                    for (StoaStructureModel.Flag flag : flags) {
                        if (save.getFlags().contains(flag)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    public static boolean partOfStructureWithFlag(final Location location, final StoaStructureModel.Flag flag) {
        return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.getLocations().contains(location) && save.getFlags().contains(flag);
            }
        });
    }

    public static boolean isClickableBlockWithFlag(final Location location, final StoaStructureModel.Flag flag) {
        return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.getFlags().contains(flag) && save.getClickable().contains(location);
            }
        });
    }

    public static boolean isInRadiusWithFlag(Location location, StoaStructureModel.Flag flag) {
        return !getInRadiusWithFlag(location, flag).isEmpty();
    }

    public static Collection<StoaStructureModel> getInRadiusWithFlag(final Location location, final StoaStructureModel.Flag... flags) {
        return Collections2.filter(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.getCenter().getWorld().equals(location.getWorld()) && save.getCenter().getLocation().distance(location) <= save.getType().getRadius() && save.getFlags().containsAll(Arrays.asList(flags));
            }
        });
    }

    public static Collection<StoaStructureModel> getInRadiusWithFlag(final Location location, final StoaStructureModel.Flag flag) {
        return Collections2.filter(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.getCenter().getWorld().equals(location.getWorld()) && save.getCenter().getLocation().distance(location) <= save.getType().getRadius() && save.getFlags().contains(flag);
            }
        });
    }

    public static StoaStructureModel closestInRadiusWithFlag(final Location location, final StoaStructureModel.Flag flag) {
        StoaStructureModel found = null;
        double nearestDistance = Double.MAX_VALUE;
        for (StoaStructureModel save : getStructuresInRegionalArea(location)) {
            if (save.getFlags().contains(flag)) {
                double distance = save.getCenter().getLocation().distance(location);
                if (distance <= save.getType().getRadius() && distance < nearestDistance) {
                    found = save;
                    nearestDistance = distance;
                }
            }
        }
        return found;
    }

    public static Collection<StoaStructureModel> getInRadiusWithFlag(final Location location, final StoaStructureModel.Flag flag, final int radius) {
        return Collections2.filter(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.getCenter().getWorld().equals(location.getWorld()) && save.getCenter().getLocation().distance(location) <= radius && save.getFlags().contains(flag);
            }
        });
    }

    public static void regenerateStructures() {
        StoaStructureModel alias = new StoaStructureModel();
        Db db = StoaServer.openDb();
        for (StoaStructureModel save : db.from(alias).select()) {
            save.getSchematic().generate(save.getCenter().getLocation());
        }
        db.close();
    }

    public static Set<StoaStructureModel> getStructuresWithFlag(final StoaStructureModel.Flag flag) {
        Set<StoaStructureModel> structures = Sets.newHashSet();
        for (StoaStructureModel.Type structure : Stoa.getMythos().getStructures()) {
            if (structure.getFlags().contains(flag)) {
                structures.addAll(getStructuresWithType(structure.getName()));
            }
        }
        return structures;
    }

    public static Collection<StoaStructureModel> getStructuresWithType(final String typeName) {
        StoaStructureModel alias = new StoaStructureModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).
                    where(alias.typeName).
                    is(typeName).
                    select();
        } finally {
            db.close();
        }
    }

    public static boolean noOverlapStructureNearby(Location location) {
        return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StoaStructureModel>() {
            @Override
            public boolean apply(StoaStructureModel save) {
                return save.getFlags().contains(StoaStructureModel.Flag.NO_OVERLAP);
            }
        });
    }

    /**
     * Strictly checks the <code>reference</code> location to validate if the area is safe
     * for automated generation.
     *
     * @param reference the location to be checked
     * @param area      how big of an area (in blocks) to validate
     * @return Boolean
     */
    public static boolean canGenerateStrict(Location reference, int area) {
        Location location = reference.clone();
        location.subtract(0, 1, 0);
        location.add((area / 3), 0, (area / 2));

        // Check ground
        for (int i = 0; i < area; i++) {
            if (!location.getBlock().getType().isSolid()) return false;
            location.subtract(1, 0, 0);
        }

        // Check ground adjacent
        for (int i = 0; i < area; i++) {
            if (!location.getBlock().getType().isSolid()) return false;
            location.subtract(0, 0, 1);
        }

        // Check ground adjacent again
        for (int i = 0; i < area; i++) {
            if (!location.getBlock().getType().isSolid()) return false;
            location.add(1, 0, 0);
        }

        location.add(0, 1, 0);

        // Check air diagonally
        for (int i = 0; i < area + 1; i++) {
            if (location.getBlock().getType().isSolid()) return false;
            location.add(0, 1, 1);
            location.subtract(1, 0, 0);
        }

        return true;
    }

    /**
     * Updates favor for all structures.
     */
    public static void updateSanctity() {
        for (StoaStructureModel data : getStructuresWithFlag(StoaStructureModel.Flag.DESTRUCT_ON_BREAK))
            data.corrupt(-1.0 * data.getType().getSanctityRegen());
    }
}
