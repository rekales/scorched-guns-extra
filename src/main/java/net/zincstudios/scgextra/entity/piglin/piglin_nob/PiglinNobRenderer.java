package net.zincstudios.scgextra.entity.piglin.piglin_nob;

import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.HeldGunFlashGeoLayer;
import net.zincstudios.scgextra.entity.common.client.HeldItemGeoLayer;
import net.zincstudios.scgextra.item.ModItems;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class PiglinNobRenderer <T extends Mob & GeoEntity> extends BaseEntityRenderer<T> {

    public PiglinNobRenderer(Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new HeldItemGeoLayer<>(this){
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if(!stack.is(ModItems.NETHERITE_CHAIN_AXE.get())){
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                    Minecraft.getInstance().getItemRenderer().renderStatic(animatable, stack,
                            getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, animatable.level(),
                            packedLight, packedOverlay, animatable.getId());
                    poseStack.popPose();
                }else{
                    super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                }
            }
        });
        this.addRenderLayer(new HeldGunFlashGeoLayer<>(this, 0));
    }
}