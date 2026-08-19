package net.zincstudios.scgextra.entity.asgharian.worker;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class AsgharWorkerModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {

    public AsgharWorkerModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
