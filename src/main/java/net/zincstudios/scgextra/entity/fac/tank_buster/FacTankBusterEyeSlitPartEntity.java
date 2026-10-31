package net.zincstudios.scgextra.entity.fac.tank_buster;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.common.part.RotatedWeakPointPartEntity;

import javax.annotation.ParametersAreNonnullByDefault;

// Weakpoint part that also damage mult
@ParametersAreNonnullByDefault
public class FacTankBusterEyeSlitPartEntity<T extends FacTankBusterEntity> extends RotatedWeakPointPartEntity<T> {

    public FacTankBusterEyeSlitPartEntity(T parent, Vec3 offset, float width, float height) {
        super(parent, offset, width, height);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.getParent().hurt(source, amount * 0.5f);
    }

}
