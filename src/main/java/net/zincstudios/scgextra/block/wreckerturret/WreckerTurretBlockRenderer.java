package net.zincstudios.scgextra.block.wreckerturret;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.zincstudios.scgextra.entity.common.client.MobRenderUtils;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class WreckerTurretBlockRenderer<T extends WreckerTurretBlockEntity> extends GeoBlockRenderer<T> {

    public WreckerTurretBlockRenderer(GeoModel<T> model) {
        super(model);

        this.addRenderLayer(new GeoRenderLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                               VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                String boneName;
                Level level = animatable.getLevel();
                if (level == null) return;

                // No need to loop, they're expected to run alternately
                if (animatable.leftFlashTick == level.getGameTime()) {
                    boneName = "left_flash";
                } else if (animatable.rightFlashTick == level.getGameTime()) {
                    boneName = "right_flash";
                } else {
                    return;
                }

                RandomSource rand = RandomSource.create(level.getGameTime() * animatable.getBlockPos().asLong());
                rand.nextFloat();  // because it's the same value on first get

                poseStack.pushPose();
                if(animatable.clientTurretAim()!=null){
                    poseStack.mulPose(Axis.XP.rotation(animatable.clientTurretAim()[1]));
                    poseStack.mulPose(Axis.YP.rotation(animatable.clientTurretAim()[0]));
                }else{
                    poseStack.mulPose(Axis.XP.rotation(0));
                    poseStack.mulPose(Axis.YP.rotation(0));
                }

                Optional<GeoBone> bone = bakedModel.getBone(boneName);
                if (bone.isEmpty()) return;

                Gun.Display.Flash flash = ModItems.BIRDFEEDER.get().getGun().getDisplay().getFlash();
                if (flash == null) return;
                ResourceLocation flashTexture = ResourceLocation.fromNamespaceAndPath(ScorchedGuns.MODID,
                        "textures/effect/" + flash.getTextureLocation() + ".png");

                RenderUtils.translateAndRotateMatrixForBone(poseStack, bone.get());
                MobRenderUtils.renderMuzzleFlash(poseStack, renderType, bufferSource,
                        flashTexture, false, rand, 0.8F);
                poseStack.popPose();

                super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public boolean shouldRenderOffScreen(WreckerTurretBlockEntity blockEntity) {
        return true;
    }
}
