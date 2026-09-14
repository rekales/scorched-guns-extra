package net.zincstudios.scgextra.entity.projectile.ink_projectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.whaler.tentacliator.TentacliatorEntity;

public class InkProjectileRenderer extends ArrowRenderer<InkProjectileEntity>{
    ResourceLocation TEXTURE_0 = SCGExtra.asResource("textures/entity/projectile/ink_projectile_0.png");
    ResourceLocation TEXTURE_1 = SCGExtra.asResource("textures/entity/projectile/ink_projectile_1.png");
    private final InkProjectileEntityModel model;
    public InkProjectileRenderer(Context ctx) {
        super(ctx);
        this.model = new InkProjectileEntityModel(ctx.bakeLayer(InkProjectileEntityModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(InkProjectileEntity entity) {
        if(entity.getOwner() instanceof TentacliatorEntity te){
            if(te.isGlowing()){
                return TEXTURE_1;
            }
            else{
                return TEXTURE_0;
            }
        }
        return TEXTURE_0;
    }
    @Override
    public void render(InkProjectileEntity entity, float yaw, float partialTicks,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        
        model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);

        pose.popPose();
    }
}
