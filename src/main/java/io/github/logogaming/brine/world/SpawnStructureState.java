package io.github.logogaming.brine.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class SpawnStructureState extends SavedData {
    private static final String KEY = "brine_spawn_structure";
    private boolean placed;

    public static SpawnStructureState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SpawnStructureState::new, SpawnStructureState::load, null), KEY);
    }

    private static SpawnStructureState load(CompoundTag tag, HolderLookup.Provider registries) {
        SpawnStructureState state = new SpawnStructureState();
        state.placed = tag.getBoolean("Placed");
        return state;
    }

    public boolean isPlaced() { return placed; }
    public void markPlaced() { placed = true; setDirty(); }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("Placed", placed);
        return tag;
    }
}
