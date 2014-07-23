package com.demigodsrpg.stoa.util;


import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.model.EnderChestInventoryModel;
import com.demigodsrpg.stoa.model.PlayerInventoryModel;
import com.iciql.Db;

public class ItemUtil2 {
    public static PlayerInventoryModel playerInvFromOwnerId(String ownerId) {
        PlayerInventoryModel alias = new PlayerInventoryModel();
        Db db = StoaServer.openDb();
        try {
            PlayerInventoryModel model = db.from(alias).where(alias.ownerId).is(ownerId).selectFirst();
            if (model == null) {
                model = new PlayerInventoryModel(ownerId);
                db.insert(model);
            }
            return model;
        } finally {
            db.close();
        }
    }

    public static EnderChestInventoryModel enderInvFromOwnerId(String ownerId) {
        EnderChestInventoryModel alias = new EnderChestInventoryModel();
        Db db = StoaServer.openDb();
        try {
            EnderChestInventoryModel model = db.from(alias).where(alias.ownerId).is(ownerId).selectFirst();
            if (model == null) {
                model = new EnderChestInventoryModel(ownerId);
                db.insert(model);
            }
            return model;
        } finally {
            db.close();
        }
    }
}