package com.demigodsrpg.stoa.model;

import com.iciql.Iciql;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

@Iciql.IQTable(name = "dg_server_data")
public class ServerDataModel {
    @Iciql.IQColumn(primaryKey = true)
    public String key;
    @Iciql.IQColumn
    public Timestamp expire;
    @Iciql.IQColumn
    public String data;

    public ServerDataModel() {
    }

    public ServerDataModel(String key, Object data) {
        this.key = key;
        this.data = data.toString();
    }

    public ServerDataModel(String key, Object data, int time, TimeUnit unit) {
        this(key, data);
        expire = new Timestamp(System.currentTimeMillis() + unit.toMillis(time));
    }

    public ServerDataModel(String[] key, Object data) {
        StringBuilder builder = new StringBuilder();
        for (String keyPart : key) {
            builder.append(keyPart).append(".");
        }
        String completeKey = builder.toString();
        this.key = completeKey.substring(0, completeKey.length() - 2);
        this.data = data.toString();
    }

    public ServerDataModel(String[] key, Object data, int time, TimeUnit unit) {
        this(key, data);
        expire = new Timestamp(System.currentTimeMillis() + unit.toMillis(time));
    }

    public String asString() {
        return data;
    }

    public Boolean asBool() {
        return Boolean.parseBoolean(data);
    }

    public Double asDouble() {
        return Double.parseDouble(data);
    }

    public Integer asInt() {
        return Integer.parseInt(data);
    }

    public boolean expired() {
        return expire == null || expire.getTime() < System.currentTimeMillis();
    }
}
