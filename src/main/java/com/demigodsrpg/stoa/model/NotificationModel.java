package com.demigodsrpg.stoa.model;

import com.google.common.base.Optional;
import com.iciql.Iciql;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Iciql.IQTable(name = "dg_notifications")
public class NotificationModel {
    // -- DEFAULT CONSTRUCTOR -- //
    public NotificationModel() {
    }

    // -- PRACTICAL CONSTRUCTOR -- //
    public NotificationModel(Alert alert, SenderType type, CharacterModel receiver, String name, String message, Optional<String> sender, Optional<Long> time, Optional<TimeUnit> unit) {
        // Set data
        this.alert = alert;
        this.type = type;
        this.name = name;
        this.message = message;
        if (time.isPresent() && unit.isPresent()) {
            expiration = new Timestamp(unit.get().toMillis(time.get()));
        }

        // Foreign keys
        receiverId = receiver.uuid;
        if (sender.isPresent()) {
            senderId = sender.get();
        }
    }

    // -- MODEL META -- //
    @Iciql.IQColumn(primaryKey = true)
    public String id = UUID.randomUUID().toString();

    // -- ENUMS -- //
    @Iciql.IQEnum
    public enum SenderType {
        SERVER, QUEST, CHARACTER
    }

    @Iciql.IQEnum
    public enum Alert {
        GOOD, NEUTRAL, BAD
    }

    // -- DATA -- //
    @Iciql.IQColumn
    public SenderType type = SenderType.SERVER;
    @Iciql.IQColumn
    public Alert alert = Alert.NEUTRAL;
    @Iciql.IQColumn
    public String name;
    @Iciql.IQColumn
    public String message;
    @Iciql.IQColumn
    public Timestamp expiration;

    // -- FOREIGN DATA -- //
    @Iciql.IQColumn
    public String senderId;
    @Iciql.IQColumn
    public String receiverId;
}
