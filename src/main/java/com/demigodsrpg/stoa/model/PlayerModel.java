package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.util.*;
import com.iciql.Db;
import com.iciql.Iciql;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Iciql.IQTable(name = "dg_players")
public class PlayerModel implements Participant {
    // -- MODEL META -- //
    @Iciql.IQColumn(name = "id", primaryKey = true)
    public String mojangAccount;
    // -- DATA -- //
    @Iciql.IQColumn
    public String playerName;
    @Iciql.IQColumn
    public String mortalName;
    @Iciql.IQColumn
    public String mortalListName;
    @Iciql.IQColumn
    public Boolean canPvp;
    @Iciql.IQColumn
    public Integer characterSlots;
    @Iciql.IQColumn
    public Timestamp lastLoginTime;
    @Iciql.IQColumn
    public Timestamp lastLogoutTime;
    // -- FOREIGN DATA -- //
    @Iciql.IQColumn
    public String currentCharacterId;
    @Iciql.IQColumn
    public String mortalInventoryId;
    @Iciql.IQColumn
    public String mortalEnderChestId;
    // -- TRANSIENT -- //
    public transient ChatRecorder chatRecorder;

    // -- DEFAULT CONSTRUCTOR -- //
    public PlayerModel() {
    }

    // -- PRACTICAL CONSTRUCTOR -- //
    public PlayerModel(Player player) {
        // Set ID
        mojangAccount = player.getUniqueId().toString();

        // Defaults
        playerName = player.getName();
        mortalName = player.getDisplayName();
        mortalListName = player.getPlayerListName();
        canPvp = false;
        characterSlots = StoaPlugin.config().getInt("character.default_character_slots");
        lastLoginTime = new Timestamp(System.currentTimeMillis());
    }

    public String getMojangAccount() {
        return mojangAccount;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getMortalName() {
        return mortalName;
    }

    public void setMortalName(String mortalName) {
        this.mortalName = mortalName;
    }

    public String getMortalListName() {
        return mortalListName;
    }

    public void setMortalListName(String mortalListName) {
        this.mortalListName = mortalListName;
    }

    public Boolean getCanPvp() {
        return canPvp;
    }

    public void setCanPvp(Boolean canPvp) {
        this.canPvp = canPvp;
    }

    public Integer getCharacterSlots() {
        return characterSlots;
    }

    public void setCharacterSlots(Integer characterSlots) {
        this.characterSlots = characterSlots;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Timestamp getLastLogoutTime() {
        return lastLogoutTime;
    }

    public void setLastLogoutTime(Timestamp lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    public String getCurrentCharacterId() {
        return currentCharacterId;
    }

    public void setCurrentCharacterId(String currentCharacterId) {
        this.currentCharacterId = currentCharacterId;
    }

    public String getMortalInventoryId() {
        return mortalInventoryId;
    }

    public void setMortalInventoryId(String mortalInventoryId) {
        this.mortalInventoryId = mortalInventoryId;
    }

    public String getMortalEnderChestId() {
        return mortalEnderChestId;
    }

    public void setMortalEnderChestId(String mortalEnderChestId) {
        this.mortalEnderChestId = mortalEnderChestId;
    }

    public void resetCurrent() {
        currentCharacterId = null;

        if (getEntity() != null) {
            Player player = getEntity();
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.setMaxHealth(20.0);
        }
    }

    public void updateCanPvp() {
        if (getEntity() == null) return;

        // Define variables
        final Player player = getEntity();
        final boolean inNoPvpZone = ZoneUtil.inNoPvpZone(player.getLocation());

        if (getCharacter() != null && BattleUtil.isInBattle(getCharacter())) return;

        if (!canPvp() && !inNoPvpZone) {
            setCanPvp(true);
            player.sendMessage(ChatColor.GRAY + English.UNSAFE_FROM_PVP.getLine());
        } else if (!inNoPvpZone) {
            setCanPvp(true);
            ServerDataUtil.remove(player.getName(), "pvp_cooldown");
        } else if (canPvp() && !ServerDataUtil.exists(player.getName(), "pvp_cooldown")) {
            int delay = StoaPlugin.config().getInt("zones.pvp_area_delay_time");
            ServerDataUtil.put(true, delay, TimeUnit.SECONDS, player.getName(), "pvp_cooldown");

            Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable() {
                @Override
                public void run() {
                    if (ZoneUtil.inNoPvpZone(player.getLocation())) {
                        if (getCharacter() != null && BattleUtil.isInBattle(getCharacter())) return;
                        setCanPvp(false);
                        player.sendMessage(ChatColor.GRAY + English.SAFE_FROM_PVP.getLine());
                    }
                }
            }, (delay * 20));
        }
    }

    public void setToMortal() {
        Player player = getEntity();
        saveCurrentCharacter();
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect potion : player.getActivePotionEffects())
            player.removePotionEffect(potion.getType());
        player.setDisplayName(mortalName);
        player.setPlayerListName(mortalListName);
        setMortalName(null);
        setMortalListName(null);
        applyMortalInventory();
    }

    public void saveMortalInventory(Player player) {
        // Player inventory
        Db db = StoaServer.openDb();
        PlayerInventoryModel mortalInventory = ItemUtil.playerInvFromOwnerId("mortal:" + mojangAccount);
        PlayerInventory inventory = player.getInventory();
        mortalInventory.setArmor(inventory.getArmorContents());
        mortalInventory.setContents(inventory.getContents());
        db.update(mortalInventory);

        // Enderchest
        EnderChestInventoryModel enderInventory = ItemUtil.enderInvFromOwnerId("mortal:" + mojangAccount);
        Inventory enderChest = player.getEnderChest();
        enderInventory.setContents(enderChest.getContents());
        db.update(enderInventory);

        db.close();
    }

    public void saveCurrentCharacter() {
        // Update the current character
        final Player player = getEntity();
        final CharacterModel character = getCharacter();

        if (character != null) {
            // Set to inactive and update previous
            character.setActive(false);

            // Set the values
            character.setHealth(player.getHealth() >= character.getDeity().getMaxHealth() ? character.getDeity().getMaxHealth() : player.getHealth());
            character.setHunger(player.getFoodLevel());
            character.setLevel(player.getLevel());
            character.setExperience(player.getExp());
            character.setLastLocation(player.getLocation());
            Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new Runnable() {
                @Override
                public void run() {
                    if (player.getBedSpawnLocation() != null) character.setBedSpawn(player.getBedSpawnLocation());
                }
            });
            character.setGameMode(player.getGameMode());
            // TODO character.setPotionEffects(player.getActivePotionEffects());
            character.saveInventory();

            // Disown pets
            TameableUtil.disownPets(character.name);

            // Update the db
            Db db = StoaServer.openDb();
            db.update(character);
            db.close();
        }
    }

    public void switchCharacter(final CharacterModel newChar) {
        final Player player = getEntity();

        if (!newChar.playerId.equals(mojangAccount)) {
            player.sendMessage(ChatColor.RED + "You can't do that.");
        } else {

            // Save the current character
            saveCurrentCharacter();

            // Set new character to active and other info
            currentCharacterId = newChar.uuid;

            // Apply the new character
            newChar.applyToPlayer(player);

            // Teleport them
            try {
                player.teleport(newChar.getLastLocation());
            } catch (Exception e) {
                MessageUtil.warning("There was a problem while teleporting a player to their character.");
            }

            // Save instances
            Db db = StoaServer.openDb();
            db.update(this);
            db.update(newChar);
            db.close();
        }
    }

    public List<CharacterModel> getUsableCharacters() {
        CharacterModel alias = new CharacterModel();
        Db db = StoaServer.openDb();
        try {
            return db.from(alias).where(alias.usable).is(true).and(alias.playerId).is(mojangAccount).select();
        } finally {
            db.close();
        }
    }

    public PlayerInventoryModel getMortalInventory() {
        return ItemUtil.playerInvFromOwnerId("mortal;" + mojangAccount);
    }

    public EnderChestInventoryModel getMortalEnderInventory() {
        return ItemUtil.enderInvFromOwnerId("mortal;" + mojangAccount);
    }

    public void applyMortalInventory() {
        Db db = StoaServer.openDb();

        PlayerInventoryModel inventoryModel = getMortalInventory();
        inventoryModel.setToPlayer(getEntity());
        inventoryModel.setArmor(null);
        inventoryModel.setContents(null);
        db.update(inventoryModel);

        EnderChestInventoryModel enderModel = getMortalEnderInventory();
        enderModel.setToPlayer(getEntity());
        enderModel.setContents(null);
        db.update(enderModel);

        db.close();
    }

    public boolean canMakeCharacter() {
        return getUsableCharacters().size() < getCharacterSlots();
    }

    public boolean canUseCurrent() {
        if (getCharacter() == null || !getCharacter().getUsable()) {
            if (getOfflinePlayer().isOnline()) {
                getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Your current character was unable to init!");
                getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Please contact the server administrator immediately.");
            }
            return false;
        }
        return true;
    }

    public void remove() {
        // First we need to kick the player if they're online
        if (getOfflinePlayer().isOnline()) {
            getEntity().kickPlayer(ChatColor.RED + "Your player save has been cleared.");
        }

        // Remove characters
        for (CharacterModel character : getCharacters()) {
            character.remove();
        }

        // Now we clear the save itself
        Db db = StoaServer.openDb();
        db.delete(this);
        db.close();
    }

    /* public void sendNotification(Notification notification) {
        // TODO if (getCharacter() != null) NotificationUtil.sendNotification(getCharacter(), notification);
    } */

    /**
     * Starts recording recording the <code>player</code>'s chat.
     */
    public void startRecording() {
        chatRecorder = ChatRecorder.Util.startRecording(getEntity());
    }

    /**
     * Stops recording and sends all messages that have been recorded thus far to the player.
     *
     * @param display if true, the chat will be sent to the player
     */
    public List<String> stopRecording(boolean display) {
        Player player = getEntity();
        // Handle recorded chat
        if (chatRecorder != null && chatRecorder.isRecording()) {
            // Send held back chat
            List<String> messages = chatRecorder.stop();
            if (messages.size() > 0 && display) {
                player.sendMessage(" ");
                if (messages.size() == 1) {
                    player.sendMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + English.HELD_BACK_MESSAGE.getLine());
                } else {
                    player.sendMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + English.HELD_BACK_MESSAGES.getLine().replace("{size}", "" + messages.size()));
                }
                for (String message : messages)
                    player.sendMessage(message);
            }

            return messages;
        }
        return null;
    }


    public boolean hasCharName(String name) {
        CharacterModel alias = new CharacterModel();
        Db db = StoaServer.openDb();

        try {
            return !db.from(alias).where(alias.playerId).is(mojangAccount).and(alias.name).is(name).select().isEmpty();
        } finally {
            db.close();
        }
    }

    public List<CharacterModel> getCharacters() {
        CharacterModel alias = new CharacterModel();
        Db db = StoaServer.openDb();

        try {
            return db.from(alias).where(alias.playerId).is(mojangAccount).select();
        } finally {
            db.close();
        }
    }

    @Override
    public CharacterModel getCharacter() {
        if (!hasCharacter()) return null;
        return CharacterUtil.fromId(currentCharacterId);
    }

    @Override
    public boolean hasCharacter() {
        return currentCharacterId != null;
    }

    @Override
    public String getId() {
        return mortalInventoryId;
    }

    @Override
    public boolean canPvp() {
        return canPvp;
    }

    @Override
    public Location getCurrentLocation() {
        if (getEntity() == null) return null;
        return getEntity().getLocation();
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(mojangAccount));
    }

    @Override
    public Player getEntity() {
        return Bukkit.getPlayer(mojangAccount);
    }
}
