package io.github.logogaming.brine.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public final class BrineCommands {

    private BrineCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("brine")
                        .then(ClientCommandManager.literal("waves")
                                .executes(context -> {
                                    context.getSource().sendFeedback(Component.literal("Wave strength is " + BrineConfig.waveStrength()));
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(0.0f, 3.0f))
                                        .executes(context -> {
                                            float value = FloatArgumentType.getFloat(context, "value");
                                            BrineConfig.setWaveStrength(value);
                                            context.getSource().sendFeedback(Component.literal("Wave strength set to " + value));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("calm")
                                .executes(context -> {
                                    context.getSource().sendFeedback(Component.literal("Calm floor is " + BrineConfig.calmFloor()));
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                                        .executes(context -> {
                                            float value = FloatArgumentType.getFloat(context, "value");
                                            BrineConfig.setCalmFloor(value);
                                            context.getSource().sendFeedback(Component.literal("Calm floor set to " + value));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("reset")
                                .executes(context -> {
                                    BrineConfig.reset();
                                    context.getSource().sendFeedback(Component.literal("Brine settings reset"));
                                    return 1;
                                }))
        ));
    }
}
