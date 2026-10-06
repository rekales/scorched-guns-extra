package net.zincstudios.scgextra.entity.whaler.salmonsaur;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.HeldItemGeoLayer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class SalmonsaurRenderer<T extends SalmonsaurEntity> extends BaseEntityRenderer<T> {

    public SalmonsaurRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        // custom adjustment for trident to be slightly upward
        this.addRenderLayer(new HeldItemGeoLayer<>(this) {
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(0.5F, -1F, -0.5F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                Minecraft.getInstance().getItemRenderer().renderStatic(animatable, stack,
                        getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, animatable.level(),
                        packedLight, packedOverlay, animatable.getId());
                poseStack.popPose();
            }
        });
        this.shadowRadius = 1F;
    }


}
