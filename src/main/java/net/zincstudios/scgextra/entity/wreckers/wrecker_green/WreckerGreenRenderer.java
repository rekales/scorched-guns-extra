package net.zincstudios.scgextra.entity.wreckers.wrecker_green;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.zincstudios.scgextra.entity.common.client.*;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WreckerGreenRenderer<T extends WreckerGreenEntity> extends BaseEntityRenderer<T> {

    public WreckerGreenRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new HeldGunGeoLayer<>(this));
        this.addRenderLayer(new HeldGunFlashGeoLayer<>(this));
    }

    // TODO: replace with animation controller
    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        this.model.getBone("rocket").ifPresent(bone -> bone.setHidden(entity.isRocketHidden()));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
