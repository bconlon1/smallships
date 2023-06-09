package com.talhanation.smallships.world.item.fabric;

import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.config.SmallShipsConfig;
import com.talhanation.smallships.world.entity.ship.BriggEntity;
import com.talhanation.smallships.world.entity.ship.CogEntity;
import com.talhanation.smallships.world.entity.ship.GalleyEntity;
import com.talhanation.smallships.world.item.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;

import java.util.HashMap;
import java.util.Map;

public class ModItemsImpl {
    private static final Map<String, Item> entries = new HashMap<>();

    public static Item getItem(String id) {
        return entries.get(id);
    }

    static {
        if (SmallShipsConfig.Common.smallshipsItemGroupEnable.get()) {
            ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(SmallShipsMod.MOD_ID, "smallships"));

            CreativeModeTab custom = FabricItemGroup.builder()
                    .title(Component.translatable(creativeModeTab.location().toString().replace(":", ".")))
                    .icon(() -> new ItemStack(ModItems.CANNON))
                    .displayItems((itemDisplayParameters, output) -> itemDisplayParameters.holders()
                            .lookup(Registries.ITEM)
                            .ifPresent(registryLookup -> registryLookup.listElementIds()
                                    .filter(itemResourceKey -> SmallShipsMod.MOD_ID.equals(itemResourceKey.location().getNamespace()))
                                    .forEach(itemResourceKey -> output.accept(BuiltInRegistries.ITEM.get(itemResourceKey)))
                            ))
                    .build();

            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, creativeModeTab, custom);
        } else {
            CreativeModeTab colored_blocks = FabricItemGroup.builder()
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModItems.SAIL);
                    }))
                    .build();
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CreativeModeTabs.COLORED_BLOCKS, colored_blocks);

            CreativeModeTab combat = FabricItemGroup.builder()
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModItems.CANNON);
                        output.accept(ModItems.CANNON_BALL);
                    }))
                    .build();
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CreativeModeTabs.COMBAT, combat);

            CreativeModeTab tools_and_utilities = FabricItemGroup.builder()
                    .displayItems(((itemDisplayParameters, output) -> {
                        for (Boat.Type type : Boat.Type.values()) {
                            output.accept(ModItems.COG_ITEMS.get(type));
                            output.accept(ModItems.BRIGG_ITEMS.get(type));
                            output.accept(ModItems.GALLEY_ITEMS.get(type));
                        }
                    }))
                    .build();
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CreativeModeTabs.TOOLS_AND_UTILITIES, tools_and_utilities);
        }

        register("sail", new SailItem((new Item.Properties()).stacksTo(16)));

        register("cannon", new CannonItem((new Item.Properties()).stacksTo(1)));
        register("cannon_ball", new CannonBallItem((new Item.Properties()).stacksTo(16)));

        for (Boat.Type type: Boat.Type.values()) {
            register(new ResourceLocation(type.getName()).getPath() + "_" + CogEntity.ID,  new CogItem(type, new Item.Properties().stacksTo(1)));
            register(new ResourceLocation(type.getName()).getPath() + "_" + BriggEntity.ID,  new BriggItem(type, new Item.Properties().stacksTo(1)));
            register(new ResourceLocation(type.getName()).getPath() + "_" + GalleyEntity.ID,  new GalleyItem(type, new Item.Properties().stacksTo(1)));
        }
    }

    private static void register(String id, Item item) {
        entries.put(id, register(new ResourceLocation(SmallShipsMod.MOD_ID, id), item));
    }

    private static Item register(ResourceLocation id, Item item) {
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
        }

        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }
}
