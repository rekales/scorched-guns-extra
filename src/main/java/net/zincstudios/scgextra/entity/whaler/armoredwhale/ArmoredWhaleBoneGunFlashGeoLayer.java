package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.zincstudios.scgextra.entity.common.client.GunFlashHandler;
import net.zincstudios.scgextra.entity.common.client.MobRenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

//COPIED FROM BoneGunFlashGeoLayer
public class ArmoredWhaleBoneGunFlashGeoLayer<T extends ArmoredWhaleEntity & GeoEntity> extends GeoRenderLayer<T> {

    private final Map<Integer, String> flashBones;

    public ArmoredWhaleBoneGunFlashGeoLayer(GeoRenderer<T> renderer, Map<Integer, String> flashBones) {
        super(renderer);
        this.flashBones = flashBones;
    }

    public ArmoredWhaleBoneGunFlashGeoLayer(GeoRenderer<T> renderer, String boneName) {
        super(renderer);
        this.flashBones = Map.of(0, boneName);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Set<GunFlashHandler.FlashData> flashes = GunFlashHandler.getFlashesToRender(animatable.getId());
        if (flashes.isEmpty()) return;

        RandomSource rand = RandomSource.create(animatable.level().getGameTime() * animatable.getId());
        rand.nextFloat();

        for(GunFlashHandler.FlashData flashData : flashes) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180 - Mth.lerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)));
            if (!this.flashBones.containsKey(flashData.posIndex())) continue;
            Optional<GeoBone> bone = bakedModel.getBone(animatable.isLeft() ? flashBones.get(0) : flashBones.get(1));
            if (bone.isEmpty()) continue;

            RenderUtils.translateAndRotateMatrixForBone(poseStack, bone.get());
            MobRenderUtils.renderMuzzleFlash(poseStack, renderType, bufferSource,
                    flashData.flashLoc(), flashData.enchanted(), rand, flashData.scale());
            poseStack.popPose();
        }

        super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
