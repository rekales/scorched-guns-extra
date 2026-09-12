package net.zincstudios.scgextra.entity.cog.trenchbleeder;

import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.BoneGunFlashGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;

public class CogTrenchbleederRenderer extends BaseEntityRenderer<CogTrenchbleederEntity> {
    public CogTrenchbleederRenderer(EntityRendererProvider.Context context, GeoModel<CogTrenchbleederEntity> model) {
        super(context, model);
        this.addRenderLayer(new BoneGunFlashGeoLayer<>(this, "barrel_end"));
    }
}
