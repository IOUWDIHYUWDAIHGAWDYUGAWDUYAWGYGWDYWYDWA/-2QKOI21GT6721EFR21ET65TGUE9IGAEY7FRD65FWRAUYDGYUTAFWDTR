package com.reallifeearth.client;

import com.reallifeearth.RealLifeEarthMod;
import com.reallifeearth.client.gui.RealEarthCreationScreen;
import com.reallifeearth.entity.npc.RealNpcEntity;
import com.reallifeearth.registry.ModEntities;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = RealLifeEarthMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent e) {
        e.put(ModEntities.REAL_NPC.get(), RealNpcEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.REAL_NPC.get(), ClientEvents::createNpcRenderer);
    }

    private static net.minecraft.client.renderer.entity.EntityRenderer<RealNpcEntity> createNpcRenderer(EntityRendererProvider.Context ctx) {
        return new HumanoidMobRenderer<>(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f) {
            @Override
            public ResourceLocation getTextureLocation(RealNpcEntity entity) {
                int v = entity.getSkinVariant() % 4;
                boolean male = "male".equals(entity.getGender());
                String name = "textures/entity/npc/" + (male ? "male_" : "female_") + v + ".png";
                return ResourceLocation.fromNamespaceAndPath(RealLifeEarthMod.MODID, name);
            }
        };
    }
}
