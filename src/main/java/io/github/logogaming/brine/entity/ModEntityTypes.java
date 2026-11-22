package io.github.logogaming.brine.entity;

import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.entity.custom.ClamEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class ModEntityTypes {
    public static class ModEntityIds {
        public static ResourceKey<EntityType<?>> CLAM = create("clam");

        private static ResourceKey<EntityType<?>> create(String name) {
            return ResourceKey.create(Registries.ENTITY_TYPE, Brine.id(name));
        }
    }

    public static final EntityType<ClamEntity> CLAM = register(
            "clam",
            EntityType.Builder.<ClamEntity>of(ClamEntity::new, MobCategory.WATER_CREATURE)
                    .sized(0.75f, .6f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Brine.id( name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(name));
    }

    public static void registerModEntityTypes() {
        System.out.println("Registering EntityTypes for " + Brine.MOD_ID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(CLAM, ClamEntity.createCubeAttributes());
    }
}