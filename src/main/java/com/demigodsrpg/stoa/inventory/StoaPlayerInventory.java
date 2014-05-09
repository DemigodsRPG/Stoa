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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoaPlayerInventory extends DataAccess<UUID, StoaPlayerInventory>
{
	private UUID id;
	private UUID helmet;
	private UUID chestplate;
	private UUID leggings;
	private UUID boots;
	private String[] items;

	public StoaPlayerInventory()
	{
	}

	@DataProvider(idType = DefaultIdType.UUID)
	public static StoaPlayerInventory of(UUID id, ConfigurationSection conf)
	{
		StoaPlayerInventory inv = new StoaPlayerInventory();
		inv.id = id;
		if(conf.getString("helmet") != null) inv.helmet = UUID.fromString(conf.getString("helmet"));
		if(conf.getString("chestplate") != null) inv.chestplate = UUID.fromString(conf.getString("chestplate"));
		if(conf.getString("leggings") != null) inv.leggings = UUID.fromString(conf.getString("leggings"));
		if(conf.getString("boots") != null) inv.boots = UUID.fromString(conf.getString("boots"));
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
		if(helmet != null) map.put("helmet", helmet.toString());
		if(chestplate != null) map.put("chestplate", chestplate.toString());
		if(leggings != null) map.put("leggings", leggings.toString());
		if(boots != null) map.put("boots", boots.toString());
		if(items != null) map.put("items", Lists.newArrayList(items));
		return map;
	}

	public void generateId()
	{
		id = UUID.randomUUID();
	}

	public void setHelmet(ItemStack helmet)
	{
		this.helmet = StoaItemStack.create(helmet).getId();
	}

	public void setChestplate(ItemStack chestplate)
	{
		this.chestplate = StoaItemStack.create(chestplate).getId();
	}

	public void setLeggings(ItemStack leggings)
	{
		this.leggings = StoaItemStack.create(leggings).getId();
	}

	public void setBoots(ItemStack boots)
	{
		this.boots = StoaItemStack.create(boots).getId();
	}

	public void setItems(org.bukkit.inventory.Inventory inventory)
	{
		if(this.items == null) this.items = new String[36];
		for(int i = 0; i < 35; i++)
		{
			if(inventory.getItem(i) == null) this.items[i] = StoaItemStack.create(new ItemStack(Material.AIR)).getId().toString();
			else this.items[i] = StoaItemStack.create(inventory.getItem(i)).getId().toString();
		}
	}

	public UUID getId()
	{
		return this.id;
	}

	public ItemStack getHelmet()
	{
		if(this.helmet == null) return null;
		StoaItemStack item = StoaItemStack.get(this.helmet);
		if(item != null) return item.getBukkitItem();
		return null;
	}

	public ItemStack getChestplate()
	{
		if(this.chestplate == null) return null;
		StoaItemStack item = StoaItemStack.get(this.chestplate);
		if(item != null) return item.getBukkitItem();
		return null;
	}

	public ItemStack getLeggings()
	{
		if(this.leggings == null) return null;
		StoaItemStack item = StoaItemStack.get(this.leggings);
		if(item != null) return item.getBukkitItem();
		return null;
	}

	public ItemStack getBoots()
	{
		if(this.boots == null) return null;
		StoaItemStack item = StoaItemStack.get(this.boots);
		if(item != null) return item.getBukkitItem();
		return null;
	}

	/**
	 * Applies this inventory to the given <code>player</code>.
	 *
	 * @param player the player for whom apply the inventory.
	 */
	public void setToPlayer(Player player)
	{
		// Define the inventory
		PlayerInventory inventory = player.getInventory();

		// Clear it all first
		inventory.clear();
		inventory.setHelmet(new ItemStack(Material.AIR));
		inventory.setChestplate(new ItemStack(Material.AIR));
		inventory.setLeggings(new ItemStack(Material.AIR));
		inventory.setBoots(new ItemStack(Material.AIR));

		// Set the armor contents
		if(getHelmet() != null) inventory.setHelmet(getHelmet());
		if(getChestplate() != null) inventory.setChestplate(getChestplate());
		if(getLeggings() != null) inventory.setLeggings(getLeggings());
		if(getBoots() != null) inventory.setBoots(getBoots());

		if(this.items != null)
		{
			// Set items
			for(int i = 0; i < 35; i++)
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

	private static final DataAccess<UUID, StoaPlayerInventory> DATA_ACCESS = new StoaPlayerInventory();

	public static StoaPlayerInventory get(UUID id)
	{
		return DATA_ACCESS.getDirect(id);
	}

	public static StoaPlayerInventory create(StoaCharacter character)
	{
		PlayerInventory inventory = character.getBukkitOfflinePlayer().getPlayer().getInventory();
		StoaPlayerInventory charInventory = new StoaPlayerInventory();
		charInventory.generateId();
		if(inventory.getHelmet() != null) charInventory.setHelmet(inventory.getHelmet());
		if(inventory.getChestplate() != null) charInventory.setChestplate(inventory.getChestplate());
		if(inventory.getLeggings() != null) charInventory.setLeggings(inventory.getLeggings());
		if(inventory.getBoots() != null) charInventory.setBoots(inventory.getBoots());
		charInventory.setItems(inventory);
		charInventory.save();
		return charInventory;
	}

	public static StoaPlayerInventory createEmpty()
	{
		StoaPlayerInventory charInventory = new StoaPlayerInventory();
		charInventory.generateId();
		charInventory.setHelmet(new ItemStack(Material.AIR));
		charInventory.setChestplate(new ItemStack(Material.AIR));
		charInventory.setLeggings(new ItemStack(Material.AIR));
		charInventory.setBoots(new ItemStack(Material.AIR));
		charInventory.save();
		return charInventory;
	}
}
