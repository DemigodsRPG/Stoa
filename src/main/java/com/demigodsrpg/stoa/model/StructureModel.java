package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.Stoa;
import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.schematic.Schematic;
import com.demigodsrpg.stoa.util.MetaUtil;
import com.iciql.Db;
import com.iciql.Iciql;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

@Iciql.IQTable(name = "dg_structures")
public class StructureModel {
    @Iciql.IQColumn(primaryKey = true, autoIncrement = true)
    public Long id;

    @Iciql.IQColumn(name = "center_world")
    public String world;
    @Iciql.IQColumn(name = "center_x")
    public Integer x;
    @Iciql.IQColumn(name = "center_y")
    public Integer y;
    @Iciql.IQColumn(name = "center_z")
    public Integer z;

    @Iciql.IQColumn(name = "owner_id")
    public String ownerId;

    @Iciql.IQColumn(name = "type_name")
    public String typeName;
    @Iciql.IQColumn(name = "design_name")
    public String designName;

    @Iciql.IQColumn
    public Double sanctity;
    @Iciql.IQColumn
    public Double corruption;

    private transient Block center;
    private transient Type type;
    private transient Design design;

    public StructureModel() {
    }

    public StructureModel(CharacterModel owner, Block center, Type type, Design design) {
        this(owner.getId(), center, type, design);
    }

    public StructureModel(String ownerId, Block center, Type type, Design design) {
        this.x = center.getX();
        this.y = center.getY();
        this.z = center.getZ();
        this.ownerId = ownerId;
        this.typeName = type.getName();
        this.designName = design.getName();
        this.sanctity = type.getDefSanctity();
        this.center = center;
        this.type = type;
        this.design = design;
    }

    public StructureModel marshall() {
        if (id != null && Bukkit.getWorld(world) != null) {
            center = new Location(Bukkit.getWorld(world), x, y, z).getBlock();
            type = Stoa.getMythos().getStructure(typeName);
            design = type.getDesign(designName);
            setDefaultMetaData();
        }
        return this;
    }

    public void setDefaultMetaData() {
        center.setMetadata("stoa.structure.owner", MetaUtil.makeValue(ownerId));
        center.setMetadata("stoa.structure.type", MetaUtil.makeValue(typeName));
        center.setMetadata("stoa.structure.design", MetaUtil.makeValue(designName));
        for (Location location : getLocations()) {
            Block block = location.getBlock();
            block.setMetadata("stoa.structure", MetaUtil.makeValue(true));
            block.setMetadata("stoa.structure.center.world", MetaUtil.makeValue(center.getWorld().getName()));
            block.setMetadata("stoa.structure.center.x", MetaUtil.makeValue(center.getX()));
            block.setMetadata("stoa.structure.center.y", MetaUtil.makeValue(center.getX()));
            block.setMetadata("stoa.structure.center.z", MetaUtil.makeValue(center.getX()));
        }
    }

    public void sanctify(CharacterModel character, double amount) {
        if (getType().sanctify(this, character)) {
            sanctify(amount);
        }
    }

    public void sanctify(double amount) {
        if (sanctity == null || sanctity < 0F) {
            sanctity = 0.0;
        }
        sanctity = sanctity + amount;
    }

    public void corrupt(CharacterModel character, double amount) {
        if (getType().corrupt(this, character)) {
            corrupt(amount);
            if (corruption >= sanctity && getType().kill(this, character)) {
                kill(character);
            }
        }
    }

    public void corrupt(double amount) {
        if (corruption == null || corruption < 0F) {
            corruption = 0.0;
        }
        corruption = corruption + amount;
    }

    public Block getCenter() {
        return center;
    }

    public void setCenter(Block center) {
        this.center = center;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Collection<Flag> getFlags() {
        return type.getFlags();
    }

    public Design getDesign() {
        return design;
    }

    public Collection<Location> getClickable() {
        return design.getClickable(center.getLocation());
    }

    public Schematic getSchematic() {
        return design.getSchematic(this);
    }

    public Set<Location> getLocations() {
        return getSchematic().getLocations(center.getLocation());
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    public boolean sanctify(CharacterModel character) {
        return type.sanctify(this, character);
    }

    public boolean corrupt(CharacterModel character) {
        return type.corrupt(this, character);
    }

    public boolean birth(CharacterModel character) {
        return type.birth(this, character);
    }

    public boolean kill(CharacterModel character) {
        return type.kill(this, character);
    }

    public void remove() {
        for (Location location : getLocations()) {
            Block block = location.getBlock();
            block.setType(Material.AIR);
            block.removeMetadata("stoa.structure.owner", StoaPlugin.getInst());
            block.removeMetadata("stoa.structure.type", StoaPlugin.getInst());
            block.removeMetadata("stoa.structure.design", StoaPlugin.getInst());
            block.removeMetadata("stoa.structure.center.world", StoaPlugin.getInst());
            block.removeMetadata("stoa.structure.center.x", StoaPlugin.getInst());
            block.removeMetadata("stoa.structure.center.y", StoaPlugin.getInst());
            block.removeMetadata("stoa.structure.center.z", StoaPlugin.getInst());
        }
        Db db = StoaServer.openDb();
        db.delete(this);
        db.close();
    }

    public interface Type {
        String getName();

        Design getDesign(final String name);

        Collection<Design> getDesigns();

        Collection<Flag> getFlags();

        Collection<String> getSpecialMetaKeys();

        Listener getListener();

        boolean sanctify(StructureModel data, CharacterModel character);

        boolean corrupt(StructureModel data, CharacterModel character);

        boolean birth(StructureModel data, CharacterModel character);

        boolean kill(StructureModel data, CharacterModel character);

        double getDefSanctity();

        double getSanctityRegen();

        int getRadius();

        int getRequiredGenerationCoords();

        boolean isAllowed(@Nullable StructureModel data, Player sender);

        StructureModel createNew(boolean generate, @Nullable String design, Location... reference);
    }

    public interface Design {
        String getName();

        Set<Location> getClickable(Location reference);

        Schematic getSchematic(@Nullable StructureModel data);
    }

    public enum Flag {
        DELETE_WITH_OWNER, DESTRUCT_ON_BREAK, PROTECTED_BLOCKS, NO_GRIEFING, NO_PVP, PRAYER_LOCATION, OBELISK_LOCATION, TRIBUTE_LOCATION, RESTRICTED_AREA, NO_OVERLAP, STRUCTURE_WAND_GENERABLE
    }
}
