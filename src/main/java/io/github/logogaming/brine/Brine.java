package io.github.logogaming.brine;

import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.logging.BrineLogger;
import io.github.logogaming.brine.world.FirstJoinSpawn;
import net.fabricmc.api.ModInitializer;

public class Brine implements ModInitializer {
    @Override
    public void onInitialize() {
        BrineItems.register();
        FirstJoinSpawn.register();
        BrineLogger.info("Started");
    }
}
