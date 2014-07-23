package com.demigodsrpg.stoa;

import com.censoredsoftware.library.command.AbstractJavaPlugin;
import com.demigodsrpg.stoa.mythos.Mythos;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Bukkit plugin object for implementations of Stoa.
 */
public abstract class StoaPlugin extends AbstractJavaPlugin {
    static StoaPlugin INST;
    static boolean ready = false;

    public static StoaPlugin getInst() {
        return INST;
    }

    public static Configuration config() {
        return INST.getConfig();
    }

    public static boolean getReady() {
        return ready;
    }

    /**
     * The Bukkit enable method.
     */
    @Override
    public void onEnable() {
        INST = this;

        loadComponents();

        // Load the game engine.
        if (!Stoa.getServer().init()) {
            getLogger().severe(getName() + " could not initialize.");
            getPluginLoader().disablePlugin(this);
            return;
        } else ready = true;

        // Print success!
        message(" enabled");
    }

    /**
     * The Bukkit disable method.
     */
    @Override
    public void onDisable() {
        Stoa.getServer().uninit();

        if (ready) message(" disabled");
    }

    protected abstract void message(String status);

    public abstract Mythos getBaseGame();

    // FIXME
    private void loadComponents() {
        // Unload all incorrectly installed plugins
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            // Not soft-depend
            List<String> depends = plugin.getDescription().getDepend();
            if (depends != null && !depends.isEmpty() && depends.contains(getName())) {
                getLogger().warning(plugin.getName() + " was put in the wrong directory.");
                getLogger().warning("Please place " + getName() + " addons in the");
                getLogger().warning(getDataFolder().getPath() + "\\addons\\ directory");
                getLogger().warning("(i.e. " + getDataFolder().getPath() + "\\addons\\" + plugin.getName() + ").");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }

        // Load Demigods plugins
        File pluginsFolder = new File(getDataFolder() + "/addons");
        if (!pluginsFolder.exists()) pluginsFolder.mkdirs();

        Collection<File> files = Collections2.filter(Sets.newHashSet(pluginsFolder.listFiles()), new Predicate<File>() {
            @Override
            public boolean apply(File file) {
                return file != null && file.getName().toLowerCase().endsWith(".jar");
            }
        });

        for (File file : files) {
            try {
                getLogger().info(file.getName() + " loading.");
                Bukkit.getServer().getPluginManager().enablePlugin(Bukkit.getServer().getPluginManager().loadPlugin(file));
            } catch (Exception errored) {
                getLogger().warning(errored.getMessage());
            }
        }
    }
}
