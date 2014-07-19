package com.demigodsrpg.stoa.model;

import com.censoredsoftware.library.messages.CommonSymbol;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class Battle {
    public static final ConcurrentMap<Battle, Long> BATTLE_MAP = new ConcurrentHashMap<>();

    private transient Boolean active;

    private transient Location startLocation;
    private transient String startedBy;
    private transient Long startTime;
    private transient Long deleteTime;
    private transient Set<String> participants;
    private transient Integer killCounter;
    private transient Map<String, Integer> kills;
    private transient Map<String, Integer> deaths;

    private transient Integer runnableId;

    public Battle(Participant participant) {
        active = true;

        startLocation = participant.getCurrentLocation();
        startedBy = participant.getCharacter().getId();
        startTime = System.currentTimeMillis();
        participants = new HashSet<>();
        killCounter = 0;
        kills = new HashMap<>();
        deaths = new HashMap<>();

        participants.add(startedBy);
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

    public String getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(String startedBy) {
        this.startedBy = startedBy;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }

    public void setKillCounter(Integer killCounter) {
        this.killCounter = killCounter;
    }

    public Map<String, Integer> getKills() {
        return kills;
    }

    public void setKills(Map<String, Integer> kills) {
        this.kills = kills;
    }

    public Map<String, Integer> getDeaths() {
        return deaths;
    }

    public void setDeaths(Map<String, Integer> deaths) {
        this.deaths = deaths;
    }

    public Integer getRunnableId() {
        return runnableId;
    }

    public void setRunnableId(Integer runnableId) {
        this.runnableId = runnableId;
    }

    public Map<String, Integer> getScores() {
        Map<String, Integer> score = Maps.newHashMap();
        for (Map.Entry<String, Integer> entry : kills.entrySet()) {
            if (!getParticipants().contains(entry.getKey())) continue;
            score.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : deaths.entrySet()) {
            int base = 0;
            if (score.containsKey(entry.getKey())) base = score.get(entry.getKey());
            score.put(entry.getKey(), base - entry.getValue());
        }
        return score;
    }

    public int getScore(final Alliance alliance) {
        Map<String, Integer> score = Maps.newHashMap();
        for (Map.Entry<String, Integer> entry : kills.entrySet()) {
            if (!getParticipants().contains((entry.getKey()))) continue;
            score.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : deaths.entrySet()) {
            int base = 0;
            if (score.containsKey(entry.getKey())) base = score.get(entry.getKey());
            score.put(entry.getKey(), base - entry.getValue());
        }
        int sum = 0;
        for (int i : Collections2.transform(Collections2.filter(score.entrySet(), new Predicate<Map.Entry<String, Integer>>() {
            @Override
            public boolean apply(Map.Entry<String, Integer> entry) {
                return CharacterUtil.fromId(entry.getKey()).getAlliance().getName().equalsIgnoreCase(alliance.getName());
            }
        }), new Function<Map.Entry<String, Integer>, Integer>() {
            @Override
            public Integer apply(Map.Entry<String, Integer> entry) {
                return entry.getValue();
            }
        }))
            sum += i;
        return sum;
    }

    public Collection<CharacterModel> getMVPs() {
        final int max = Collections.max(getScores().values());
        return Collections2.transform(Collections2.filter(getScores().entrySet(), new Predicate<Map.Entry<String, Integer>>() {
            @Override
            public boolean apply(Map.Entry<String, Integer> entry) {
                return entry.getValue() == max;
            }
        }), new Function<Map.Entry<String, Integer>, CharacterModel>() {
            @Override
            public CharacterModel apply(Map.Entry<String, Integer> entry) {
                return CharacterUtil.fromId(entry.getKey());
            }
        });
    }

    public int getKillCounter() {
        return this.killCounter;
    }

    public void end() // TODO Make this specify that it was a pet that won/lost a duel
    {
        for (String stringId : participants)
            ServerDataUtil.put(true, 1, TimeUnit.MINUTES, stringId, "just_finished_battle");

        Map<String, Integer> scores = getScores();
        List<String> participants = Lists.newArrayList(scores.keySet());
        if (participants.size() == 2) {
            if (scores.get(participants.get(0)).equals(scores.get(participants.get(1)))) {
                CharacterModel one = CharacterUtil.fromId(participants.get(0));
                CharacterModel two = CharacterUtil.fromId(participants.get(1));
                MessageUtil.broadcast(one.getDeity().getColor() + one.getName() + ChatColor.GRAY + " and " + two.getDeity().getColor() + two.getName() + ChatColor.GRAY + " just tied in a duel.");
            } else {
                int winnerIndex = scores.get(participants.get(0)) > scores.get(participants.get(1)) ? 0 : 1;
                CharacterModel winner = CharacterUtil.fromId(participants.get(winnerIndex));
                CharacterModel loser = CharacterUtil.fromId(participants.get(winnerIndex == 0 ? 1 : 0));
                MessageUtil.broadcast(winner.getDeity().getColor() + winner.getName() + ChatColor.GRAY + " just won in a duel against " + loser.getDeity().getColor() + loser.getName() + ChatColor.GRAY + ".");
            }
        } else if (participants.size() > 2) {
            Alliance winningAlliance = null;
            int winningScore = 0;
            Collection<CharacterModel> MVPs = getMVPs();
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
                for (CharacterModel mvp : MVPs)
                    MessageUtil.broadcast(" " + ChatColor.DARK_GRAY + CommonSymbol.RIGHTWARD_ARROW + " " + mvp.getDeity().getColor() + mvp.getName() + ChatColor.GRAY + " / " + ChatColor.YELLOW + "Kills" + ChatColor.GRAY + ": " + kills.get(mvp.getId()) + " / " + ChatColor.YELLOW + "Deaths" + ChatColor.GRAY + ": " + deaths.get(mvp.getId()));
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
        for (String participant : getParticipants())
            set.add(CharacterUtil.fromId(participant).getAlliance());
        return set;
    }

    public void sendMessage(String message) {
        for (String stringId : participants) {
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
                for (String stringId : participants) {
                    OfflinePlayer offlinePlayer = StoaCharacter.get(UUID.fromString(stringId)).getBukkitOfflinePlayer();
                    if (offlinePlayer.isOnline()) BattleUtil.updateScoreboard(offlinePlayer.getPlayer(), battle);
                }
            }
        }, 20, 20);
    }

    public void resetScoreboards() {
        // Cancel the runnable
        Bukkit.getScheduler().cancelTask(runnableId);

        // Clear the scoreboards
        for (String stringId : participants) {
            OfflinePlayer offlinePlayer = StoaCharacter.get(UUID.fromString(stringId)).getBukkitOfflinePlayer();
            if (offlinePlayer.isOnline())
                offlinePlayer.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

}
