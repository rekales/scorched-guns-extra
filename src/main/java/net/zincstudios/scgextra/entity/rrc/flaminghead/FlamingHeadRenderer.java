package net.zincstudios.scgextra.entity.rrc.flaminghead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class FlamingHeadRenderer<T extends FlamingHeadEntity> extends BaseEntityRenderer<T> {

    private static final String[] FLAMETHROWER_BONE_NAMES = {"nw_flame", "sw_flame", "ne_flame", "se_flame"};

    private final GeoBone[] flamethrowerBones = {null, null, null, null};

    public FlamingHeadRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this){
            @Override
            public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                RenderType emissiveRenderType = getRenderType(animatable);
                getRenderer().reRender(
                        bakedModel,
                        poseStack,
                        bufferSource,
                        animatable,
                        emissiveRenderType,
                        bufferSource.getBuffer(emissiveRenderType),
                        partialTick,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        1,
                        1,
                        1,
                        1
                );
            }
        });
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        updateFlamethrowerPosRot(entity);
    }

    private void updateFlamethrowerPosRot(T entity) {
        Vec3[] flamethrowerPos = {Vec3.ZERO,Vec3.ZERO,Vec3.ZERO,Vec3.ZERO};
        Vec3[] flamethrowerDir = {Vec3.ZERO,Vec3.ZERO,Vec3.ZERO,Vec3.ZERO};
        for (int i = 0; i < 4; i++) {
            if (this.flamethrowerBones[i] == null) {
                Optional<GeoBone> opt = this.getGeoModel().getBone(FLAMETHROWER_BONE_NAMES[i]);
                if (opt.isPresent()) {
                    this.flamethrowerBones[i] = opt.get();
                } else {
                    break;
                }
            }
            Vector3d pos = this.flamethrowerBones[i].getWorldPosition();

            Matrix4f worldMatrix = this.flamethrowerBones[i].getModelRotationMatrix();
            Vector4f up = new Vector4f(0, 1, 0, 0);
            worldMatrix.transform(up);

            flamethrowerPos[i] = new Vec3(pos.x, pos.y, pos.z);
            flamethrowerDir[i] = new Vec3(up.x, -up.y, up.z).normalize();
        }
        entity.setFlamethrowerPos(flamethrowerPos);
        entity.setFlamethrowerDir(flamethrowerDir);
    }
}
