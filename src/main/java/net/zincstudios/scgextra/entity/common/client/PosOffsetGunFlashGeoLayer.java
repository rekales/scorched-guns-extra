package net.zincstudios.scgextra.entity.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunModifierHelper;

// TODO: change cog juggernaut flash to use this
public abstract class PosOffsetGunFlashGeoLayer<T extends LivingEntity & GeoEntity> extends GeoRenderLayer<T> {

    public PosOffsetGunFlashGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    public abstract Vec3 getPos();

    public static <T extends LivingEntity & GeoEntity> PosOffsetGunFlashGeoLayer<T> fixedPos(GeoRenderer<T> renderer, Vec3 pos) {
        return new PosOffsetGunFlashGeoLayer<>(renderer) {
            @Override
            public Vec3 getPos() {
                return pos;
            }
        };
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!GunFlashHandler.hasFlashToRender(animatable.getId())) return;

        ItemStack stack = animatable.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gunItem)) return;
        Gun gun = gunItem.getModifiedGun(stack);
        Gun.Display.Flash flash = gun.getDisplay().getFlash();
        if (flash == null) return;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - Mth.lerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)));
        Vec3 flashPosition = getPos();

        RandomSource rand = RandomSource.create(animatable.level().getGameTime());
        flashPosition = flashPosition.add((rand.nextFloat()-0.5)*0.07, (rand.nextFloat()-0.5)*0.07, 0);
        poseStack.translate(flashPosition.x, flashPosition.y, flashPosition.z);
        float scale = (float)(PropertyHelper.getMuzzleFlashScale(stack, gun).x * GunModifierHelper.getMuzzleFlashScale(stack, 1.0));

        ResourceLocation flashTexture = ResourceLocation.fromNamespaceAndPath(ScorchedGuns.MODID,
                "textures/effect/" + flash.getTextureLocation() + ".png");
        MobRenderUtils.renderMuzzleFlash(poseStack, renderType, bufferSource,
                flashTexture, stack.isEnchanted(), rand, scale);
        poseStack.popPose();
    }
}
