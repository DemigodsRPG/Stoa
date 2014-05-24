package com.demigodsrpg.stoa.controller;

import com.censoredsoftware.shaded.com.iciql.Db;
import com.demigodsrpg.stoa.model.Model;
import com.demigodsrpg.stoa.util.Configs;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class Controller<T extends Model>
{
	protected Controller()
	{
	}

	public abstract Controller<T> control(String modelId);

	protected Controller<T> control(final String modelId, T type)
	{
		open();
		T found = Iterables.find(DB.from(type).select(), new Predicate<T>()
		{
			@Override
			public boolean apply(T model)
			{
				return modelId.equals(model.id());
			}
		}, null);
		if(found != null) return control(found);
		throw new NullPointerException("Cannot find data model from id.");
	}

	public Controller<T> control(T model)
	{
		this.model = model;
		return this;
	}

	public Controller<T> open()
	{
		DB = openDb();
		return this;
	}

	public Controller<T> close()
	{
		if(DB != null)
		{
			DB.close();
			DB = null;
		}
		return this;
	}

	public Controller<T> update()
	{
		DB.update(model);
		return this;
	}

	public abstract Controller<T> refresh();

	public void relinquish()
	{
		close();
		model = null;
	}

	public T getModel()
	{
		return model;
	}

	public boolean isOpen()
	{
		return DB != null;
	}

	protected T model;
	protected Db DB;

	protected static Db openDb()
	{
		return Db.open("jdbc:" + Configs.getSettingString("db.type") + "://" + Configs.getSettingString("db.host") + ":" + Configs.getSettingString("db.port") + "/" + Configs.getSettingString("db.name"), Configs.getSettingString("db.user"), Configs.getSettingString("db.pass"));
	}
}
