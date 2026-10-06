package net.zincstudios.scgextra.entity.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

// Integrating bone checks and caching for performance
// Edit in case there's dual wielding mobs, or an alternate geo layer

/**
 * For integrating bone checks and caching for performance.
 * Renders the held item or left_hand or right_hand bones.
 * Checks Mob#isLefthanded if the item should be rendered to the left hand
 * <p>
 *
 * @param <T>
 */
public class HeldItemGeoLayer<T extends Mob & GeoAnimatable> extends BlockAndItemGeoLayer<T> {

    private GeoBone cachedLeftHand = null;
    private GeoBone cachedRightHand = null;

    public HeldItemGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
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
        if (stack.getItem() instanceof TieredItem) {
            poseStack.translate(0, 0.25F, -0.3125F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        } else if (stack.getItem() instanceof ShieldItem) {
            poseStack.translate(0, 1.25F, -1/16f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        } else if (stack.getItem() instanceof TridentItem) {
            poseStack.translate(0.5F, -1F, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }
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