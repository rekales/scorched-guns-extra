package net.zincstudios.scgextra.entity.projectile;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zincstudios.scgextra.entity.ModEntities;
import top.ribs.scguns.init.ModParticleTypes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoulFireball extends AbstractHurtingProjectile {

    private float explosionPower = 2.5f;

    public SoulFireball(EntityType<SoulFireball> entityType, Level level) {
        super(entityType, level);
    }

    public SoulFireball(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ, float explosionPower) {
        super(ModEntities.LARGE_SOUL_FIREBALL.get(), shooter, offsetX, offsetY, offsetZ, level);
        this.explosionPower = explosionPower;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            ClientLevel level = (ClientLevel) level();  // Dedicated Server doesn't like doing instanceof ClientLevel

            level.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.position().x + (level.getRandom().nextDouble() - 0.5) * 0.5,
                    this.position().y + (level.getRandom().nextDouble() - 0.5) * 0.5 + 0.5,
                    this.position().z + (level.getRandom().nextDouble() - 0.5) * 0.5,
                    (level.getRandom().nextDouble() - 0.5) * 0.05,
                    (level.getRandom().nextDouble() - 0.5) * 0.05,
                    (level.getRandom().nextDouble() - 0.5) * 0.05
            );

            if (this.tickCount % 3 == 0) {
                level.addParticle(
                        ModParticleTypes.SOUL_FIREBALL.get(),
                        this.position().x + (level.getRandom().nextDouble() - 0.5) * 0.5,
                        this.position().y + (level.getRandom().nextDouble() - 0.5) * 0.5 + 0.5,
                        this.position().z + (level.getRandom().nextDouble() - 0.5) * 0.5,
                        (level.getRandom().nextDouble() - 0.5) * 0.05,
                        (level.getRandom().nextDouble() - 0.5) * 0.05,
                        (level.getRandom().nextDouble() - 0.5) * 0.05
                );
            }
        }
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    protected void onHit(HitResult result) {
        super.onHit(result);
        this.spawnExplosionParticles();
        if (!this.level().isClientSide) {
            Explosion explosion = this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.explosionPower, false, Level.ExplosionInteraction.MOB);
            explosion.getHitPlayers().forEach(
                    (player, pos) -> player.setSecondsOnFire(3)
            );
            this.discard();
        }
    }

    protected void spawnExplosionParticles() {
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.position().x,
                    this.position().y + 0.5,
                    this.position().z,
                    30,
                    3, 3, 3,
                    0.15
            );

            level.sendParticles(
                    ModParticleTypes.SOUL_FIREBALL.get(),
                    this.position().x,
                    this.position().y + 0.5,
                    this.position().z,
                    20,
                    3, 3, 3,
                    0.15
            );
        }

        // NOTE: too inconsistent for some reason, sometimes this method doesn't get invoked on client.
//        if (this.level() instanceof ClientLevel level) {
//            for (int i = 0; i < 20; i++) {
//                double xRand = level.getRandom().nextDouble() - 0.5;
//                double yRand = level.getRandom().nextDouble() - 0.5;
//                double zRand = level.getRandom().nextDouble() - 0.5;
//                level.addParticle(
//                        ParticleTypes.SOUL_FIRE_FLAME,
//                        this.position().x + xRand * 5,
//                        this.position().y + yRand * 5 + 0.5,
//                        this.position().z + zRand * 5,
//                        xRand * 0.25,
//                        yRand * 0.25,
//                        zRand * 0.25
//                );
//            }
//
//            for (int i = 0; i < 15; i++) {
//                double xRand = level.getRandom().nextDouble() - 0.5;
//                double yRand = level.getRandom().nextDouble() - 0.5;
//                double zRand = level.getRandom().nextDouble() - 0.5;
//                level.addParticle(
//                        ModParticleTypes.SOUL_FIREBALL.get(),
//                        this.position().x + xRand * 5,
//                        this.position().y + yRand * 5 + 0.5,
//                        this.position().z + zRand * 5,
//                        xRand * 0.25,
//                        yRand * 0.25,
//                        zRand * 0.25
//                );
//            }
//        }
    }

    /**
     * Called when the arrow hits an entity
     */
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity entity = result.getEntity();
            Entity owner = this.getOwner();
            entity.hurt(this.damageSources().explosion(owner, this), 10);
            if (owner instanceof LivingEntity) {
                this.doEnchantDamageEffects((LivingEntity)owner, entity);
            }

        }
    }

    public boolean isPickable() {
        return false;
    }

    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    protected boolean shouldBurn() {
        return false;
    }

}
