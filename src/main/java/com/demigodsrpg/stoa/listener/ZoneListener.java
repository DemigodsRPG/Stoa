package com.demigodsrpg.stoa.listener;

import com.demigodsrpg.stoa.event.StoaChatEvent;
import com.demigodsrpg.stoa.model.PlayerModel;
import com.demigodsrpg.stoa.util.PlayerUtil;
import com.demigodsrpg.stoa.util.ZoneUtil;
import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Set;

public class ZoneListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDemigodsChat(StoaChatEvent event) {
        Set<Player> modified = Sets.newHashSet(event.getRecipients());
        for (Player player : event.getRecipients())
            if (ZoneUtil.inNoStoaZone(player.getLocation())) modified.remove(player);
        if (modified.size() < 1) event.setCancelled(true);
        event.setRecipients(modified);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldSwitch(PlayerChangedWorldEvent event) {
        // Only continue if the player is a character
        Player player = event.getPlayer();
        PlayerModel playerSave = PlayerUtil.fromPlayer(player);

        // Leaving a disabled world
        if (ZoneUtil.isNoStoaWorld(event.getFrom()) && !ZoneUtil.isNoStoaWorld(player.getWorld())) {
            if (playerSave.getCharacter() != null) {
                playerSave.saveMortalInventory(player);
                playerSave.getCharacter().applyToPlayer(player);
            }
            player.sendMessage(ChatColor.YELLOW + "Demigods is enabled in this world.");
        }
        // Entering a disabled world
        else if (!ZoneUtil.isNoStoaWorld(event.getFrom()) && ZoneUtil.isNoStoaWorld(player.getWorld())) {
            if (playerSave.getCharacter() != null) playerSave.setToMortal();
            player.sendMessage(ChatColor.GRAY + "Demigods is disabled in this world.");
        }
    }
}
