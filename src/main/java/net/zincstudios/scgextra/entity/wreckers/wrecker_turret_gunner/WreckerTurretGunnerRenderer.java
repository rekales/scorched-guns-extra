package net.zincstudios.scgextra.entity.wreckers.wrecker_turret_gunner;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.model.GeoModel;

public class WreckerTurretGunnerRenderer<T extends WreckerTurretGunnerEntity> extends BaseEntityRenderer<T> {
    private static final double PILLAR_OFFSET_Z = 12.5D / 16D;

    public WreckerTurretGunnerRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
        poseStack.translate(0.0D, 0.0D, PILLAR_OFFSET_Z);
    }
}
