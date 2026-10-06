package io.github.logogaming.devsmodwithnocoms.client;

import io.github.logogaming.devsmodwithnocoms.logging.DevsmodwithnocomsLogger;
import net.fabricmc.api.ClientModInitializer;

public class DevsmodwithnocomsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DevsmodwithnocomsLogger.info("Client Loading..");
    }
}
