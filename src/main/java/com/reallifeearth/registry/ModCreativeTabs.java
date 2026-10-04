package com.reallifeearth.registry;

import com.reallifeearth.RealLifeEarthMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RealLifeEarthMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.reallifeearth"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.PHONE.get().getDefaultInstance())
            .displayItems((p, out) -> {
                out.accept(ModItems.PHONE.get());
                out.accept(ModItems.BANK_CARD.get());
                out.accept(ModItems.WALLET.get());
                out.accept(ModItems.CAR_KEY.get());
                out.accept(ModItems.ASPHALT_ITEM.get());
                out.accept(ModItems.SIDEWALK_ITEM.get());
                out.accept(ModItems.SHOP_SHELF_ITEM.get());
                out.accept(ModItems.OFFICE_DESK_ITEM.get());
                var spawnEgg = ModEntities.NPC_SPAWN_EGG;
                if (spawnEgg != null) {
                    try { out.accept(spawnEgg.get()); } catch (Exception ignored) {}
                }
            }).build());
}
