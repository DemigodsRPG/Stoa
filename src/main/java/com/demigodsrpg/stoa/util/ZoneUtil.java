package com.demigodsrpg.stoa.util;

import com.censoredsoftware.library.util.WorldGuards;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.listener.ZoneListener;
import com.demigodsrpg.stoa.model.StoaStructureModel;
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
        WorldGuards.createFlag("STATE", "demigods", true, "ALL");
        WorldGuards.registerCreatedFlag("demigods");
        WorldGuards.setWhenToOverridePVP(StoaPlugin.getInst(), new Predicate<EntityDamageByEntityEvent>() {
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
        if (WorldGuards.worldGuardEnabled())
            return StoaStructureModel.Type.Util.isInRadiusWithFlag(location, StoaStructureModel.Type.Flag.NO_PVP) || !WorldGuards.canPVP(location);
        return StoaStructureModel.Type.Util.isInRadiusWithFlag(location, StoaStructureModel.Type.Flag.NO_PVP);
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
        if (WorldGuards.worldGuardEnabled() && !WorldGuards.canBuild(player, location)) return true;
        StoaStructureModel save = Iterables.getFirst(StoaStructureModel.Type.Util.getInRadiusWithFlag(location, StoaStructureModel.Type.Flag.NO_GRIEFING), null);
        return StoaCharacter.of(player) != null && save != null && !StoaCharacter.of(player).getId().equals(save.getOwner());
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
