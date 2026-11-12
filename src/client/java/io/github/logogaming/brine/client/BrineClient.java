package io.github.logogaming.brine.client;

import io.github.logogaming.brine.client.shader.BrineShaders;
import io.github.logogaming.brine.logging.BrineLogger;
import net.fabricmc.api.ClientModInitializer;

public class BrineClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BrineShaders.register();
        BrineLogger.info("Client Loading..");
    }
}
