package net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter;

import net.zincstudios.scgextra.entity.common.client.BoneGunFlashGeoLayer;

import java.util.Map;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import software.bernie.geckolib.model.GeoModel;

public class PiglinBlazeKopterRenderer<T extends PiglinBlazeKopterEntity> extends BaseEntityRenderer<T>{

    public PiglinBlazeKopterRenderer(Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new BoneGunFlashGeoLayer<>(this, Map.of(
                0, "flash",
                1, "flash"
        )));
    }
    
}