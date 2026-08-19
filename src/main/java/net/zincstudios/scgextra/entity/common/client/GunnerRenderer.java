package net.zincstudios.scgextra.entity.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import javax.annotation.ParametersAreNonnullByDefault;

@Deprecated
@ParametersAreNonnullByDefault
public class GunnerRenderer <T extends GunnerEntity & GeoEntity> extends GeoEntityRenderer<T> {

    protected boolean noDeathTilt = false;
    protected boolean noDeathRedTint = false;  // NOTE: can't be assed to do it since nothing uses it yet.
    protected boolean hasCustomShadowRadius = false;

    public GunnerRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
        this.shadowRadius = 0;  // no way to get the entity type on construction

        addRenderLayer(new GunGeoLayer<>(this));
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

    public GunnerRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, float gunTilt) {
        super(renderManager, model);
        this.shadowRadius = 0;

        addRenderLayer(new GunGeoLayer<>(this, gunTilt));
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

    /**
     * Constructor for extending and adding a custom gun render layer
     */
    protected GunnerRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, boolean ignored) {
        super(renderManager, model);
        this.shadowRadius = 0;
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

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!this.hasCustomShadowRadius && this.shadowRadius == 0) {
            this.shadowRadius = entity.getBbWidth()/2;
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    protected float getDeathMaxRotation(T animatable) {
        if (this.noDeathTilt) return 0;
        return 90f;
    }


    // Factory methods

    public GunnerRenderer<T> noDeathTilt() {
        this.noDeathTilt = true;
        return this;
    }

    public GunnerRenderer<T> noDeathRedTint() {
        this.noDeathRedTint = true;
        return this;
    }

    public GunnerRenderer<T> customShadowRadius(float shadowRadius) {
        this.hasCustomShadowRadius = true;
        this.shadowRadius = shadowRadius;
        return this;
    }
}
