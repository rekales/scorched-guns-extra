package net.zincstudios.scgextra.entity.piglin.piglin_hog_rider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.zincstudios.scgextra.entity.common.client.HeldItemGeoLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.item.ItemStack;
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
        this.addRenderLayer(new HeldItemGeoLayer<>(this){
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(0, -0.5F, 0.2125F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                Minecraft.getInstance().getItemRenderer().renderStatic(animatable, stack,
                        getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, animatable.level(),
                        packedLight, packedOverlay, animatable.getId());
                poseStack.popPose();
            }
        });
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if(animatable.level()==null || animatable.tickCount == 0){
            if(!toHide.get(1).contains(bone.getName())){
                super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            }
        }else{
            if(!toHide.get(animatable.getVariant()).contains(bone.getName())){
                super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
    }
}