package net.zincstudios.scgextra.entity.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class TransformableItemHoldingMobRenderer <T extends Mob & GeoEntity> extends BaseEntityRenderer<T> {

    public TransformableItemHoldingMobRenderer(EntityRendererProvider.Context context, GeoModel<T> model, Vec3 translateVector, Vec3 rotateVector, Vec3 scaleVector) {
        super(context, model);
        this.addRenderLayer(new HeldItemGeoLayer<>(this) {
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(translateVector.x, translateVector.y, translateVector.z);
                poseStack.mulPose(Axis.XP.rotationDegrees((float)rotateVector.x));
                poseStack.mulPose(Axis.YP.rotationDegrees((float)rotateVector.y));
                poseStack.mulPose(Axis.ZP.rotationDegrees((float)rotateVector.z));
                poseStack.scale((float)scaleVector.x, (float)scaleVector.y, (float)scaleVector.z);
                Minecraft.getInstance().getItemRenderer().renderStatic(animatable, stack,
                        getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, animatable.level(),
                        packedLight, packedOverlay, animatable.getId());
                poseStack.popPose();
            }
        });
    }
}
