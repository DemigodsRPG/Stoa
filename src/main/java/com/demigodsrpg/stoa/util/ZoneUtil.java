package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.listener.ZoneListener;
import com.demigodsrpg.stoa.model.StructureModel;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Set;

public class ZoneUtil {
    private static final Set<String> ENABLED_WORLDS = Sets.newHashSet();
    private static final Configuration PLUGIN_CONFIG = StoaPlugin.getInst().getConfig();

    public static int init() {
        // Load disabled worlds
        Set<String> enabledWorlds = Sets.newHashSet();
        int erroredWorlds = 0;
        for (String world : PLUGIN_CONFIG.getStringList("restrictions.enabled_worlds")) {
            enabledWorlds.add(world);
            erroredWorlds += Bukkit.getServer().getWorld(world) == null ? 1 : 0;
        }
        ENABLED_WORLDS.addAll(enabledWorlds);

        // Zone listener (load here for consistency)
        Bukkit.getPluginManager().registerEvents(new ZoneListener(), StoaPlugin.getInst());

        // Init WorldGuard stuff
        WorldGuardUtil.setWhenToOverridePVP(StoaPlugin.getInst(), new Predicate<EntityDamageByEntityEvent>() {
            @Override
            public boolean apply(EntityDamageByEntityEvent event) {
                return !inNoStoaZone(event.getEntity().getLocation());
            }
        });

        return erroredWorlds;
    }

    /**
     * Returns true if <code>location</code> is within a no-PVP zone.
     *
     * @param location the location to check.
     * @return true/false depending on if it's a no-PVP zone or not.
     */
    public static boolean inNoPvpZone(Location location) {
        if (PLUGIN_CONFIG.getBoolean("zones.allow_skills_anywhere")) return false;
        if (WorldGuardUtil.worldGuardEnabled())
            return StructureUtil.isInRadiusWithFlag(location, StructureModel.Flag.NO_PVP) || !WorldGuardUtil.canPVP(location);
        return StructureUtil.isInRadiusWithFlag(location, StructureModel.Flag.NO_PVP);
    }

    /**
     * Returns true if <code>location</code> is within a no-build zone
     * for <code>player</code>.
     *
     * @param player   the player to check.
     * @param location the location to check.
     * @return true/false depending on the position of the <code>player</code>.
     */
    public static boolean inNoBuildZone(Player player, Location location) {
        if (WorldGuardUtil.worldGuardEnabled() && !WorldGuardUtil.canBuild(player, location)) return true;
        StructureModel save = Iterables.getFirst(StructureUtil.getInRadiusWithFlag(location, StructureModel.Flag.NO_GRIEFING), null);
        return CharacterUtil.currentFromPlayer(player) != null && save != null && !CharacterUtil.currentFromPlayer(player).uuid.equals(save.getOwnerId());
    }

    public static boolean inNoStoaZone(Location location) {
        return isNoStoaWorld(location.getWorld()); // || WorldGuards.worldGuardEnabled() && WorldGuards.checkForCreatedFlagValue("demigods", "deny", location);
    }

    public static boolean isNoStoaWorld(World world) {
        return !ENABLED_WORLDS.contains(world.getName());
    }

    public static void enableWorld(String worldName) {
        ENABLED_WORLDS.add(worldName);
    }

    public static void disableWorld(String worldName) {
        ENABLED_WORLDS.remove(worldName);
    }
}
