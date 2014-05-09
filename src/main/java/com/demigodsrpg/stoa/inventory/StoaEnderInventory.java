package com.demigodsrpg.stoa.inventory;

import com.censoredsoftware.library.data.DataProvider;
import com.censoredsoftware.library.data.DefaultIdType;
import com.demigodsrpg.stoa.data.DataAccess;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.item.StoaItemStack;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoaEnderInventory extends DataAccess<UUID, StoaEnderInventory>
{
	private UUID id;
	private String[] items;

	public StoaEnderInventory()
	{
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static StoaEnderInventory of(UUID id, ConfigurationSection conf)
	{
		StoaEnderInventory inv = new StoaEnderInventory();
		inv.id = id;
		if(conf.getStringList("items") != null)
		{
			List<String> stringItems = conf.getStringList("items");
			inv.items = new String[stringItems.size()];
			for(int i = 0; i < stringItems.size(); i++)
				inv.items[i] = stringItems.get(i);
		}
		return inv;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = Maps.newHashMap();
		if(items != null) map.put("items", Lists.newArrayList(items));
		return map;
	}

	public void generateId()
	{
		id = UUID.randomUUID();
	}

	public void setItems(org.bukkit.inventory.Inventory inventory)
	{
		if(this.items == null) this.items = new String[26];
		for(int i = 0; i < 26; i++)
		{
			if(inventory.getItem(i) == null) this.items[i] = StoaItemStack.create(new ItemStack(Material.AIR)).getId().toString();
			else this.items[i] = StoaItemStack.create(inventory.getItem(i)).getId().toString();
		}
	}

	public UUID getId()
	{
		return this.id;
	}

	/**
	 * Applies this inventory to the given <code>player</code>.
	 *
	 * @param player the player for whom apply the inventory.
	 */
	public void setToPlayer(Player player)
	{
		// Define the inventory
		Inventory inventory = player.getEnderChest();

		// Clear it all first
		inventory.clear();

		if(this.items != null)
		{
			// Set items
			for(int i = 0; i < 26; i++)
			{
				if(this.items[i] != null)
				{
					ItemStack itemStack = StoaItemStack.get(UUID.fromString(this.items[i])).getBukkitItem();
					if(itemStack != null) inventory.setItem(i, StoaItemStack.get(UUID.fromString(this.items[i])).getBukkitItem());
				}
			}
		}

		// Delete
		remove();
	}

	private static final DataAccess<UUID, StoaEnderInventory> DATA_ACCESS = new StoaEnderInventory();

	public static StoaEnderInventory get(UUID id)
	{
		return DATA_ACCESS.getDirect(id);
	}

	public static StoaEnderInventory create(StoaCharacter character)
	{
		Inventory inventory = character.getBukkitOfflinePlayer().getPlayer().getEnderChest();
		StoaEnderInventory enderInventory = new StoaEnderInventory();
		enderInventory.generateId();
		enderInventory.setItems(inventory);
		enderInventory.save();
		return enderInventory;
	}

	public static StoaEnderInventory createEmpty()
	{
		StoaEnderInventory enderInventory = new StoaEnderInventory();
		enderInventory.generateId();
		enderInventory.save();
		return enderInventory;
	}
}
