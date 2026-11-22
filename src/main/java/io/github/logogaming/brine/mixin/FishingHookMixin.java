package io.github.logogaming.brine.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.logogaming.brine.logging.BrineLogger;
import io.github.logogaming.brine.util.BrineFishing;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    @Inject(method = "shouldStopFishing", at= @At("HEAD"), cancellable = true)
    private void brine$allowTieredRods(Player player, CallbackInfoReturnable<Boolean> cir)
    {
        boolean holdingRod = player.getMainHandItem().getItem() instanceof FishingRodItem
                || player.getOffhandItem().getItem() instanceof FishingRodItem;

        FishingHook self = (FishingHook) (Object) this;
        if (!player.isRemoved() && player.isAlive() && holdingRod
                && self.distanceToSqr(player) <= 1024.0) {
            cir.setReturnValue(false);
        }
    }


    @WrapOperation(
            method = "retrieve",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/ReloadableServerRegistries$Holder;getLootTable(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/level/storage/loot/LootTable;")
    )
    private LootTable brine$rodLootTable(ReloadableServerRegistries.Holder instance, ResourceKey<LootTable> lootTableKey, Operation<LootTable> original, @Local(argsOnly = true) ItemStack rod) {
        ResourceKey<LootTable> custom = BrineFishing.tableFor(rod.getItem());
        BrineLogger.info("FISH");
        System.out.println(custom);
        return original.call(instance, custom != null ? custom : lootTableKey);
    }
}
