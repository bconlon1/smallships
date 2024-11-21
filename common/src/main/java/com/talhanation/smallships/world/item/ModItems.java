package com.talhanation.smallships.world.item;

import com.talhanation.smallships.world.entity.ship.BriggEntity;
import com.talhanation.smallships.world.entity.ship.CogEntity;
import com.talhanation.smallships.world.entity.ship.DrakkarEntity;
import com.talhanation.smallships.world.entity.ship.GalleyEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ModItems {
    public static final Item SAIL = getItem("sail");

    public static final Item CANNON = getItem("cannon");
    public static final CannonBallItem CANNON_BALL = (CannonBallItem) getItem("cannon_ball");

    public static final Map<Boat.Type, Item> COG_ITEMS = new HashMap<>(Boat.Type.values().length);
    public static final Map<Boat.Type, Item> BRIGG_ITEMS = new HashMap<>(Boat.Type.values().length);
    public static final Map<Boat.Type, Item> GALLEY_ITEMS = new HashMap<>(Boat.Type.values().length);
    public static final Map<Boat.Type, Item> DRAKKAR_ITEMS = new HashMap<>(Boat.Type.values().length);

    static {
        Boat.Type[] boatTypes = Boat.Type.values();
        for (Boat.Type type : boatTypes) {
            String name = type.getName().replaceAll("[^a-z0-9_.-]", "_");
            COG_ITEMS.put(type, getItem(name + "_" + CogEntity.ID));
            BRIGG_ITEMS.put(type, getItem(name + "_" + BriggEntity.ID));
            GALLEY_ITEMS.put(type, getItem(name + "_" + GalleyEntity.ID));
            DRAKKAR_ITEMS.put(type, getItem(name + "_" + DrakkarEntity.ID));
        }
    }

    @ExpectPlatform
    public static Item getItem(String id) {
        throw new AssertionError();
    }
}
