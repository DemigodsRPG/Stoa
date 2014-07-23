package com.demigodsrpg.stoa.model;

import com.demigodsrpg.stoa.StoaServer;
import com.iciql.Db;
import com.iciql.Iciql;
import org.bukkit.Material;

@Iciql.IQTable(name = "dg_tributes")
public class TributeModel {
    @Iciql.IQColumn(primaryKey = true, autoIncrement = true)
    public Long id;
    @Iciql.IQColumn
    public String category;
    @Iciql.IQEnum
    @Iciql.IQColumn
    public Material material;
    @Iciql.IQColumn
    public Integer amount;

    public TributeModel() {
    }

    public TributeModel(String category, Material material, int amount) {
        this.category = category;
        this.material = material;
        this.amount = amount;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void remove() {
        Db db = StoaServer.openDb();
        db.delete(this);
        db.close();
    }
}
