package net.zincstudios.scgextra.entity.neutral.end.end_pod;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EndPodRenderer<T extends EndPodEntity> extends BaseEntityRenderer<T> {

    public EndPodRenderer(Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
