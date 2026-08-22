package net.zincstudios.scgextra.entity.whaler.fishfolk;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class FishFolkModel<T extends FishFolkEntity> extends DefaultedEntityGeoModel<T> {

    public FishFolkModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return super.getTextureResource(animatable)
                .withPath(s -> s.substring(0, s.length() - 4) +  "_" + animatable.getVariant() + ".png");
    }
}