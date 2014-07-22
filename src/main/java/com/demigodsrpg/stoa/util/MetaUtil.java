package com.demigodsrpg.stoa.util;

import com.demigodsrpg.stoa.StoaPlugin;
import com.demigodsrpg.stoa.data.MetaCallable;
import net.minecraft.util.com.google.common.collect.Iterables;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class MetaUtil {
    private MetaUtil() {
    }

    public static MetadataValue getMetadata(Metadatable obj, String key) {
        return Iterables.getFirst(obj.getMetadata(key), null);
    }

    public static MetadataValue makeValue(Object data) {
        return new LazyMetadataValue(StoaPlugin.getInst(), new MetaCallable(data));
    }
}
