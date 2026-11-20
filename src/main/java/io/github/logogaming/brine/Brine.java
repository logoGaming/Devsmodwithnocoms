package io.github.logogaming.brine;

import io.github.logogaming.brine.effect.BrineEffects;
import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.logging.BrineLogger;
import io.github.logogaming.brine.oxygen.BrineAttributes;
import io.github.logogaming.brine.world.BrineEntitySpawns;
import io.github.logogaming.brine.world.FirstJoinSpawn;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class Brine implements ModInitializer {

    public static final String MOD_ID = "brine";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        BrineAttributes.register();
        BrineItems.initialize();
        BrineEffects.init();
        FirstJoinSpawn.register();
        BrineEntitySpawns.addEntitySpawns();
        BrineLogger.info("Started");
    }
}
