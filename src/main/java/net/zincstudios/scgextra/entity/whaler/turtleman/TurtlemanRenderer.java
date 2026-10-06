package net.zincstudios.scgextra.entity.whaler.turtleman;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.common.client.*;
import software.bernie.geckolib.model.GeoModel;

public class TurtlemanRenderer<T extends TurtlemanEntity> extends BaseEntityRenderer<T> {

    public TurtlemanRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.addRenderLayer(new HeldGunGeoLayer<>(this, 3));
        this.addRenderLayer(PosOffsetGunFlashGeoLayer.fixedPos(this, new Vec3(0.9, 0.8, -1.9)));
    }
}
