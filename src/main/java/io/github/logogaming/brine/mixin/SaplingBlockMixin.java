package io.github.logogaming.brine.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin {

    @Inject(method = "advanceTree", at = @At("HEAD"))
    private void brine$anchorOnWater(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, CallbackInfo ci) {
        if (!(((Object) this) instanceof MangrovePropaguleBlock)) return;
        BlockPos below = pos.below();
        if (level.getFluidState(below).is(FluidTags.WATER)) {
            // TODO: unfinished
            level.setBlock(below, Blocks.MUD.defaultBlockState(), 3);
        }
    }
}