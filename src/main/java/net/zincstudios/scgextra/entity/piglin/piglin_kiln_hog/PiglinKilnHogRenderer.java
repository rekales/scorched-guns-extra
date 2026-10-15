package net.zincstudios.scgextra.entity.piglin.piglin_kiln_hog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class PiglinKilnHogRenderer <T extends PiglinKilnHogEntity> extends BaseEntityRenderer<T>{

    public PiglinKilnHogRenderer(Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
            MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if(bone.getName().contains("bone")){
            if(bone.getName().equals("bone"+animatable.getLavaPhase())){
                super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick,
                        packedLight, packedOverlay, red, green, blue, alpha);
            }
        }else{
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick,
                        packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
