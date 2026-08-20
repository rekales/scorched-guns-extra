package net.zincstudios.scgextra.entity.neutral.end.end_dweller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EndDwellerRenderer<T extends EndDwellerEntity> extends BaseEntityRenderer<T> {

    public EndDwellerRenderer(Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this){
            @Override
            public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType emissiveRenderType = getRenderType(animatable);
                if (animatable.isCharging()) {
                    getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType),
                            partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 0, 0, 1);
                } else {
                    getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType),
                            partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
                }
            }
        });
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
