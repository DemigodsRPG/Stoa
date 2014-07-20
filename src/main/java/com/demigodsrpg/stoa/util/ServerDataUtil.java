package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaServer;
import com.demigodsrpg.stoa.model.ServerDataModel;
import com.iciql.Db;

import java.util.concurrent.TimeUnit;

public class ServerDataUtil {
    private ServerDataUtil() {
    }

    public static boolean exists(String... key) {
        ServerDataModel alias = new ServerDataModel();
        Db db = StoaServer.openDb();

        try {
            if (key.length == 1) {
                return !db.from(alias).where(alias.key).is(key[0]).select().isEmpty();
            } else {
                StringBuilder builder = new StringBuilder();
                for (String keyPart : key) {
                    builder.append(keyPart).append(".");
                }
                String completeKey = builder.toString();
                completeKey = completeKey.substring(0, completeKey.length() - 2);
                return !db.from(alias).where(alias.key).is(completeKey).select().isEmpty();
            }
        } finally {
            db.close();
        }
    }

    public static ServerDataModel get(String... key) {
        ServerDataModel alias = new ServerDataModel();
        Db db = StoaServer.openDb();

        try {
            if (key.length == 1) {
                return db.from(alias).where(alias.key).is(key[0]).selectFirst();
            } else {
                StringBuilder builder = new StringBuilder();
                for (String keyPart : key) {
                    builder.append(keyPart).append(".");
                }
                String completeKey = builder.toString();
                completeKey = completeKey.substring(0, completeKey.length() - 2);
                return db.from(alias).where(alias.key).is(completeKey).selectFirst();
            }
        } finally {
            db.close();
        }
    }

    public static ServerDataModel put(Object data, String... key) {
        ServerDataModel model = new ServerDataModel(key, data);
        Db db = StoaServer.openDb();

        if (exists(key)) {
            db.update(model);
        } else {
            db.insert(model);
        }

        db.close();
        return model;
    }

    public static ServerDataModel put(Object data, int time, TimeUnit unit, String... key) {
        ServerDataModel model = new ServerDataModel(key, data, time, unit);

        Db db = StoaServer.openDb();
        if (exists(key)) {
            db.update(model);
        } else {
            db.insert(model);
        }

        db.close();
        return model;
    }

    public static int remove(String... key) {
        ServerDataModel alias = new ServerDataModel();
        Db db = StoaServer.openDb();

        try {
            if (key.length == 1) {
                return db.delete(db.from(alias).where(alias.key).is(key[0]).selectFirst());
            } else {
                StringBuilder builder = new StringBuilder();
                for (String keyPart : key) {
                    builder.append(keyPart).append(".");
                }
                String completeKey = builder.toString();
                completeKey = completeKey.substring(0, completeKey.length() - 2);
                return db.delete(db.from(alias).where(alias.key).is(completeKey).selectFirst());
            }
        } finally {
            db.close();
        }
    }

    public static String getCompleteKey(String... key) {
        StringBuilder builder = new StringBuilder();
        for (String keyPart : key) {
            builder.append(keyPart).append(".");
        }
        String completeKey = builder.toString();
        return completeKey.substring(0, completeKey.length() - 2);
    }

    public static void clearExpired() {
        ServerDataModel alias = new ServerDataModel();
        Db db = StoaServer.openDb();

        for (ServerDataModel model : db.from(alias).select()) {
            if (model.expired()) {
                db.delete(model);
            }
        }

        db.close();
    }
}
