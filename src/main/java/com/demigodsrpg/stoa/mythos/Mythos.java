package com.demigodsrpg.stoa.mythos;

import com.censoredsoftware.library.command.CommandManager;
import com.demigodsrpg.stoa.data.thread.SyncAsyncRunnable;
import com.demigodsrpg.stoa.deity.Alliance;
import com.demigodsrpg.stoa.deity.Deity;
import com.demigodsrpg.stoa.item.DivineItem;
import com.demigodsrpg.stoa.model.StructureModel;
import com.google.common.collect.ImmutableCollection;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

public interface Mythos {
    String getTitle();

    String getTagline();

    String getAuthor();

    Boolean isPrimary();

    Boolean allowSecondary();

    String[] getIncompatible();

    Boolean useBaseGame();

    ImmutableCollection<DivineItem> getDivineItems();

    ImmutableCollection<Alliance> getAlliances();

    Alliance getAlliance(String allianceName);

    ImmutableCollection<Deity> getDeities();

    Deity getDeity(String deityName);

    ImmutableCollection<StructureModel.Type> getStructures();

    StructureModel.Type getStructure(String structureName);

    Boolean levelSeperateSkills();

    ImmutableCollection<Listener> getListeners();

    ImmutableCollection<Permission> getPermissions();

    ImmutableCollection<CommandManager> getCommands();

    ImmutableCollection<SyncAsyncRunnable> getSyncAsyncTasks();

    void setSecondary();

    void lock();
}
