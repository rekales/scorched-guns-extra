package net.zincstudios.scgextra.entity.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import javax.annotation.ParametersAreNonnullByDefault;

// Normally I don't like making base classes but having this utility outweighs it.
/**
 * Base renderer that provides automatic shadow radius and factory methods for customizability without needing to extend
 */
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class BaseEntityRenderer <T extends LivingEntity & GeoEntity> extends GeoEntityRenderer<T> {

    protected boolean noDeathTilt = false;
    protected boolean noDeathRedTint = false;  // NOTE: can't be assed to do it since nothing uses it yet.
    protected boolean hasCustomShadowRadius = false;

    public BaseEntityRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.shadowRadius = 0;  // no way to get the entity type on construction other than passing, maybe I should
    }

    public BaseEntityRenderer(EntityRendererProvider.Context context, ResourceLocation modelLoc) {
        this(context, new DefaultedEntityGeoModel<>(modelLoc));
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

    public BaseEntityRenderer<T> noDeathTilt() {
        this.noDeathTilt = true;
        return this;
    }

    public BaseEntityRenderer<T> noDeathRedTint() {
        this.noDeathRedTint = true;
        return this;
    }

    public BaseEntityRenderer<T> shadowRadius(float shadowRadius) {
        this.hasCustomShadowRadius = true;
        this.shadowRadius = shadowRadius;
        return this;
    }

    public BaseEntityRenderer<T> hasGlowLayer() {
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            // TODO: check difference with the super method
            @Override
            public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType emissiveRenderType = getRenderType(animatable);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType),
                        partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1
                );
            }
        });
        return this;
    }
}