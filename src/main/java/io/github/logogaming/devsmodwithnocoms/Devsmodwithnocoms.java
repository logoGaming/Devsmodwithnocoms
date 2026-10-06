package io.github.logogaming.devsmodwithnocoms;

import io.github.logogaming.devsmodwithnocoms.logging.DevsmodwithnocomsLogger;
import net.fabricmc.api.ModInitializer;

public class Devsmodwithnocoms implements ModInitializer {
    @Override
    public void onInitialize() {
        DevsmodwithnocomsLogger.info("Started");
    }
}