package net.zincstudios.scgextra.entity.common.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.GeoModel;

public class TransformableItemHoldingMobRenderer <T extends Mob & GeoEntity> extends BaseEntityRenderer<T> {

    public TransformableItemHoldingMobRenderer(EntityRendererProvider.Context context, GeoModel<T> model, Vec3 translateVector, Vec3 rotateVector, Vec3 scaleVector) {
        super(context, model);
        this.addRenderLayer(new TransformableHeldItemGeoLayer<>(this, translateVector, rotateVector, scaleVector));
    }
}
