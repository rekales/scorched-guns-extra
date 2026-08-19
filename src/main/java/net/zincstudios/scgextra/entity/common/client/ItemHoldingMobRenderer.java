package net.zincstudios.scgextra.entity.common.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.ParametersAreNonnullByDefault;

// intended to be non-extendable, extend base and add the layers there instead
@ParametersAreNonnullByDefault
public final class ItemHoldingMobRenderer <T extends Mob & GeoEntity> extends BaseEntityRenderer<T> {

    public ItemHoldingMobRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new HeldItemGeoLayer<>(this));
    }
}