package com.demigodsrpg.stoa.battle;

import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.language.CommonSymbol;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.util.BattleUtil;
import com.demigodsrpg.stoa.util.CharacterUtil;
import com.demigodsrpg.stoa.util.MessageUtil;
import com.demigodsrpg.stoa.util.ServerDataUtil;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class Battle {
    public static final ConcurrentMap<UUID, Battle> BATTLE_MAP = new ConcurrentHashMap<>();

    private transient UUID id;
    private transient Boolean active;

    private transient Location startLocation;
    private transient Participant startedBy;
    private transient Long startTime;
    private transient Long deleteTime;
    private transient Set<Participant> participants;
    private transient Integer killCounter;
    private transient Map<Participant, Integer> kills;
    private transient Map<Participant, Integer> deaths;

    private transient Integer runnableId;

    public Battle(Participant damager, Participant damaged) {
        id = UUID.randomUUID();

        active = true;

        startLocation = damager.getCurrentLocation();
        startedBy = damager;
        startTime = System.currentTimeMillis();
        participants = new HashSet<>();
        killCounter = 0;
        kills = new HashMap<>();
        deaths = new HashMap<>();

        participants.add(damager);
        participants.add(damaged);
    }

    public UUID getId() {
        return id;
    }

    public double getRadius() {
        int base = StoaPlugin.config().getInt("battles.min_radius");
        if (participants.size() > 2) return base * Math.log10(10 + Math.ceil(Math.pow(participants.size(), 1.5)));
        return base;
    }

    public boolean isActive() {
        return this.active;
    }

    public long getDuration() {
        long base = StoaPlugin.config().getInt("battles.min_duration") * 1000;
        long per = StoaPlugin.config().getInt("battles.duration_multiplier") * 1000;
        if (participants.size() > 2) return base + (per * (participants.size() - 2));
        return base;
    }

    public int getMinKills() {
        int base = StoaPlugin.config().getInt("battles.min_kills");
        int per = 2;
        if (participants.size() > 2) return base + (per * (participants.size() - 2));
        return base;
    }

    public int getMaxKills() {
        int base = StoaPlugin.config().getInt("battles.max_kills");
        int per = 3;
        if (participants.size() > 2) return base + (per * (participants.size() - 2));
        return base;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Participant getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(Participant startedBy) {
        this.startedBy = startedBy;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    public void setKillCounter(Integer killCounter) {
        this.killCounter = killCounter;
    }

    public Map<Participant, Integer> getKills() {
        return kills;
    }

    public void addDeath(Participant participant) {
        int death = deaths.get(participant);
        deaths.put(participant, death + 1);
    }

    public void addKill(Participant participant) {
        int kill = kills.get(participant);
        kills.put(participant, kill + 1);
    }

    public void setKills(Map<Participant, Integer> kills) {
        this.kills = kills;
    }

    public Map<Participant, Integer> getDeaths() {
        return deaths;
    }

    public void setDeaths(Map<Participant, Integer> deaths) {
        this.deaths = deaths;
    }

    public Integer getRunnableId() {
        return runnableId;
    }

    public void setRunnableId(Integer runnableId) {
        this.runnableId = runnableId;
    }

    public Map<Participant, Integer> getScores() {
        Map<Participant, Integer> score = Maps.newHashMap();
        for (Map.Entry<Participant, Integer> entry : kills.entrySet()) {
            if (!getParticipants().contains(entry.getKey())) continue;
            score.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Participant, Integer> entry : deaths.entrySet()) {
            int base = 0;
            if (score.containsKey(entry.getKey())) base = score.get(entry.getKey());
            score.put(entry.getKey(), base - entry.getValue());
        }
        return score;
    }

    public int getScore(final Alliance alliance) {
        Map<Participant, Integer> score = Maps.newHashMap();
        for (Map.Entry<Participant, Integer> entry : kills.entrySet()) {
            if (!getParticipants().contains((entry.getKey()))) continue;
            score.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Participant, Integer> entry : deaths.entrySet()) {
            int base = 0;
            if (score.containsKey(entry.getKey())) base = score.get(entry.getKey());
            score.put(entry.getKey(), base - entry.getValue());
        }
        int sum = 0;
        for (int i : Collections2.transform(Collections2.filter(score.entrySet(), new Predicate<Map.Entry<Participant, Integer>>() {
            @Override
            public boolean apply(Map.Entry<Participant, Integer> entry) {
                return entry.getKey().getCharacter().getAlliance().getName().equalsIgnoreCase(alliance.getName());
            }
        }), new Function<Map.Entry<Participant, Integer>, Integer>() {
            @Override
            public Integer apply(Map.Entry<Participant, Integer> entry) {
                return entry.getValue();
            }
        }))
            sum += i;
        return sum;
    }

    public Collection<String> getMVPs() {
        final int max = Collections.max(getScores().values());
        return Collections2.transform(Collections2.filter(getScores().entrySet(), new Predicate<Map.Entry<Participant, Integer>>() {
            @Override
            public boolean apply(Map.Entry<Participant, Integer> entry) {
                return entry.getValue() == max;
            }
        }), new Function<Map.Entry<Participant, Integer>, String>() {
            @Override
            public String apply(Map.Entry<Participant, Integer> entry) {
                return entry.getKey().getId();
            }
        });
    }

    public int getKillCounter() {
        return this.killCounter;
    }

    public void end() // TODO Make this specify that it was a pet that won/lost a duel
    {
        for (Participant participant : participants)
            ServerDataUtil.put(true, 1, TimeUnit.MINUTES, participant.getId(), "just_finished_battle");

        Map<Participant, Integer> scores = getScores();
        List<Participant> participants = Lists.newArrayList(scores.keySet());
        if (participants.size() == 2) {
            if (scores.get(participants.get(0)).equals(scores.get(participants.get(1)))) {
                CharacterModel one = participants.get(0).getCharacter();
                CharacterModel two = participants.get(1).getCharacter();
                MessageUtil.broadcast(one.getDeity().getColor() + one.getName() + ChatColor.GRAY + " and " + two.getDeity().getColor() + two.getName() + ChatColor.GRAY + " just tied in a duel.");
            } else {
                int winnerIndex = scores.get(participants.get(0)) > scores.get(participants.get(1)) ? 0 : 1;
                CharacterModel winner = participants.get(winnerIndex).getCharacter();
                CharacterModel loser = participants.get(winnerIndex == 0 ? 1 : 0).getCharacter();
                MessageUtil.broadcast(winner.getDeity().getColor() + winner.getName() + ChatColor.GRAY + " just won in a duel against " + loser.getDeity().getColor() + loser.getName() + ChatColor.GRAY + ".");
            }
        } else if (participants.size() > 2) {
            Alliance winningAlliance = null;
            int winningScore = 0;
            Collection<String> MVPs = getMVPs();
            boolean oneMVP = MVPs.size() == 1;
            for (Alliance alliance : getInvolvedAlliances()) {
                int score = getScore(alliance);
                if (getScore(alliance) > winningScore) {
                    winningAlliance = alliance;
                    winningScore = score;
                }
            }
            if (winningAlliance != null) {
                MessageUtil.broadcast(ChatColor.GRAY + "The " + ChatColor.YELLOW + winningAlliance.getName() + "s " + ChatColor.GRAY + "just won a battle involving " + participants.size() + " participants.");
                MessageUtil.broadcast(ChatColor.GRAY + "The " + ChatColor.YELLOW + "MVP" + (oneMVP ? "" : "s") + ChatColor.GRAY + " from this battle " + (oneMVP ? "is" : "are") + ":");
                for (String mvpId : MVPs) {
                    CharacterModel mvp = CharacterUtil.fromId(mvpId);
                    MessageUtil.broadcast(" " + ChatColor.DARK_GRAY + CommonSymbol.RIGHTWARD_ARROW + " " + mvp.getDeity().getColor() + mvp.getName() + ChatColor.GRAY + " / " + ChatColor.YELLOW + "Kills" + ChatColor.GRAY + ": " + kills.get(mvp.getId()) + " / " + ChatColor.YELLOW + "Deaths" + ChatColor.GRAY + ": " + deaths.get(mvp.getId()));
                }
            }
        }

        // Reset scoreboards
        resetScoreboards();

        // Remind of cooldown
        sendMessage(ChatColor.YELLOW + "You are safe for 60 seconds.");

        // Prepare for graceful delete
        deleteTime = System.currentTimeMillis() + 3000L;
        setActive(false);
    }

    public Collection<Alliance> getInvolvedAlliances() {
        Set<Alliance> set = Sets.newHashSet();
        for (Participant participant : getParticipants())
            set.add(participant.getCharacter().getAlliance());
        return set;
    }

    public void sendMessage(String message) {
        for (Participant participant : participants) {
            try {
                participant.getCharacter().getEntity().sendMessage(message);
            } catch (Exception ignored) {
            }
        }
    }

    public void remove() {
        BATTLE_MAP.remove(id);
    }

    public void startScoreboardRunnable() {
        final Battle battle = this;

        runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(StoaPlugin.getInst(), new BukkitRunnable() {
            @Override
            public void run() {
                // TODO: This loop could cause some lag
                for (Participant participant : participants) {
                    try {
                        BattleUtil.updateScoreboard(participant.getCharacter().getEntity(), battle);
                    } catch (Exception ignored) {
                    }
                }
            }
        }, 20, 20);
    }

    public void resetScoreboards() {
        // Cancel the runnable
        Bukkit.getScheduler().cancelTask(runnableId);

        // Clear the scoreboards
        for (Participant participant : participants) {
            try {
                participant.getCharacter().getEntity().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            } catch (Exception ignored) {
            }
        }
    }
}
