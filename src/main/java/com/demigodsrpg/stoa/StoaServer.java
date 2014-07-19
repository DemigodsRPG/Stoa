package com.demigodsrpg.stoa;

import com.demigodsrpg.stoa.data.TaskManager;
import com.demigodsrpg.stoa.deity.Ability;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.entity.player.attribute.Skill;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.model.CharacterModel;
import com.demigodsrpg.stoa.model.PlayerModel;
import com.demigodsrpg.stoa.model.StructureModel;
import com.demigodsrpg.stoa.mythos.Mythos;
import com.demigodsrpg.stoa.mythos.MythosSet;
import com.demigodsrpg.stoa.util.*;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.iciql.Db;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.MetricsLite;

import java.util.*;

public class StoaServer {
    // Mythos
    private final Mythos mythos;

    StoaServer() {
        mythos = loadMythos();
    }

    public Mythos getMythos() {
        return mythos;
    }

    // Load everything else.
    protected boolean init() {
        // Config
        Configuration config = StoaPlugin.getInst().getConfig();
        config.options().copyDefaults(true);
        StoaPlugin.getInst().saveConfig();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Initialize metrics
        try {
            (new MetricsLite(StoaPlugin.getInst())).start();
        } catch (Exception ignored) {
            // ignored
        }

        try {
            if (getMythos() == null) {
                MessageUtil.severe("Demigods was unable to load a Mythos.");
                MessageUtil.severe("Please install a Mythos plugin or place the default Demigods-Greek.jar into the plugins\\Demigods\\addons directory.");
                return false;
            }

            if (!StoaPlugin.getInst().getServer().getOnlineMode()) {
                MessageUtil.warning("We depend on Mojang's auth servers for player id.");
                MessageUtil.warning("Players who are not premium may be kicked from the game.");
            }

            // Check for world load errors
            if (loadWorlds() > 0) {
                MessageUtil.warning("Demigods was unable to confirm any worlds.");
                MessageUtil.warning("This may be caused by misspelled world names.");
                MessageUtil.warning("Multi-world plugins can cause this message, and in that case this may be a false alarm.");
            }

            // Load listeners, commands, permissions, and the scoreboard
            loadListeners();
            loadPermissions(true);

            // Update characters
            CharacterUtil.updateUsableCharacters();

            // Start threads
            TaskManager.startThreads();

            // Regenerate structures (on a delay)
            Bukkit.getScheduler().scheduleSyncDelayedTask(StoaPlugin.getInst(), new BukkitRunnable() {
                @Override
                public void run() {
                    StructureUtil.regenerateStructures();
                }
            }, 120);

            if (isRunningSpigot()) {
                MessageUtil.info(("Spigot found, extra API features enabled."));
            } else MessageUtil.warning(("Without Spigot, some features may not work."));

            // Clean skills
            SkillUtil.cleanSkills();

            return true;
        } catch (Exception errored) {
            errored.printStackTrace();
        }
        return false;
    }

    protected Mythos loadMythos() {
        ServicesManager servicesManager = StoaPlugin.getInst().getServer().getServicesManager();
        Collection<RegisteredServiceProvider<Mythos>> mythosProviders = servicesManager.getRegistrations(Mythos.class);
        if (Iterables.any(mythosProviders, new Predicate<RegisteredServiceProvider<Mythos>>() {
            @Override
            public boolean apply(RegisteredServiceProvider<Mythos> mythosProvider) {
                return mythosProvider.getProvider().isPrimary();
            }
        })) {
            // Decide the primary Mythos
            Mythos reiningMythos = null;
            int reiningPriority = 5;

            Set<Mythos> workingSet = Sets.newHashSet();
            for (RegisteredServiceProvider<Mythos> mythosProvider : mythosProviders) {
                Mythos mythos = mythosProvider.getProvider();
                workingSet.add(mythos);
                MessageUtil.info("The " + mythos.getTitle() + " Mythos has enabled!");
                MessageUtil.info("-> Created by " + mythos.getAuthor() + ".");
                MessageUtil.info("-> " + mythos.getTagline());
                if (!mythosProvider.getProvider().isPrimary()) continue;
                if (mythosProvider.getPriority().ordinal() < reiningPriority) // not really sure how Bukkit handles priority, presuming the same way as EventPriority
                {
                    reiningMythos = mythos;
                    reiningPriority = mythosProvider.getPriority().ordinal();
                }
            }

            if (reiningMythos != null) {
                workingSet.remove(reiningMythos);
                if (reiningMythos.useBaseGame() || reiningMythos.allowSecondary() && !workingSet.isEmpty())
                    reiningMythos = new MythosSet(reiningMythos, reiningMythos.allowSecondary() ? workingSet : new HashSet<Mythos>());
                reiningMythos.lock();
                return reiningMythos;
            }
        }
        return null;
    }

    protected int loadWorlds() {
        return ZoneUtil.init();
    }

    protected void loadListeners() {
        PluginManager register = Bukkit.getServer().getPluginManager();

        // Mythos
        for (Listener listener : getMythos().getListeners())
            register.registerEvents(listener, StoaPlugin.getInst());

        // Abilities
        for (Ability ability : Ability.Util.getLoadedAbilities())
            if (ability.getListener() != null) register.registerEvents(ability.getListener(), StoaPlugin.getInst());

        // Structures
        for (StructureModel.Type structureType : Collections2.filter(getMythos().getStructures(), new Predicate<StructureModel.Type>() {
            @Override
            public boolean apply(StructureModel.Type structureType) {
                return structureType.getListener() != null;
            }
        }))
            if (structureType.getListener() != null)
                register.registerEvents(structureType.getListener(), StoaPlugin.getInst());

        // Divine Items
        for (DivineItem divineItem : getMythos().getDivineItems()) {
            if (divineItem.getUniqueListener() != null)
                register.registerEvents(divineItem.getUniqueListener(), StoaPlugin.getInst());
            if (divineItem.getRecipe() != null) StoaPlugin.getInst().getServer().addRecipe(divineItem.getRecipe());
        }

        // Quit reason.
        // TODO Bukkit.getServer().getLogger().addHandler(new QuitReasonHandler());
    }

    protected void loadPermissions(final boolean load) {
        final PluginManager register = Bukkit.getServer().getPluginManager();

        // Mythos
        for (Permission permission : getMythos().getPermissions()) {
            // catch errors to avoid any possible buggy permissions
            try {
                for (Map.Entry<String, Boolean> entry : permission.getChildren().entrySet())
                    registerPermission(register, new Permission(entry.getKey(), entry.getValue() ? PermissionDefault.TRUE : PermissionDefault.FALSE), load);
                registerPermission(register, permission, load);
            } catch (Exception ignored) {
                // ignored
            }
        }

        // Alliances, Deities, and Abilities
        for (final Alliance alliance : getMythos().getAlliances()) {
            // catch errors to avoid any possible buggy permissions
            try {
                registerPermission(register, new Permission(alliance.getPermission(), "The permission to use the " + alliance.getName() + " alliance.", alliance.getPermissionDefault(), new HashMap<String, Boolean>() {
                    {
                        for (Deity deity : Alliance.Util.getLoadedDeitiesInAlliance(alliance)) {
                            registerPermission(register, new Permission(deity.getPermission(), alliance.getPermissionDefault()), load);
                            put(deity.getPermission(), deity.getPermissionDefault().equals(PermissionDefault.NOT_OP) ? alliance.getPermissionDefault().equals(PermissionDefault.TRUE) : deity.getPermissionDefault().equals(PermissionDefault.TRUE));
                        }
                    }
                }), load);
            } catch (Exception ignored) {
                // ignored
            }
        }

        // Skill types
        for (Skill.Type skill : Skill.Type.values())
            registerPermission(register, skill.getPermission(), load);
    }

    void uninit() {
        // Cancel all threads, event calls, and unregister permissions.
        try {
            TaskManager.stopThreads();
            HandlerList.unregisterAll(StoaPlugin.getInst());
            unloadPermissions();
        } catch (Exception ignored) {
            // ignored
        }
    }

    void registerPermission(PluginManager register, Permission permission, boolean load) {
        if (load) register.addPermission(permission);
        else register.removePermission(permission);
    }

    void unloadPermissions() {
        loadPermissions(false);
    }

    public static boolean isRunningSpigot() {
        try {
            Bukkit.getServer().getWorlds().get(0).spigot();
            return true;
        } catch (Exception ignored) {
            // ignored
        }
        return false;
    }

    // -- PLAYER -- //

    public List<PlayerModel> getAllPlayers() {
        PlayerModel alias = new PlayerModel();
        Db db = openDb();
        try {
            return db.from(alias).select();
        } finally {
            db.close();
        }
    }

    public Collection<PlayerModel> getOnlinePlayers() {
        return Collections2.transform(Bukkit.getOnlinePlayers(), new Function<Player, PlayerModel>() {
            @Override
            public PlayerModel apply(Player player) {
                return PlayerUtil.fromPlayer(player);
            }
        });
    }

    // -- MORTAL -- //

    public Collection<PlayerModel> getMortals() {
        PlayerModel alias = new PlayerModel();
        Db db = openDb();
        try {
            return Collections2.filter(db.from(alias).select(), new Predicate<PlayerModel>() {
                @Override
                public boolean apply(PlayerModel player) {
                    CharacterModel character = player.getCharacter();
                    return character == null || !character.usable || !character.active;
                }
            });
        } finally {
            db.close();
        }
    }

    public Collection<PlayerModel> getOnlineMortals() {
        PlayerModel alias = new PlayerModel();
        Db db = openDb();
        try {
            return Collections2.filter(db.from(alias).select(), new Predicate<PlayerModel>() {
                @Override
                public boolean apply(PlayerModel player) {
                    CharacterModel character = player.getCharacter();
                    return player.getOfflinePlayer().isOnline() && (character == null || !character.usable || !character.active);
                }
            });
        } finally {
            db.close();
        }
    }

    // -- CHARACTER -- //

    public CharacterModel getCharacter(String name) {
        return CharacterUtil.fromName(name);
    }

    public Collection<CharacterModel> getActiveCharacters() {
        CharacterModel alias = new CharacterModel();
        Db db = openDb();
        List<CharacterModel> found = db.from(alias).where(alias.usable).is(true).and(alias.active).is(true).select();
        db.close();
        return found;
    }

    public Collection<CharacterModel> getUsableCharacters() {
        CharacterModel alias = new CharacterModel();
        Db db = openDb();
        List<CharacterModel> found = db.from(alias).where(alias.usable).is(true).select();
        db.close();
        return found;
    }

    public Collection<CharacterModel> getOnlineCharacters() {
        List<CharacterModel> onlineCharacters = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            CharacterModel online = CharacterUtil.currentFromPlayer(player);
            if (online != null) onlineCharacters.add(online);
        }
        return onlineCharacters;
    }

    public Collection<CharacterModel> getOnlineCharactersWithAlliance(Alliance alliance) {
        List<CharacterModel> onlineCharacters = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            CharacterModel online = CharacterUtil.currentFromPlayer(player);
            if (online != null && online.getAlliance().equals(alliance)) onlineCharacters.add(online);
        }
        return onlineCharacters;
    }

    public static Db openDb() {
        Configuration config = StoaPlugin.getInst().getConfig();
        return Db.open("jdbc:" + config.getString("db.type") + "://" + config.getString("db.host") + ":" + config.getString("db.port") + "/" + config.getString("db.name"), config.getString("db.user"), config.getString("db.pass"));
    }

}
