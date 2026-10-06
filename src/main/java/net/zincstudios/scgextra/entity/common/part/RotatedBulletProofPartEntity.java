package net.zincstudios.scgextra.entity.common.part;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RotatedBulletProofPartEntity<T extends LivingEntity> extends RotatedSegmentPartEntity<T> {
    public RotatedBulletProofPartEntity(T parent, Vec3 offset, float width, float height) {
        super(parent, offset, width, height);
    }

    public RotatedBulletProofPartEntity(T parent, Vec3 offset, float width, float height, boolean collision) {
        super(parent, offset, width, height, collision);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        this.playSound(SoundEvents.ANVIL_LAND, 0.65F, 1.5F + this.level().random.nextFloat() * 0.4F);
        return false;
    }
}
