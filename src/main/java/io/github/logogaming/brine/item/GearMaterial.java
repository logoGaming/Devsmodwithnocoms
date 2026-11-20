package io.github.logogaming.brine.item;

import io.github.logogaming.brine.Brine;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class GearMaterial {

    public static final int KELP_DURABILITY_MULTIPLIER = 5;
    public static final int IRON_DURABILITY_MULTIPLIER = 15;

    public static final Holder<ArmorMaterial> KELPG = registerMaterial("kelpg", Map.of(
            ArmorItem.Type.HELMET, 1,
            ArmorItem.Type.CHESTPLATE, 3,
            ArmorItem.Type.LEGGINGS, 2,
            ArmorItem.Type.BOOTS, 1
        ),
        20,
        SoundEvents.ARMOR_EQUIP_TURTLE,
        () -> Ingredient.of(Items.KELP),
        0.0F,
        0.0F,
        false);

    public static final Holder<ArmorMaterial> IRONG = registerMaterial("irong",
        Map.of(
            ArmorItem.Type.HELMET, 4,
            ArmorItem.Type.CHESTPLATE, 6,
            ArmorItem.Type.LEGGINGS, 5,
            ArmorItem.Type.BOOTS, 3
        ),
        10,
        SoundEvents.ARMOR_EQUIP_IRON,
        () -> Ingredient.of(Items.IRON_INGOT),
        0.0F,
        0.0F,
        false);


    public static Holder<ArmorMaterial> registerMaterial(String id, Map<ArmorItem.Type, Integer> defensePoints, int enchantability, Holder<SoundEvent> equipSound, Supplier<Ingredient> repairIngredientSupplier, float toughness, float knockbackResistance, boolean dyeable) {
        List<ArmorMaterial.Layer> layers = List.of(
            new ArmorMaterial.Layer(Brine.id(id), "", dyeable)
        );
        ArmorMaterial material = new ArmorMaterial(defensePoints, enchantability, equipSound, repairIngredientSupplier, layers, toughness, knockbackResistance);
        material = Registry.register(BuiltInRegistries.ARMOR_MATERIAL, ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, id), material);
        return Holder.direct(material);
    }


    public static void initialize() {
    }
}
