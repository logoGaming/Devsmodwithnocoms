package io.github.r4t2.devsmodwithnocoms.client;

import io.github.r4t2.devsmodwithnocoms.logging.DevsmodwithnocomsLogger;
import net.fabricmc.api.ClientModInitializer;

public class DevsmodwithnocomsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DevsmodwithnocomsLogger.info("Client Loading..");
    }
}
