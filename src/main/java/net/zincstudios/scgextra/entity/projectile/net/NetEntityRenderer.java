package net.zincstudios.scgextra.entity.projectile.net;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

public class NetEntityRenderer extends GeoEntityRenderer<NetEntity>{
    public NetEntityRenderer(Context renderManager, GeoModel<NetEntity> model) {
        super(renderManager, model);
    }

    @Override
    public void render(NetEntity entity, float yaw, float partialTicks,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();

        pose.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        pose.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) - 10));

        pose.mulPose(Axis.XP.rotationDegrees(90));

        pose.scale(2F, 2F, 2F);

        pose.popPose();

        super.render(entity, yaw, partialTicks, pose, buffer, light);
    }
}
