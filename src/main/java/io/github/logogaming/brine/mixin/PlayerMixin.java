package io.github.logogaming.brine.mixin;

import io.github.logogaming.brine.item.BrineItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract @NotNull ItemStack getItemBySlot(EquipmentSlot slot);

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void brine$sinkWithAnchor(CallbackInfo ci) {
        if (this.isInWater() && !this.isSpectator()) {
            if (isHolding(BrineItems.ANCHOR)) {
                push(0, -0.15D, 0);
            } else if (isHolding(BrineItems.LIFE_PRESERVER)) {
                push(0, 0.15D, 0);
            }
        }
    }
}
