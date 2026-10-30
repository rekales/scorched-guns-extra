package net.zincstudios.scgextra.item.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.zincstudios.scgextra.item.ModItems;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BaseArmorSetPartRenderer extends GeoArmorRenderer<GeoArmorSetPartItem> {

    public BaseArmorSetPartRenderer(GeoModel<GeoArmorSetPartItem> model) {
        super(model);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this){
            @Override
            public void render(PoseStack poseStack, GeoArmorSetPartItem animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType emissiveRenderType = getRenderType(animatable);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType),
                        partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1
                );
            }
        });
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void hideHeadWithCommissarHelmet(RenderPlayerEvent.Pre event) {
        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        if (event.getEntity().getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.COMMISSAR_HELMET.get()) {
            model.head.visible = false;
        }
    }
}
