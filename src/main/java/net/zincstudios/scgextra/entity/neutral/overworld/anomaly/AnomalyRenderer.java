package net.zincstudios.scgextra.entity.neutral.overworld.anomaly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.model.GeoModel;

public class AnomalyRenderer<T extends AnomalyEntity> extends BaseEntityRenderer<T>{

    public AnomalyRenderer(Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        renderLaserBeam(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    public static final ResourceLocation BEACON_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    private void renderLaserBeam(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0,entity.getEyeHeight()+0.15,0);
        Vec3 eyePosOffset = new Vec3(0, 0, 0.5).yRot((-entityYaw + 720)%360 * Mth.DEG_TO_RAD);
        poseStack.translate(eyePosOffset.x, eyePosOffset.y, eyePosOffset.z);

        Vec3 sourceVec = new Vec3(0,entity.getEyeHeight()+0.15,0.5)
                .yRot((-entityYaw + 720)%360 * Mth.DEG_TO_RAD);
        sourceVec = sourceVec.add(entity.position());

        Vec3 targetVec = entity.getBeamLoc();  // TODO: lerp

        if(targetVec.equals(Vec3.ZERO)){
            poseStack.popPose();
            return;
        }

        Vec3 direction = targetVec.subtract(sourceVec);
        double distance = direction.length();
        direction = direction.normalize();

        poseStack.mulPose(Axis.YP.rotation((float)Math.atan2(direction.x, direction.z)));
        poseStack.mulPose(Axis.XP.rotation((float)Math.asin(-direction.y) + Mth.PI/2));

        // Compensate for renderBeaconBeam() translating the goddamn thing by 0.5,0,0.5
        poseStack.translate(-0.5,0,-0.5);

        float partialTimer = 37-partialTicks;

        float outerRadiusMult = 0;
        if (85 >= partialTimer) {
            outerRadiusMult = Math.min((85 - partialTimer) / 35, 1);
        }

        float radiusMult = 0;
        if (partialTimer <= 15) {
            radiusMult = partialTimer/15F;
            outerRadiusMult = radiusMult;
        } else if (partialTimer <= 37) {
            radiusMult = 1;
        } else if (partialTimer <= 40) {
            radiusMult = (41-partialTimer)/4F;
        }

        BeaconRenderer.renderBeaconBeam(
                poseStack,
                buffer,
                BEACON_BEAM_LOCATION,
                partialTicks,
                1.0F, // beam height scale
                entity.level().getGameTime(),
                0,
                (int)Math.ceil(distance),
                new float[]{1F, 1F, 1F}, // RGB color
                0.4F * radiusMult, // inner radius
                0.43F * outerRadiusMult // outer radius
        );

        poseStack.popPose();
    }
    
}
