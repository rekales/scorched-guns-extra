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

public class TransformableGunHoldingMobRenderer<T extends Mob & GeoEntity> extends BaseEntityRenderer<T> {

    public TransformableGunHoldingMobRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        this(renderManager, model, 0, Vec3.ZERO, Vec3.ZERO);
    }

    public TransformableGunHoldingMobRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, float gunTilt, Vec3 translateVector, Vec3 scaleVector) {
        super(renderManager, model);
        this.addRenderLayer(new HeldGunGeoLayer<>(this, gunTilt){
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable,
                    MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(translateVector.x, translateVector.y, translateVector.z);
                poseStack.scale((float)scaleVector.x, (float)scaleVector.y, (float)scaleVector.z);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90 + gunTilt));
                Minecraft.getInstance().getItemRenderer().renderStatic(animatable, stack,
                    getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, animatable.level(),
                    packedLight, packedOverlay, animatable.getId());
                poseStack.popPose();
            }
        });
        this.addRenderLayer(new HeldGunFlashGeoLayer<>(this, gunTilt));
    }
}