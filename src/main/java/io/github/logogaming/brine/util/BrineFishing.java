package io.github.logogaming.brine.util;

import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.item.BrineItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashMap;
import java.util.Map;

public class BrineFishing {
    private static final Map<Item, ResourceKey<LootTable>> TABLES = new HashMap<>();

    public static void initialize() {
        TABLES.put(Items.FISHING_ROD, key("gameplay/fishing/fishing_rod"));
        TABLES.put(BrineItems.BARNACLE_ROD, key("gameplay/fishing/barnacle_rod"));
        TABLES.put(BrineItems.IRON_ROD, key("gameplay/fishing/iron_rod"));
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Brine.id(path));
    }

    public static ResourceKey<LootTable> tableFor(Item item) {
        return TABLES.get(item);
    }
}
