package com.demigodsrpg.stoa.model;

import com.censoredsoftware.library.data.ServerData;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.battle.Battle;
import com.demigodsrpg.stoa.battle.Participant;
import com.demigodsrpg.stoa.controller.CharacterController;
import com.demigodsrpg.stoa.entity.StoaTameable;
import com.demigodsrpg.stoa.entity.player.StoaCharacter;
import com.demigodsrpg.stoa.entity.player.attribute.Notification;
import com.demigodsrpg.stoa.inventory.StoaEnderInventory;
import com.demigodsrpg.stoa.inventory.StoaPlayerInventory;
import com.demigodsrpg.stoa.language.English;
import com.demigodsrpg.stoa.util.ChatRecorder;
import com.demigodsrpg.stoa.util.MessageUtil;
import com.demigodsrpg.stoa.util.ZoneUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.iciql.Db;
import com.iciql.Iciql;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Iciql.IQTable(name = "dg_players")
public class PlayerModel implements Participant {
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
        lastLoginTime = Timestamp.from(Instant.now());

        // Don't set character, inventory, or last logout time data yet
    }

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

        if (getCharacter() != null && Battle.isInBattle(getCharacter())) return;

        if (!canPvp() && !inNoPvpZone) {
            setCanPvp(true);
            player.sendMessage(ChatColor.GRAY + English.UNSAFE_FROM_PVP.getLine());
        } else if (!inNoPvpZone) {
            setCanPvp(true);
            ServerData.remove(DataManager.DATA_MANAGER, player.getName(), "pvp_cooldown");
        } else if (canPvp() && !ServerData.exists(DataManager.DATA_MANAGER, player.getName(), "pvp_cooldown")) {
            int delay = Configs.getSettingInt("zones.pvp_area_delay_time");
            ServerData.put(DataManager.DATA_MANAGER, player.getName(), "pvp_cooldown", true, delay, TimeUnit.SECONDS);

            Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable() {
                @Override
                public void run() {
                    if (ZoneUtil.inNoPvpZone(player.getLocation())) {
                        if (getCharacter() != null && Battle.isInBattle(getCharacter())) return;
                        setCanPvp(false);
                        player.sendMessage(ChatColor.GRAY + English.SAFE_FROM_PVP.getLine());
                    }
                }
            }, (delay * 20));
        }
        return this;
    }

    public PlayerController setToMortal() {
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
        player.setDisplayName(model.mortalName);
        player.setPlayerListName(model.mortalListName);
        setMortalName(null);
        setMortalListName(null);
        applyMortalInventory();
        return this;
    }

    public PlayerController saveMortalInventory(Player player) {
        // Player inventory
        StoaPlayerInventory mortalInventory = new StoaPlayerInventory();
        PlayerInventory inventory = player.getInventory();
        mortalInventory.generateId();
        if (inventory.getHelmet() != null) mortalInventory.setHelmet(inventory.getHelmet());
        if (inventory.getChestplate() != null) mortalInventory.setChestplate(inventory.getChestplate());
        if (inventory.getLeggings() != null) mortalInventory.setLeggings(inventory.getLeggings());
        if (inventory.getBoots() != null) mortalInventory.setBoots(inventory.getBoots());
        mortalInventory.setItems(inventory);
        mortalInventory.save();
        model.mortalInventoryId = mortalInventory.getId().toString();

        // Enderchest
        StoaEnderInventory enderInventory = new StoaEnderInventory();
        Inventory enderChest = player.getEnderChest();
        enderInventory.generateId();
        enderInventory.setItems(enderChest);
        enderInventory.save();
        model.mortalEnderChestId = enderInventory.getId().toString();

        return this;
    }

    public PlayerController saveCurrentCharacter() {
        // Update the current character
        final Player player = getEntity();
        final CharacterController character = getCharacter();

        if (character != null) {
            character.open();

            // Set to inactive and update previous
            character.setActive(false);

            // Set the values
            character.setHealth(player.getHealth() >= character.getDeity().getMaxHealth() ? character.getDeity().getMaxHealth() : player.getHealth());
            character.setHunger(player.getFoodLevel());
            character.setLevel(player.getLevel());
            character.setExperience(player.getExp());
            character.setLocation(player.getLocation());
            Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getBedSpawnLocation() != null) character.setBedSpawn(player.getBedSpawnLocation());
                }
            }, 1);
            character.setGameMode(player.getGameMode());
            character.setPotionEffects(player.getActivePotionEffects());
            character.saveInventory();

            // Disown pets
            StoaTameable.disownPets(character.model.name);

            character.update();

            character.relinquish();
        }

        return this;
    }

    public PlayerController switchCharacter(final CharacterController newChar) {
        final Player player = getEntity();

        if (!newChar.model.playerId.equals(model.id())) {
            player.sendMessage(ChatColor.RED + "You can't do that.");
            return this;
        }

        // Save the current character
        saveCurrentCharacter();

        // Set new character to active and other info
        model.currentCharacterId = newChar.getModel().id();

        // Apply the new character
        newChar.applyToPlayer(player);

        // Teleport them
        try {
            player.teleport(LocationModel.fromId(newChar.model.locationId));
        } catch (Exception e) {
            MessageUtil.warning("There was a problem while teleporting a player to their character.");
        }

        // Save instances
        open().update().close();
        newChar.open().update().relinquish();

        return this;
    }

    public Set<StoaCharacter> getUsableCharacters() {
        return Sets.filter(getCharacters(), new Predicate<StoaCharacter>() {
            @Override
            public boolean apply(StoaCharacter character) {
                return character.isUsable();
            }
        });
    }

    public StoaPlayerInventory getMortalInventory() {
        return StoaPlayerInventory.get(mortalInventory);
    }

    public StoaEnderInventory getMortalEnderInventory() {
        return StoaEnderInventory.get(mortalEnderInventory);
    }

    public void applyMortalInventory() {
        if (getMortalInventory() == null) mortalInventory = StoaPlayerInventory.createEmpty().getId();
        if (getMortalEnderInventory() == null) mortalEnderInventory = StoaEnderInventory.createEmpty().getId();
        getMortalInventory().setToPlayer(getBukkitOfflinePlayer().getPlayer());
        getMortalEnderInventory().setToPlayer(getBukkitOfflinePlayer().getPlayer());
        mortalInventory = null;
        mortalEnderInventory = null;
    }

    public boolean canMakeCharacter() {
        return getUsableCharacters().size() < getCharacterSlots();
    }

    public boolean canUseCurrent() {
        if (getCharacter() == null || !getCharacter().isUsable()) {
            getBukkitOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Your current character was unable to init!");
            getBukkitOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Please contact the server administrator immediately.");
            return false;
        } else return getBukkitOfflinePlayer().isOnline();
    }

    public void remove() {
        // First we need to kick the player if they're online
        if (getBukkitOfflinePlayer().isOnline())
            getBukkitOfflinePlayer().getPlayer().kickPlayer(ChatColor.RED + "Your player save has been cleared.");

        // Remove characters
        for (StoaCharacter character : getCharacters())
            character.remove();

        // Now we clear the DemigodsPlayer save itself
        super.remove();
    }

    public void sendNotification(Notification notification) {
        if (getCharacter() != null) Notification.sendNotification(getCharacter(), notification);
    }

    /**
     * Starts recording recording the <code>player</code>'s chat.
     */
    public void startRecording() {
        chatRecording = ChatRecorder.Util.startRecording(getBukkitOfflinePlayer().getPlayer());
    }

    /**
     * Stops recording and sends all messages that have been recorded thus far to the player.
     *
     * @param display if true, the chat will be sent to the player
     */
    public List<String> stopRecording(boolean display) {
        Player player = getBukkitOfflinePlayer().getPlayer();
        // Handle recorded chat
        if (chatRecording != null && chatRecording.isRecording()) {
            // Send held back chat
            List<String> messages = chatRecording.stop();
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

    public static StoaPlayer getFromName(final String playerName) {
        try {
            return Iterables.find(all(), new Predicate<StoaPlayer>() {
                @Override
                public boolean apply(StoaPlayer stoaPlayer) {
                    return stoaPlayer.getPlayerName().equals(playerName);
                }
            });
        } catch (NoSuchElementException ignored) {
        }
        return null;
    }

    /**
     * Returns true if the <code>player</code> is currently immortal.
     *
     * @param player the player to check.
     * @return boolean
     */
    public static boolean isImmortal(Player player) {
        StoaCharacter character = of(player).getCharacter();
        return character != null && character.isUsable() && character.isActive();
    }

    /**
     * Returns true if <code>player</code> has a character with the name <code>charName</code>.
     *
     * @param player   the player to check.
     * @param charName the charName to check with.
     * @return boolean
     */
    public static boolean hasCharName(Player player, String charName) {
        for (StoaCharacter character : of(player).getCharacters())
            if (character.getName().equalsIgnoreCase(charName)) return true;
        return false;
    }

    public List<CharacterModel> getCharacters() {
        CharacterModel alias = new CharacterModel();
        Db db = openDb();
        List<CharacterModel> found = db.from(alias).where(alias.playerId).is(model.id()).select();
        db.close();

        return found;
    }

    @Override
    public PlayerController control(String modelId) {
        return (PlayerController) control(modelId, new PlayerModel());
    }

    @Override
    public PlayerController refresh() {
        model = Iterables.getFirst(DB.from(model).where(model.mojangAccount).is(model.id()).select(), model);
        return this;
    }

    @Override
    public CharacterController getCharacter() {
        if (!hasCharacter()) return null;
        return new CharacterController().control(model.currentCharacterId);
    }

    @Override
    public boolean hasCharacter() {
        return model.currentCharacterId != null;
    }

    @Override
    public boolean canPvp() {
        return model.canPvp;
    }

    @Override
    public Location getCurrentLocation() {
        if (getEntity() == null) return null;
        return getEntity().getLocation();
    }

    @Override
    public Player getEntity() {
        return Bukkit.getPlayer(model.mojangAccount);
    }
}
