package com.demigodsrpg.stoa.item;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.demigodsrpg.stoa.data.DataAccess;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class StoaItemStack extends DataAccess<UUID, StoaItemStack>
{
	private UUID id;
	private ItemStack item;

	public StoaItemStack()
	{
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static StoaItemStack of(UUID id, ConfigurationSection conf)
	{
		StoaItemStack stoaItem = new StoaItemStack();
		stoaItem.id = id;
		if(conf.getValues(true) != null) stoaItem.item = ItemStack.deserialize(conf.getValues(true));
		return stoaItem;
	}

	@Override
	public Map<String, Object> serialize()
	{
		return item.serialize();
	}

	public void generateId()
	{
		id = UUID.randomUUID();
	}

	public UUID getId()
	{
		return id;
	}

	public void setItem(ItemStack item)
	{
		this.item = item;
	}

	/**
	 * Returns the DItemStack as an actual, usable ItemStack.
	 *
	 * @return ItemStack
	 */
	public ItemStack getBukkitItem()
	{
		return item;
	}

	private static final DataAccess<UUID, StoaItemStack> DATA_ACCESS = new StoaItemStack();

	public static StoaItemStack get(UUID id)
	{
		return DATA_ACCESS.getDirect(id);
	}

	public static StoaItemStack create(ItemStack item)
	{
		StoaItemStack trackedItem = new StoaItemStack();
		trackedItem.generateId();
		trackedItem.setItem(item);
		trackedItem.save();
		return trackedItem;
	}
}
