package net.zincstudios.scgextra.entity.rrc.scrapguard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.GunGeoLayer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class ScrapGuardRenderer<T extends ScrapGuardEntity> extends BaseEntityRenderer<T> {

    public ScrapGuardRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);

        // TODO: replace with pre-built held gun renderer
        addRenderLayer(new GunGeoLayer<>(this) {
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(0, -0.1, 0.1);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                
                //copied from krei's code
                poseStack.mulPose(Axis.XP.rotationDegrees(90)); // Compensate for super
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                poseStack.popPose();
            }
        });
    }
}
