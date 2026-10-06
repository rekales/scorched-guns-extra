package net.zincstudios.scgextra.entity.projectile.ink_projectile;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class InkProjectileModel <T extends InkProjectileEntity> extends DefaultedEntityGeoModel<T> {
    
    public InkProjectileModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return super.getTextureResource(animatable)
                .withPath(s -> s.substring(0, s.length() - 4) +  "_" + animatable.getVariant() + ".png");
    }
}
