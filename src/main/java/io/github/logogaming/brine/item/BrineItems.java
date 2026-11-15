package io.github.logogaming.brine.item;

import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.oxygen.BrineAttributes;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class BrineItems {

    public static final Item FLASHLIGHT = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("brine", "flashlight"),
            new FlashlightItem(new Item.Properties().stacksTo(1))
    );

    public static final Item COPPERBATTERY = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("brine", "copper-battery"),
            new Item(new Item.Properties().stacksTo(64))
    );

    public static final Item ANCHOR = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("brine", "anchor"),
            new Item(new Item.Properties().stacksTo(1))
    );

    public static final Item PEARL = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("brine", "pearl"),
            new Item(new Item.Properties().stacksTo(64))
    );

    //unfinshed
    public static final Item SEASHIFT = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("brine", "seashift"),
            new Item(new Item.Properties().stacksTo(1))
    );


    public static final Item KELPOXYGENTANK = register(
            new ArmorItem(GearMaterial.IRONG, ArmorItem.Type.CHESTPLATE, new Item.Properties()
                    .durability(ArmorItem.Type.HELMET.getDurability(GearMaterial.IRON_DURABILITY_MULTIPLIER))
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(BrineAttributes.MAX_OXYGEN, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "iron_diving_mask_oxygen"),
                                            500.0,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                            .add(Attributes.SUBMERGED_MINING_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "iron_diving_mask_oxygen"),
                                            0.25,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                            .build())),
            "kelp-oxygen-tank"
    );

    public static final Item IRONOXYGENTANK = register(
            new ArmorItem(GearMaterial.IRONG, ArmorItem.Type.CHESTPLATE, new Item.Properties()
                    .durability(ArmorItem.Type.HELMET.getDurability(GearMaterial.IRON_DURABILITY_MULTIPLIER))
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(BrineAttributes.MAX_OXYGEN, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "iron_diving_mask_oxygen"),
                                            1000.0,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                            .add(Attributes.SUBMERGED_MINING_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "iron_diving_mask_oxygen"),
                                            0.75,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                            .build())),
            "iron-oxygen-tank"
    );


    public static final Item KELPMASK = register(
            new ArmorItem(GearMaterial.KELPG, ArmorItem.Type.HELMET, new Item.Properties()
                    .durability(ArmorItem.Type.HELMET.getDurability(GearMaterial.IRON_DURABILITY_MULTIPLIER))
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(BrineAttributes.OXYGEN_DRAIN_RATE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "iron_diving_mask_oxygen"),
                                            -0.25,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.HEAD)
                            .build())),
            "kelp-mask"
    );

    public static final Item IRONDIVINGMASK = register(
            new ArmorItem(GearMaterial.IRONG, ArmorItem.Type.HELMET, new Item.Properties()
                    .durability(ArmorItem.Type.HELMET.getDurability(GearMaterial.IRON_DURABILITY_MULTIPLIER))
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(BrineAttributes.OXYGEN_DRAIN_RATE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "iron_diving_mask_oxygen"),
                                            -100,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.HEAD)
                            .build())),
            "iron-diving-mask"
    );




    private BrineItems() {
    }

    public static Item register(Item item, String id) {
        ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, id);
        return Registry.register(BuiltInRegistries.ITEM, itemID, item);
    }

    public static final ResourceKey<CreativeModeTab> BRINE_ITEMS_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "brine_items"));
    public static final CreativeModeTab BRINE_ITEMS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(BrineItems.FLASHLIGHT))
            .title(Component.translatable("brine-items.brine"))
            .build();



    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BRINE_ITEMS_KEY, BRINE_ITEMS);

        ItemGroupEvents.modifyEntriesEvent(BRINE_ITEMS_KEY).register(itemGroup -> {
            itemGroup.accept(BrineItems.FLASHLIGHT);
            itemGroup.accept(BrineItems.KELPMASK);
            itemGroup.accept(BrineItems.KELPOXYGENTANK);
            itemGroup.accept(BrineItems.IRONDIVINGMASK);
            itemGroup.accept(BrineItems.IRONOXYGENTANK);
            itemGroup.accept(BrineItems.SEASHIFT);
            itemGroup.accept(BrineItems.PEARL);
            itemGroup.accept(BrineItems.COPPERBATTERY);
            itemGroup.accept(BrineItems.ANCHOR);
        });

        GearMaterial.initialize();


    }
}
