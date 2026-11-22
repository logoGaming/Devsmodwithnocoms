package io.github.logogaming.brine;

import io.github.logogaming.brine.effect.BrineEffects;
import io.github.logogaming.brine.entity.ModEntityTypes;
import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.logging.BrineLogger;
import io.github.logogaming.brine.oxygen.BrineAttributes;
import io.github.logogaming.brine.util.BrineFishing;
import io.github.logogaming.brine.world.BrineDimensions;
import io.github.logogaming.brine.world.BrineEntitySpawns;
import io.github.logogaming.brine.world.FirstJoinSpawn;
import io.github.logogaming.brine.world.SpawnStructureState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Brine implements ModInitializer {

    public static final String MOD_ID = "brine";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        BrineAttributes.register();
        BrineItems.initialize();
        BrineFishing.initialize();
        BrineEffects.init();
        FirstJoinSpawn.register();
        ModEntityTypes.registerAttributes();
        ModEntityTypes.registerModEntityTypes();
        BrineEntitySpawns.addEntitySpawns();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel level = server.getLevel(BrineDimensions.BRINE);
            SpawnStructureState state  = SpawnStructureState.get(level);
            if(state.isPlaced()) return;

            Optional<StructureTemplate> boat = level.getStructureManager().get(Brine.id("boat"));
            if(boat.isEmpty()) {
                // big uh-oh
                BrineLogger.info("Unable to find spawn boat");
                return;
            }

            BlockPos spawn = new BlockPos(0,0,0);
            Vec3i size = boat.get().getSize();
            BlockPos corner = new BlockPos(spawn.getX() - size.getX() / 2, BrineDimensions.SEA_LEVEL, spawn.getZ() - size.getZ() / 2);

            boat.get().placeInWorld(level, corner, corner, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);

            state.markPlaced();
        });

        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.MANGROVE_PROPAGULE)) return InteractionResultHolder.pass(stack);

            Vec3 eye = player.getEyePosition();
            Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
            BlockHitResult hit = level.clip(new ClipContext(eye, end,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
            if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);

            BlockPos waterPos = hit.getBlockPos();
            BlockPos placePos = waterPos.above();
            BlockState propagule = Blocks.MANGROVE_PROPAGULE.defaultBlockState();

            if (level.getFluidState(waterPos).getType() != Fluids.WATER
                    || !level.getBlockState(placePos).isAir()
                    || !level.mayInteract(player, waterPos)
                    || !propagule.canSurvive(level, placePos)) {
                return InteractionResultHolder.pass(stack);
            }

            if (!level.isClientSide()) {
                level.setBlockAndUpdate(placePos, propagule);
                level.playSound(null, placePos, propagule.getSoundType().getPlaceSound(),
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.consume(1, player);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        });

        BrineLogger.info("Started");
    }
}
