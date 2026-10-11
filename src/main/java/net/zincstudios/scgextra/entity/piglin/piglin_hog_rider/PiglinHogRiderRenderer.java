package net.zincstudios.scgextra.entity.piglin.piglin_hog_rider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class PiglinHogRiderRenderer <T extends PiglinHogRiderEntity> extends BaseEntityRenderer<T>{

    public static final Map<Integer, List<String>> toHide = new HashMap<>(
        Map.of(
            1, List.of("horn_1","shoulder_plate_3","shoulder_plate_1"),
            2, List.of("horn_2","shoulder_plate_4","shoulder_plate_2")
        )
    );

    public PiglinHogRiderRenderer(Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if(!toHide.get(animatable.getVariant()).contains(bone.getName())){
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
