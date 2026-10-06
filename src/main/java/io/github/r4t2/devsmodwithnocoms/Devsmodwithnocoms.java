package io.github.r4t2.devsmodwithnocoms;

import io.github.r4t2.devsmodwithnocoms.logging.DevsmodwithnocomsLogger;
import net.fabricmc.api.ModInitializer;

public class Devsmodwithnocoms implements ModInitializer {
    @Override
    public void onInitialize() {
        DevsmodwithnocomsLogger.info("Started");
    }
}