package com.demigodsrpg.stoa.event;

import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.util.CharacterUtil;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class BattleDeathEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    UUID battle;
    private boolean cancel = false;
    private String character, killer;

    public BattleDeathEvent(Battle battle, Participant character) {
        this.battle = battle.getId();
        this.character = character.getId();
    }

    public BattleDeathEvent(Battle battle, Participant character, Participant killer) {
        this.battle = battle.getId();
        this.character = character.getId();
        this.killer = killer.getId();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Battle getBattle() {
        if (Battle.BATTLE_MAP.containsKey(battle)) return Battle.BATTLE_MAP.get(battle);
        return null;
    }

    public CharacterModel getCharacter() {
        return CharacterUtil.fromId(character);
    }

    public boolean hasKiller() {
        return getKiller() != null;
    }

    public CharacterModel getKiller() {
        if (killer != null) return CharacterUtil.fromId(killer);
        return null;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
