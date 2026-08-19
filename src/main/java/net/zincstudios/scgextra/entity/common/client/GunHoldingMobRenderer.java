package net.zincstudios.scgextra.entity.common.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.ParametersAreNonnullByDefault;

// intended to be non-extendable, extend base and add the layers there instead
@ParametersAreNonnullByDefault
public final class GunHoldingMobRenderer<T extends Mob & GeoEntity> extends BaseEntityRenderer<T> {

    public GunHoldingMobRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        this(renderManager, model, 0);
    }

    public GunHoldingMobRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, float gunTilt) {
        super(renderManager, model);
        this.addRenderLayer(new HeldGunGeoLayer<>(this, gunTilt));
        this.addRenderLayer(new HeldGunFlashGeoLayer<>(this, gunTilt));
    }
}