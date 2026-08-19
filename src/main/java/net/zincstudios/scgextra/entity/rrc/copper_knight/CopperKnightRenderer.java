package net.zincstudios.scgextra.entity.rrc.copper_knight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.GunGeoLayer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class CopperKnightRenderer<T extends CopperKnightEntity> extends BaseEntityRenderer<T> {

    public CopperKnightRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);

        // TODO: replace with pre-built held gun renderer
        addRenderLayer(new GunGeoLayer<>(this) {
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                if (animatable.isAiming()) {
                    poseStack.translate(-0.2, -0.3, -0.2);
                    poseStack.mulPose(Axis.XP.rotationDegrees(135));
                    poseStack.mulPose(Axis.YP.rotationDegrees(170));
                } else {
                    poseStack.translate(-0.45, -0.3, -0.35);
                    poseStack.mulPose(Axis.XP.rotationDegrees(145));
                    poseStack.mulPose(Axis.YP.rotationDegrees(125));
                }
                poseStack.mulPose(Axis.XP.rotationDegrees(90)); // Compensate for super
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                poseStack.popPose();
            }
        });
    }
}
