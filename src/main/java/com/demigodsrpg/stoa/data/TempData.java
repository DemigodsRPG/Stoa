package com.demigodsrpg.stoa.data;

import com.google.common.base.Suppliers;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alexander on 7/20/2014.
 */
public class TempData {
    public final static Table<String, String, Object> TABLE = Tables.newCustomTable(new ConcurrentHashMap<String, Map<String, Object>>(), Suppliers.ofInstance(new ConcurrentHashMap<String, Object>()));
}
