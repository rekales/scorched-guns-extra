package net.zincstudios.scgextra.entity.whaler.tentacliator;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class TentacliatorModel<T extends TentacliatorEntity> extends DefaultedEntityGeoModel<T> {

    public TentacliatorModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return super.getTextureResource(animatable)
                .withPath(s -> s.substring(0, s.length() - 4) +  "_" + animatable.getVariant() + ".png");
    }
}