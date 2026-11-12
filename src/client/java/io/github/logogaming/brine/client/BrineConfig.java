package io.github.logogaming.brine.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.logogaming.brine.logging.BrineLogger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BrineConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("brine.json");

    private static Data data = new Data();

    private BrineConfig() {
    }

    public static float waveStrength() {
        return data.waveStrength;
    }

    public static float calmFloor() {
        return data.calmFloor;
    }

    public static void setWaveStrength(float value) {
        data.waveStrength = value;
        save();
    }

    public static void setCalmFloor(float value) {
        data.calmFloor = value;
        save();
    }

    public static void reset() {
        data = new Data();
        save();
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }
        try {
            Data loaded = GSON.fromJson(Files.readString(PATH), Data.class);
            if (loaded != null) {
                data = loaded;
            }
        } catch (IOException | RuntimeException exception) {
            BrineLogger.warn("Could not read brine.json, using defaults");
        }
    }

    private static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(data));
        } catch (IOException exception) {
            BrineLogger.warn("Could not write brine.json");
        }
    }

    private static final class Data {
        float waveStrength = 1.0f;
        float calmFloor = 0.22f;
    }
}
