package com.demigodsrpg.stoa.util;

import com.censoredsoftware.library.util.RandomUtil;
import com.censoredsoftware.library.util.VehicleUtil;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.event.BattleDeathEvent;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public class BattleUtil {
    public static Battle create(Participant damager, Participant damaged) {
        Battle battle = new Battle(damager, damaged);

        // Log the creation
        MessageUtil.info(English.LOG_BATTLE_STARTED.getLine().replace("{locX}", battle.getStartLocation().getX() + "").replace("{locY}", battle.getStartLocation().getY() + "").replace("{locZ}", battle.getStartLocation().getZ() + "").replace("{world}", battle.getStartLocation().getWorld().getName()).replace("{starter}", battle.getStartedBy().getCharacter().getName()));

        return battle;
    }

    public static Collection<Battle> getAllActive() {
        return Collections2.filter(Battle.BATTLE_MAP.values(), new Predicate<Battle>() {
            @Override
            public boolean apply(Battle battle) {
                return battle.getActive();
            }
        });
    }

    public static Collection<Battle> getAllInactive() {
        return Collections2.filter(Battle.BATTLE_MAP.values(), new Predicate<Battle>() {
            @Override
            public boolean apply(Battle battle) {
                return !battle.getActive();
            }
        });
    }

    public static boolean existsInRadius(Location location) {
        return getInRadius(location) != null;
    }

    public static Battle getInRadius(final Location location) {
        return Iterables.find(Battle.BATTLE_MAP.values(), new Predicate<Battle>() {
            @Override
            public boolean apply(Battle battle) {
                return battle.getActive() && battle.getStartLocation().distance(location) <= battle.getRadius();
            }
        }, null);
    }

    public static boolean isInBattle(final Participant participant) {
        return Iterators.any(getAllActive().iterator(), new Predicate<Battle>() {
            @Override
            public boolean apply(Battle battle) {
                return battle.getParticipants().contains(participant);
            }
        });
    }

    public static Battle getBattle(final Participant participant) {
        try {
            return Iterators.find(getAllActive().iterator(), new Predicate<Battle>() {
                @Override
                public boolean apply(Battle battle) {
                    return battle.getParticipants().contains(participant);
                }
            });
        } catch (NoSuchElementException ignored) {
            // ignored
        }
        return null;
    }

    public static boolean existsNear(Location location) {
        return getNear(location) != null;
    }

    public static Battle getNear(final Location location) {
        try {
            return Iterators.find(getAllActive().iterator(), new Predicate<Battle>() {
                @Override
                public boolean apply(Battle battle) {
                    double distance = battle.getStartLocation().distance(location);
                    return distance > battle.getRadius() && distance <= StoaPlugin.config().getInt("battles.merge_radius");
                }
            });
        } catch (NoSuchElementException ignored) {
            // ignored
        }
        return null;
    }

    public static Collection<Location> battleBorder(final Battle battle) {
        if (!StoaServer.isRunningSpigot()) throw new RuntimeException("Cannot find Spigot.");
        return Collections2.transform(LocationUtil.getCirclePoints(battle.getStartLocation(), battle.getRadius(), 120), new Function<Location, Location>() {
            @Override
            public Location apply(Location point) {
                return new Location(point.getWorld(), point.getBlockX(), point.getWorld().getHighestBlockYAt(point), point.getBlockZ());
            }
        });
    }

    /*
     * This is completely broken. TODO
     */
    public static Location randomRespawnPoint(Battle battle) {
        List<Location> respawnPoints = getSafeRespawnPoints(battle);
        if (respawnPoints.size() == 0) return battle.getStartLocation();

        Location target = respawnPoints.get(RandomUtil.generateIntRange(0, respawnPoints.size() - 1));

        Vector direction = target.toVector().subtract(battle.getStartLocation().toVector()).normalize();
        double X = direction.getX();
        double Y = direction.getY();
        double Z = direction.getZ();

        // Now change the angle FIXME
        Location changed = target.clone();
        changed.setYaw(180 - LocationUtil.toDegree(Math.atan2(Y, X)));
        changed.setPitch(90 - LocationUtil.toDegree(Math.acos(Z)));
        return changed;
    }

    /*
     * This is completely broken. TODO
     */
    public static boolean isSafeLocation(Location reference, Location checking) {
        if (checking.getBlock().getType().isSolid() || checking.getBlock().getType().equals(Material.LAVA)) {
            return false;
        }
        double referenceY = reference.getY();
        double checkingY = checking.getY();
        return Math.abs(referenceY - checkingY) <= 5;
    }

    public static List<Location> getSafeRespawnPoints(final Battle battle) {
        return Lists.newArrayList(Collections2.filter(Collections2.transform(LocationUtil.getCirclePoints(battle.getStartLocation(), battle.getRadius() - 1.5, 100), new Function<Location, Location>() {
            @Override
            public Location apply(Location point) {
                return new Location(point.getWorld(), point.getBlockX(), point.getWorld().getHighestBlockYAt(point), point.getBlockZ());
            }
        }), new Predicate<Location>() {
            @Override
            public boolean apply(Location location) {
                return isSafeLocation(battle.getStartLocation(), location);
            }
        }));
    }

    public static boolean canParticipate(Entity entity) {
        if (entity instanceof Player) {
            CharacterModel character = CharacterUtil.currentFromPlayer((Player) entity);
            return character != null && !character.getDeity().getFlags().contains(Deity.Flag.NO_BATTLE);
        }
        return entity instanceof Tameable && TameableUtil.fromEntity((LivingEntity) entity) != null && isInBattle(TameableUtil.fromEntity((LivingEntity) entity).getCharacter());
    }

    public static Participant defineParticipant(Entity entity) {
        if (!canParticipate(entity)) return null;
        if (entity instanceof Player) return CharacterUtil.currentFromPlayer((Player) entity);
        return TameableUtil.fromEntity((LivingEntity) entity);
    }

    public static void battleDeath(Participant damager, Participant damagee, Battle battle) {
        BattleDeathEvent event = new BattleDeathEvent(battle, damagee, damager);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        if (damager instanceof CharacterModel) ((CharacterModel) damager).addKill();
        if (damager.getOfflinePlayer().isOnline())
            damager.getOfflinePlayer().getPlayer().sendMessage(ChatColor.GREEN + "+1 Kill.");
        battle.addKill(damager);
        damagee.getEntity().setHealth(damagee.getEntity().getMaxHealth());
        VehicleUtil.teleport(damagee.getEntity(), randomRespawnPoint(battle));
        if (damagee instanceof CharacterModel) {
            CharacterModel character = (CharacterModel) damagee;
            Player player = character.getEntity();
            player.setFoodLevel(20);
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
            character.setPotionEffects(player.getActivePotionEffects());
            character.addDeath();
        }
        if (damagee.getOfflinePlayer().isOnline())
            damagee.getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "+1 Death.");
        battle.addDeath(damagee);
    }

    public static void battleDeath(Participant damagee, Battle battle) {
        BattleDeathEvent event = new BattleDeathEvent(battle, damagee);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        damagee.getEntity().setHealth(damagee.getEntity().getMaxHealth());
        damagee.getEntity().teleport(randomRespawnPoint(battle));
        if (damagee instanceof CharacterModel) ((CharacterModel) damagee).addDeath();
        if (damagee.getOfflinePlayer().isOnline())
            damagee.getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "+1 Death.");
        battle.addDeath(damagee);
    }

    public static boolean canTarget(Entity entity) {
        return !canParticipate(entity) || canParticipate(entity) && canTarget(defineParticipant(entity));
    }

    /**
     * Returns true if target is allowed for <code>player</code>.
     *
     * @param participant the player to check.
     * @return true/false depending on if target is allowed.
     */
    public static boolean canTarget(Participant participant) // TODO REDO THIS
    {
        return participant == null || participant.canPvp() || participant.getCurrentLocation() != null && !ZoneUtil.inNoPvpZone(participant.getCurrentLocation());
    }

    /**
     * Updates all battle particles. Meant for use in a Runnable.
     */
    public static void updateBattleParticles() {
        for (Battle battle : getAllActive())
            for (Location point : battleBorder(battle))
                point.getWorld().playEffect(point, Effect.MOBSPAWNER_FLAMES, 0, (int) (battle.getRadius() * 2));
    }

    /**
     * Updates all battles.
     */
    public static void updateBattles() {
        // End all active battles that should end.
        for (Battle battle : Collections2.filter(getAllActive(), new Predicate<Battle>() {
            @Override
            public boolean apply(Battle battle) {
                return battle.getKillCounter() >= battle.getMaxKills() || battle.getStartTime() + battle.getDuration() <= System.currentTimeMillis() && battle.getKillCounter() >= battle.getMinKills() || battle.getParticipants().size() < 2 || battle.getInvolvedAlliances().size() < 2;
            }
        })) {
            battle.end();
            SkillUtil.processBattle(battle);
        }

        // Delete all inactive battles that should be deleted.
        for (Battle battle : Collections2.filter(getAllInactive(), new Predicate<Battle>() {
            @Override
            public boolean apply(Battle battle) {
                return battle.getDeleteTime() >= System.currentTimeMillis();
            }
        }))
            battle.remove();
    }

    /**
     * Updates the scoreboard for the given <code>player</code> with information from the <code>battle</code>.
     *
     * @param player the player to give the scoreboard to.
     * @param battle the battle to grab stats from.
     */
    public static void updateScoreboard(Player player, Battle battle) {
        // Define variables
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Define sidebar objective
        Objective info = scoreboard.registerNewObjective("battle_info", "dummy");
        info.setDisplaySlot(DisplaySlot.SIDEBAR);
        info.setDisplayName(ChatColor.AQUA + "Current Battle Stats");

        // Add the information
        Score kills = info.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Total Kills"));
        kills.setScore(battle.getKillCounter());

        Score neededKills = info.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Kills Needed"));
        neededKills.setScore(battle.getMinKills());

        for (Alliance alliance : battle.getInvolvedAlliances()) {
            Score allianceKills = info.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + alliance.getName() + ChatColor.GRAY + " Score"));
            allianceKills.setScore(battle.getScore(alliance));
        }

        Score participants = info.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Participants"));
        participants.setScore(battle.getParticipants().size());

        Score points = info.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Duration"));
        points.setScore((int) (System.currentTimeMillis() - battle.getStartTime()) / 1000);

        player.setScoreboard(scoreboard);
    }
}
