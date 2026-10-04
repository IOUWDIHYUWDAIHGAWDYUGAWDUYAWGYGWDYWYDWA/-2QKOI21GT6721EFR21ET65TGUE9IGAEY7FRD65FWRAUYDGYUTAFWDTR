package com.reallifeearth.registry;

import com.reallifeearth.RealLifeEarthMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RealLifeEarthMod.MODID);

    public static final DeferredItem<BlockItem> ASPHALT_ITEM = ITEMS.registerSimpleBlockItem("asphalt", ModBlocks.ASPHALT);
    public static final DeferredItem<BlockItem> ASPHALT_LINE_ITEM = ITEMS.registerSimpleBlockItem("asphalt_line", ModBlocks.ASPHALT_LINE);
    public static final DeferredItem<BlockItem> SIDEWALK_ITEM = ITEMS.registerSimpleBlockItem("sidewalk", ModBlocks.SIDEWALK);
    public static final DeferredItem<BlockItem> SHOP_SHELF_ITEM = ITEMS.registerSimpleBlockItem("shop_shelf", ModBlocks.SHOP_SHELF);
    public static final DeferredItem<BlockItem> OFFICE_DESK_ITEM = ITEMS.registerSimpleBlockItem("office_desk", ModBlocks.OFFICE_DESK);

    public static final DeferredItem<Item> PHONE = ITEMS.registerSimpleItem("phone", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BANK_CARD = ITEMS.registerSimpleItem("bank_card", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> WALLET = ITEMS.registerSimpleItem("wallet", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> CAR_KEY = ITEMS.registerSimpleItem("car_key", new Item.Properties().stacksTo(1));
}
