package net.zincstudios.scgextra.entity.common.brain;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import java.util.List;

public class SlamAttack extends DelayedMeleeAttack {

    public SlamAttack(int damageDelay, int meleeDuration, float range, int meleeCooldown) {
        super(damageDelay, meleeDuration, range, meleeCooldown);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        if (entity.getBrain().getMemory(ModBrainMemories.DELAYED_MELEE.get()).get() == gameTime) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                    entity.getBoundingBox().inflate(this.range * 1.3),
                    target -> entity != target
            );

            for (LivingEntity target : entities) {
                entity.doHurtTarget(target);
                double dx = entity.getX() - target.getX();
                double dz = entity.getZ() - target.getZ();
                target.knockback(2, dx, dz);
            }
        }
    }
}
