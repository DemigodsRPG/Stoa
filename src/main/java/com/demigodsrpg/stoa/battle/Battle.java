package com.demigodsrpg.stoa.battle;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.censoredsoftware.library.data.ServerData;
import com.censoredsoftware.library.messages.CommonSymbol;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.data.DataAccess;
import com.demigodsrpg.stoa.data.DataManager;
import com.demigodsrpg.stoa.data.WorldDataManager;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.location.StoaLocation;
import com.demigodsrpg.stoa.util.Configs;
import com.demigodsrpg.stoa.util.MessageUtil;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Battle extends DataAccess<UUID, Battle> {
    private UUID id;
    private String world;
    private UUID startLoc;
    private boolean active;
    private long startTime;
    private long deleteTime;
    private Set<String> involvedPlayers;
    private Set<String> involvedTameable;
    private int killCounter;
    private int runnableId;
    private Map<String, Object> kills;
    private Map<String, Object> deaths;
    private UUID startedBy;

    private Battle(Object ignored) {
    }

    public Battle() {
        this.kills = Maps.newHashMap();
        this.deaths = Maps.newHashMap();
        this.involvedPlayers = Sets.newHashSet();
        this.involvedTameable = Sets.newHashSet();
        this.killCounter = 0;
    }

    @DataProvider(idType = DefaultIdType.UUID)
    public static Battle of(UUID id, ConfigurationSection conf) {
        Battle battle = new Battle(null);
        battle.id = id;
        battle.world = conf.getString("world");
        battle.startLoc = UUID.fromString(conf.getString("startLoc"));
        battle.active = conf.getBoolean("active");
        battle.startTime = conf.getLong("startTime");
        battle.deleteTime = conf.getLong("deleteTime");
        battle.involvedPlayers = Sets.newHashSet(conf.getStringList("involvedPlayers"));
        battle.involvedTameable = Sets.newHashSet(conf.getStringList("involvedTameable"));
        battle.killCounter = conf.getInt("killCounter");
        battle.runnableId = conf.getInt("runnableId");
        battle.kills = conf.getConfigurationSection("kills").getValues(false);
        battle.deaths = conf.getConfigurationSection("deaths").getValues(false);
        battle.startedBy = UUID.fromString(conf.getString("startedBy"));
        return battle;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("world", world);
        map.put("startLoc", startLoc.toString());
        map.put("active", active);
        map.put("startTime", startTime);
        map.put("deleteTime", deleteTime);
        map.put("involvedPlayers", Lists.newArrayList(involvedPlayers));
        map.put("involvedTameable", Lists.newArrayList(involvedTameable));
        map.put("killCounter", killCounter);
        map.put("runnableId", runnableId);
        map.put("kills", kills);
        map.put("deaths", deaths);
        map.put("startedBy", startedBy.toString());
        return map;
    }

    public void generateId() {
        id = UUID.randomUUID();
    }

    public void setActive() {
        this.active = true;
        save();
    }

    public void setInactive() {
        this.active = false;
        save();
    }

    void setStartLocation(Location location) {
        this.startLoc = StoaLocation.track(location).getId();
        this.world = location.getWorld().getName();
    }

    void setStartTime(long time) {
        this.startTime = time;
    }

    void setDeleteTime(long time) {
        this.deleteTime = time;
        save();
    }

    public UUID getId() {
        return this.id;
    }

    public double getRadius() {
        int base = Configs.getSettingInt("battles.min_radius");
        if (involvedPlayers.size() > 2) return base * Math.log10(10 + Math.ceil(Math.pow(involvedPlayers.size(), 1.5)));
        return base;
    }

    public boolean isActive() {
        return this.active;
    }

    public long getDuration() {
        long base = Configs.getSettingInt("battles.min_duration") * 1000;
        long per = Configs.getSettingInt("battles.duration_multiplier") * 1000;
        if (involvedPlayers.size() > 2) return base + (per * (involvedPlayers.size() - 2));
        return base;
    }

    public int getMinKills() {
        int base = Configs.getSettingInt("battles.min_kills");
        int per = 2;
        if (involvedPlayers.size() > 2) return base + (per * (involvedPlayers.size() - 2));
        return base;
    }

    public int getMaxKills() {
        int base = Configs.getSettingInt("battles.max_kills");
        int per = 3;
        if (involvedPlayers.size() > 2) return base + (per * (involvedPlayers.size() - 2));
        return base;
    }

    public StoaLocation getStartLocation() {
        return StoaLocation.get(WorldDataManager.getWorld(world), this.startLoc);
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getDeleteTime() {
        return this.deleteTime;
    }

    void setStarter(StoaCharacter character) {
        this.startedBy = character.getId();
        addParticipant(character);
    }

    public void addParticipant(Participant participant) {
        if (participant instanceof StoaCharacter) this.involvedPlayers.add((participant.getId().toString()));
        else this.involvedTameable.add(participant.getId().toString());
        save();
    }

    public void removeParticipant(Participant participant) {
        if (participant instanceof StoaCharacter) this.involvedPlayers.remove((participant.getId().toString()));
        else this.involvedTameable.remove(participant.getId().toString());
        save();
    }

    public void addKill(Participant participant) {
        this.killCounter += 1;
        StoaCharacter character = participant.getRelatedCharacter();
        if (this.kills.containsKey(character.getId().toString()))
            this.kills.put(character.getId().toString(), Integer.parseInt(this.kills.get(character.getId().toString()).toString()) + 1);
        else this.kills.put(character.getId().toString(), 1);
        save();
    }

    public void addDeath(Participant participant) {
        StoaCharacter character = participant.getRelatedCharacter();
        if (this.deaths.containsKey(character.getId().toString()))
            this.deaths.put(character.getId().toString(), Integer.parseInt(this.deaths.get(character.getId().toString()).toString()) + 1);
        else this.deaths.put(character.getId().toString(), 1);
        save();
    }

    public StoaCharacter getStarter() {
        return StoaCharacter.get(startedBy);
    }

    public Set<Participant> getParticipants() {
        return Sets.filter(Sets.union(Sets.newHashSet(Collections2.transform(involvedPlayers, new Function<String, Participant>() {
            @Override
            public Participant apply(String character) {
                return StoaCharacter.get(UUID.fromString(character));
            }
        })), Sets.newHashSet(Collections2.transform(involvedTameable, new Function<String, Participant>() {
            @Override
            public Participant apply(String tamable) {
                return StoaTameable.get(UUID.fromString(tamable));
            }
        }))), new Predicate<Participant>() {
            @Override
            public boolean apply(@Nullable Participant participant) {
                return participant != null && participant.getRelatedCharacter() != null;
            }
        });
    }

    public Collection<Alliance> getInvolvedAlliances() {
        Set<Alliance> set = Sets.newHashSet();
        for (Participant participant : getParticipants())
            set.add(participant.getRelatedCharacter().getAlliance());
        return set;
    }

    public int getKills(Participant participant) {
        try {
            return Integer.parseInt(kills.get(participant.getId().toString()).toString());
        } catch (Exception ignored) {
            // ignored
        }
        return 0;
    }

    public int getDeaths(Participant participant) {
        try {
            return Integer.parseInt(deaths.get(participant.getId().toString()).toString());
        } catch (Exception ignored) {
            // ignored
        }
        return 0;
    }

    public Map<UUID, Integer> getScores() {
        Map<UUID, Integer> score = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : kills.entrySet()) {
            if (!getParticipants().contains(StoaCharacter.get(UUID.fromString(entry.getKey())))) continue;
            score.put(UUID.fromString(entry.getKey()), Integer.parseInt(entry.getValue().toString()));
        }
        for (Map.Entry<String, Object> entry : deaths.entrySet()) {
            int base = 0;
            if (score.containsKey(UUID.fromString(entry.getKey()))) base = score.get(UUID.fromString(entry.getKey()));
            score.put(UUID.fromString(entry.getKey()), base - Integer.parseInt(entry.getValue().toString()));
        }
        return score;
    }

    public int getScore(final Alliance alliance) {
        Map<UUID, Integer> score = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : kills.entrySet()) {
            if (!getParticipants().contains(StoaCharacter.get(UUID.fromString(entry.getKey())))) continue;
            score.put(UUID.fromString(entry.getKey()), Integer.parseInt(entry.getValue().toString()));
        }
        for (Map.Entry<String, Object> entry : deaths.entrySet()) {
            int base = 0;
            if (score.containsKey(UUID.fromString(entry.getKey()))) base = score.get(UUID.fromString(entry.getKey()));
            score.put(UUID.fromString(entry.getKey()), base - Integer.parseInt(entry.getValue().toString()));
        }
        int sum = 0;
        for (int i : Collections2.transform(Collections2.filter(score.entrySet(), new Predicate<Map.Entry<UUID, Integer>>() {
            @Override
            public boolean apply(Map.Entry<UUID, Integer> entry) {
                return StoaCharacter.get(entry.getKey()).getAlliance().getName().equalsIgnoreCase(alliance.getName());
            }
        }), new Function<Map.Entry<UUID, Integer>, Integer>() {
            @Override
            public Integer apply(Map.Entry<UUID, Integer> entry) {
                return entry.getValue();
            }
        }))
            sum += i;
        return sum;
    }

    public Collection<StoaCharacter> getMVPs() {
        final int max = Collections.max(getScores().values());
        return Collections2.transform(Collections2.filter(getScores().entrySet(), new Predicate<Map.Entry<UUID, Integer>>() {
            @Override
            public boolean apply(Map.Entry<UUID, Integer> entry) {
                return entry.getValue() == max;
            }
        }), new Function<Map.Entry<UUID, Integer>, StoaCharacter>() {
            @Override
            public StoaCharacter apply(Map.Entry<UUID, Integer> entry) {
                return StoaCharacter.get(entry.getKey());
            }
        });
    }

    public int getKillCounter() {
        return this.killCounter;
    }

    public void end() // TODO Make this specify that it was a pet that won/lost a duel
    {
        for (String stringId : involvedPlayers)
            ServerData.put(DataManager.DATA_MANAGER, stringId, "just_finished_battle", true, 1, TimeUnit.MINUTES);

        Map<UUID, Integer> scores = getScores();
        List<UUID> participants = Lists.newArrayList(scores.keySet());
        if (participants.size() == 2) {
            if (scores.get(participants.get(0)).equals(scores.get(participants.get(1)))) {
                StoaCharacter one = StoaCharacter.get(participants.get(0));
                StoaCharacter two = StoaCharacter.get(participants.get(1));
                MessageUtil.broadcast(one.getDeity().getColor() + one.getName() + ChatColor.GRAY + " and " + two.getDeity().getColor() + two.getName() + ChatColor.GRAY + " just tied in a duel.");
            } else {
                int winnerIndex = scores.get(participants.get(0)) > scores.get(participants.get(1)) ? 0 : 1;
                StoaCharacter winner = StoaCharacter.get(participants.get(winnerIndex));
                StoaCharacter loser = StoaCharacter.get(participants.get(winnerIndex == 0 ? 1 : 0));
                MessageUtil.broadcast(winner.getDeity().getColor() + winner.getName() + ChatColor.GRAY + " just won in a duel against " + loser.getDeity().getColor() + loser.getName() + ChatColor.GRAY + ".");
            }
        } else if (participants.size() > 2) {
            Alliance winningAlliance = null;
            int winningScore = 0;
            Collection<StoaCharacter> MVPs = getMVPs();
            boolean oneMVP = MVPs.size() == 1;
            for (Alliance alliance : getInvolvedAlliances()) {
                int score = getScore(alliance);
                if (getScore(alliance) > winningScore) {
                    winningAlliance = alliance;
                    winningScore = score;
                }
            }
            if (winningAlliance != null) {
                MessageUtil.broadcast(ChatColor.GRAY + "The " + ChatColor.YELLOW + winningAlliance.getName() + "s " + ChatColor.GRAY + "just won a battle involving " + involvedPlayers.size() + " participants.");
                MessageUtil.broadcast(ChatColor.GRAY + "The " + ChatColor.YELLOW + "MVP" + (oneMVP ? "" : "s") + ChatColor.GRAY + " from this battle " + (oneMVP ? "is" : "are") + ":");
                for (StoaCharacter mvp : MVPs)
                    MessageUtil.broadcast(" " + ChatColor.DARK_GRAY + CommonSymbol.RIGHTWARD_ARROW + " " + mvp.getDeity().getColor() + mvp.getName() + ChatColor.GRAY + " / " + ChatColor.YELLOW + "Kills" + ChatColor.GRAY + ": " + getKills(mvp) + " / " + ChatColor.YELLOW + "Deaths" + ChatColor.GRAY + ": " + getDeaths(mvp));
            }
        }

        // Reset scoreboards
        resetScoreboards();

        // Remind of cooldown
        sendMessage(ChatColor.YELLOW + "You are safe for 60 seconds.");

        // Prepare for graceful delete
        setDeleteTime(System.currentTimeMillis() + 3000L);
        setInactive();
    }

    public void sendMessage(String message) {
        for (String stringId : involvedPlayers) {
            OfflinePlayer offlinePlayer = StoaCharacter.get(UUID.fromString(stringId)).getBukkitOfflinePlayer();
            if (offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(message);
        }
    }

    public void startScoreboardRunnable() {
        final Battle battle = this;

        runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(StoaPlugin.getInst(), new BukkitRunnable() {
            @Override
            public void run() {
                // TODO: This loop could cause some lag
                for (String stringId : involvedPlayers) {
                    OfflinePlayer offlinePlayer = StoaCharacter.get(UUID.fromString(stringId)).getBukkitOfflinePlayer();
                    if (offlinePlayer.isOnline()) updateScoreboard(offlinePlayer.getPlayer(), battle);
                }
            }
        }, 20, 20);
    }

    public void resetScoreboards() {
        // Cancel the runnable
        Bukkit.getScheduler().cancelTask(runnableId);

        // Clear the scoreboards
        for (String stringId : involvedPlayers) {
            OfflinePlayer offlinePlayer = StoaCharacter.get(UUID.fromString(stringId)).getBukkitOfflinePlayer();
            if (offlinePlayer.isOnline())
                offlinePlayer.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    // -- STATIC GETTERS/SETTERS -- //

    private static final DataAccess<UUID, Battle> DATA_ACCESS = new Battle(null);

    public static Battle get(UUID id) {
        return DATA_ACCESS.getDirect(id);
    }

    public static Collection<Battle> all() {
        return DATA_ACCESS.allDirect();
    }

    // -- UTIL METHODS -- //


}
