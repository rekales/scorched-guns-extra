package net.zincstudios.scgextra.entity.wreckers.wrecker_green;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.zincstudios.scgextra.entity.common.client.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WreckerGreenRenderer<T extends WreckerGreenEntity> extends BaseEntityRenderer<T> {

    public WreckerGreenRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new HeldGunGeoLayer<>(this));
        this.addRenderLayer(new HeldGunFlashGeoLayer<>(this));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this){
            @Override
            public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType emissiveRenderType = getRenderType(animatable);
                getRenderer().reRender(
                        bakedModel,
                        poseStack,
                        bufferSource,
                        animatable,
                        emissiveRenderType,
                        bufferSource.getBuffer(emissiveRenderType),
                        partialTick,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        1,
                        1,
                        1,
                        1
                );
            }
        });
    }

    // TODO: replace with animation controller
    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        this.model.getBone("rocket").ifPresent(bone -> bone.setHidden(entity.isRocketHidden()));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
