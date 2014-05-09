package com.demigodsrpg.stoa.data;

import com.censoredsoftware.library.data.*;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.entity.player.StoaPlayer;
import com.demigodsrpg.stoa.inventory.StoaEnderInventory;
import com.demigodsrpg.stoa.inventory.StoaPlayerInventory;
import com.demigodsrpg.stoa.item.StoaItemStack;
import com.demigodsrpg.stoa.tribute.TributeData;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

/**
 * Meta data for each data type.
 */
@SuppressWarnings("unchecked")
public enum DataType implements com.censoredsoftware.library.data.DataType
{
	/**
	 * DemigodsPlayer.
	 */
	PLAYER(StoaPlayer.class, DefaultIdType.UUID, "pl"),
	/**
	 * DemigodsCharacter.
	 */
	CHARACTER(StoaCharacter.class, DefaultIdType.UUID, "ch"),
	/**
	 * DemigodsCharacterMeta.
	 */
	CHARACTER_META(com.demigodsrpg.stoa.entity.player.attribute.StoaCharacterMeta.class, DefaultIdType.UUID, "chmt"),
	/**
	 * DemigodsPlayerInventory.
	 */
	PLAYER_INVENTORY(StoaPlayerInventory.class, DefaultIdType.UUID, "plin"),
	/**
	 * DemigodsEnderInventory.
	 */
	ENDER_INVENTORY(StoaEnderInventory.class, DefaultIdType.UUID, "enin"),
	/**
	 * DemigodsTameable.
	 */
	TAMABLE(StoaTameable.class, DefaultIdType.UUID, "tm"),
	/**
	 * Death.
	 */
	DEATH(com.demigodsrpg.stoa.entity.player.attribute.Death.class, DefaultIdType.UUID, "d"),
	/**
	 * Skill.
	 */
	SKILL(com.demigodsrpg.stoa.entity.player.attribute.Skill.class, DefaultIdType.UUID, "sk"),
	/**
	 * Notification.
	 */
	NOTIFICATION(com.demigodsrpg.stoa.entity.player.attribute.Notification.class, DefaultIdType.UUID, "n"),
	/**
	 * DemigodsItemStack.
	 */
	ITEM_STACK(StoaItemStack.class, DefaultIdType.UUID, "it"),
	/**
	 * DemigodsPotionEffect.
	 */
	POTION_EFFECT(com.demigodsrpg.stoa.entity.player.attribute.StoaPotionEffect.class, DefaultIdType.UUID, "po"),
	/**
	 * Battle.
	 */
	BATTLE(Battle.class, DefaultIdType.UUID, "b"),
	/**
	 * TributeData.class
	 */
	TRIBUTE(TributeData.class, DefaultIdType.UUID, "tr"),
	/**
	 * ServerData.
	 */
	SERVER(ServerData.class, DefaultIdType.UUID, "srv"),
	/**
	 * Returned when no valid type can be found.
	 */
	INVALID(Invalid.class, DefaultIdType.VOID, "IF_YOU_SEE_THIS_PLEASE_TELL_US_ON_THE_SITE_YOU_DOWNLOADED_DEMIGODS_FROM");

	private Class clazz;
	private IdType idType;
	private String abbr;

	/**
	 * Meta data for a data type.
	 *
	 * @param clazz  The object class that holds the data.
	 * @param idType The id type this data type uses.
	 * @param abbr   The abbreviation for use in certain data managers.
	 */
	private <V extends DataSerializable<?>> DataType(Class<V> clazz, DefaultIdType idType, String abbr)
	{
		this.clazz = clazz;
		this.idType = idType;
		this.abbr = abbr;
	}

	@Override
	public String toString()
	{
		return name();
	}

	public Class getDataClass()
	{
		return clazz;
	}

	public IdType getIdType()
	{
		return idType;
	}

	public String getAbbreviation()
	{
		return abbr;
	}

	public static String[] names()
	{
		String[] names = new String[values().length];
		for(int i = 0; i < values().length; i++)
			names[i] = values()[i].name();
		return names;
	}

	public static <V extends DataAccess> Class<V>[] classes()
	{
		Class<V>[] classes = new Class[values().length];
		for(int i = 0; i < values().length; i++)
			classes[i] = values()[i].clazz;
		return classes;
	}

	public static <V extends DataAccess> DataType typeFromClass(final Class<V> clazz)
	{
		return Iterables.find(Arrays.asList(values()), new Predicate<DataType>()
		{
			@Override
			public boolean apply(DataType dataType)
			{
				return clazz.equals(dataType.clazz);
			}
		}, INVALID);
	}
}
