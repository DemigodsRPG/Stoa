package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.battle.Participant;
import com.iciql.Iciql;

import java.sql.Timestamp;

@Iciql.IQTable(name = "dg_deaths")
public class DeathModel {
    // -- DEFAULT CONSTRUCTOR -- //
    public DeathModel() {
    }

    // -- PRACTICAL CONSTRUCTOR -- //
    public DeathModel(Participant victim) {
        // Set data and foreign keys
        deathTime = new Timestamp(System.currentTimeMillis());
        victimId = victim.getCharacter().getId();
    }

    public DeathModel(Participant victim, Participant killer) {
        this(victim);
        killerId = killer.getCharacter().getId();
    }

    // -- MODEL META -- //
    @Iciql.IQColumn(primaryKey = true, autoIncrement = true)
    public Long id;

    // -- DATA -- //
    @Iciql.IQColumn
    public Timestamp deathTime;

    // -- FOREIGN DATA -- //
    @Iciql.IQColumn
    public String victimId;
    @Iciql.IQColumn
    public String killerId;

    // TODO
}
