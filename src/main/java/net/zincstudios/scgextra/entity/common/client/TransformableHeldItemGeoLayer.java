package net.zincstudios.scgextra.entity.common.client;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class TransformableHeldItemGeoLayer<T extends Mob & GeoAnimatable> extends BlockAndItemGeoLayer<T> {

    private GeoBone cachedLeftHand = null;
    private GeoBone cachedRightHand = null;
    private final Vec3 traslateVec;
    private final Vec3 rotateVec;
    private final Vec3 scaleVec;

    public TransformableHeldItemGeoLayer(GeoRenderer<T> renderer, Vec3 traslateVec, Vec3 rotateVec, Vec3 scaleVec) {
        super(renderer);
        this.traslateVec = traslateVec;
        this.rotateVec = rotateVec;
        this.scaleVec = scaleVec;
    }

    // NOTE: could do with better caching but good enough for now.
    @Override
    protected @Nullable ItemStack getStackForBone(GeoBone bone, T animatable) {
        if (this.cachedLeftHand == null && bone.getName().equals("left_hand"))
            this.cachedLeftHand = bone;
        if (this.cachedRightHand == null && bone.getName().equals("right_hand"))
            this.cachedRightHand = bone;

        if (!animatable.isLeftHanded()) {
            if (bone == this.cachedRightHand) {
                return animatable.getMainHandItem();
            } else if (bone == this.cachedLeftHand){
                return animatable.getOffhandItem();
            }
        } else {
            if (bone == this.cachedLeftHand) {
                return animatable.getMainHandItem();
            } else if (bone == this.cachedRightHand){
                return animatable.getOffhandItem();
            }
        }

        return null;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(traslateVec.x, traslateVec.y, traslateVec.z);
        poseStack.mulPose(Axis.XP.rotationDegrees((float)rotateVec.x));
        poseStack.mulPose(Axis.YP.rotationDegrees((float)rotateVec.y));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float)rotateVec.z));
        poseStack.scale((float)scaleVec.x, (float)scaleVec.y, (float)scaleVec.z);
        Minecraft.getInstance().getItemRenderer().renderStatic(animatable, stack,
                getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, animatable.level(),
                packedLight, packedOverlay, animatable.getId());
        poseStack.popPose();
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
        return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}