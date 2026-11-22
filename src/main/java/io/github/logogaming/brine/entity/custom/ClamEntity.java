package io.github.logogaming.brine.entity.custom;

import io.github.logogaming.brine.entity.goal.ClamShellGoal;
import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.logging.BrineLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ClamEntity extends Mob {

    public final AnimationState openAnimationState = new AnimationState();
    public final AnimationState closeAnimationState = new AnimationState();

    public static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(ClamEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> HAS_PEARL = SynchedEntityData.defineId(ClamEntity.class, EntityDataSerializers.BOOLEAN);

    public ClamEntity(EntityType<ClamEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OPEN, false);
        builder.define(HAS_PEARL, true);
    }

    public boolean isOpen() {
        return this.entityData.get(OPEN);
    }

    public boolean hasPearl() {
        return this.entityData.get(HAS_PEARL);
    }

    public void setOpen(boolean open) {
        this.entityData.set(OPEN, open);
    }

    public void setHasPearl(boolean pearl) {
        this.entityData.set(HAS_PEARL, pearl);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("HasPearl", this.hasPearl());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setHasPearl(compound.getBoolean("HasPearl"));
    }

    @Override
    public void aiStep() {

//        BrineLogger.info(String.valueOf(closeAnimationState.getAccumulatedTime()));

        if(this.getRandom().nextInt(6000) == 0 && !this.hasPearl()) {
            this.setHasPearl(true);
            BrineLogger.info("generate pearl");
        }

        super.aiStep();
    }

    @Override
    public @NotNull InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if(this.hasPearl() && (this.isOpen() || (closeAnimationState.isStarted() && closeAnimationState.getAccumulatedTime() < 1000))) {
            if(!this.level().isClientSide()) {
                BrineLogger.info("grab");
                this.setHasPearl(false);
                this.spawnAtLocation(BrineItems.PEARL);
            }
        }

        return super.interactAt(player, vec, hand);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        if(OPEN.equals(dataAccessor) && this.level().isClientSide) {
            if(this.isOpen()) {
                this.closeAnimationState.stop();
                this.openAnimationState.start(this.tickCount);
            } else {
                this.openAnimationState.stop();
                this.closeAnimationState.start(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(dataAccessor);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static AttributeSupplier.Builder createCubeAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ClamShellGoal(this));
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    public static boolean checkClamSpawnRules(EntityType<ClamEntity> type, ServerLevelAccessor level,
                                              MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
