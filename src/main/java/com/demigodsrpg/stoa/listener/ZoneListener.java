package com.demigodsrpg.stoa.listener;

import com.demigodsrpg.stoa.entity.player.StoaPlayer;
import com.demigodsrpg.stoa.event.StoaChatEvent;
import com.demigodsrpg.stoa.util.Zones;
import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Set;

public class ZoneListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDemigodsChat(StoaChatEvent event)
	{
		Set<Player> modified = Sets.newHashSet(event.getRecipients());
		for(Player player : event.getRecipients())
			if(Zones.inNoStoaZone(player.getLocation())) modified.remove(player);
		if(modified.size() < 1) event.setCancelled(true);
		event.setRecipients(modified);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldSwitch(PlayerChangedWorldEvent event)
	{
		// Only continue if the player is a character
		Player player = event.getPlayer();
		StoaPlayer playerSave = StoaPlayer.of(player);

		// Leaving a disabled world
		if(Zones.isNoStoaWorld(event.getFrom()) && !Zones.isNoStoaWorld(player.getWorld()))
		{
			if(playerSave.getCharacter() != null)
			{
				playerSave.saveMortalInventory(player);
				playerSave.getCharacter().applyToPlayer(player);
			}
			player.sendMessage(ChatColor.YELLOW + "Demigods is enabled in this world.");
		}
		// Entering a disabled world
		else if(!Zones.isNoStoaWorld(event.getFrom()) && Zones.isNoStoaWorld(player.getWorld()))
		{
			if(playerSave.getCharacter() != null) playerSave.setToMortal();
			player.sendMessage(ChatColor.GRAY + "Demigods is disabled in this world.");
		}
	}
}
