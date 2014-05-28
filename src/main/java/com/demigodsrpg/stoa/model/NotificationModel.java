package com.demigodsrpg.stoa.model;

import com.censoredsoftware.shaded.com.iciql.Iciql;
import com.google.common.base.Optional;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Iciql.IQTable(name = "notifications")
public class NotificationModel implements Model
{
	// -- DEFAULT CONSTRUCTOR -- //
	public NotificationModel()
	{
	}

	// -- PRACTICAL STATIC CONSTRUCTOR -- //
	public static NotificationModel from(Alert alert, SenderType type, CharacterModel receiver, String name, String message, Optional<String> sender, Optional<Long> time, Optional<TimeUnit> unit)
	{
		NotificationModel model = new NotificationModel();

		// Set data
		model.alert = alert;
		model.type = type;
		model.name = name;
		model.message = message;
		if(time.isPresent() && unit.isPresent()) model.expiration = new Timestamp(unit.get().toMillis(time.get()));

		// Foreign keys
		model.receiverId = receiver.uuid;
		if(sender.isPresent()) model.senderId = sender.get();

		return model;
	}

	// -- MODEL META -- //
	@Iciql.IQColumn(primaryKey = true)
	public String id = UUID.randomUUID().toString();

	// -- ENUMS -- //
	@Iciql.IQEnum
	public enum SenderType
	{
		SERVER, QUEST, CHARACTER
	}

	@Iciql.IQEnum
	public enum Alert
	{
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

	// -- INTERFACE METHODS -- //
	@Override
	public String id()
	{
		return id;
	}

	@Override
	public String modelName()
	{
		return "NOTIFICATION";
	}
}
