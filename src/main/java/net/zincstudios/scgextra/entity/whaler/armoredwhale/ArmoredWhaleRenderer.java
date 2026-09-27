package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import java.util.Map;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.model.GeoModel;

public class ArmoredWhaleRenderer <T extends ArmoredWhaleEntity> extends BaseEntityRenderer<T>{

    public ArmoredWhaleRenderer(Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new ArmoredWhaleBoneGunFlashGeoLayer<>(this, Map.of(
                0, "left_flash",
                1, "right_flash"
        )));
    }
    
}
