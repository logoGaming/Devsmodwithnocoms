package io.github.logogaming.brine;

import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.logging.BrineLogger;
import io.github.logogaming.brine.oxygen.BrineAttributes;
import io.github.logogaming.brine.world.BrineEntitySpawns;
import io.github.logogaming.brine.world.FirstJoinSpawn;
import net.fabricmc.api.ModInitializer;

public class Brine implements ModInitializer {

    public static final String MOD_ID = "brine";


    @Override
    public void onInitialize() {
        BrineAttributes.register();
        BrineItems.initialize();
        FirstJoinSpawn.register();
        BrineEntitySpawns.addEntitySpawns();
        BrineLogger.info("Started");
    }
}
