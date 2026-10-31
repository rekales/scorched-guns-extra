package net.zincstudios.scgextra.entity.common.gun;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.network.GunFlashMessage;
import net.zincstudios.scgextra.network.SCGEPacketHandler;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBulletTrail;
import top.ribs.scguns.network.message.S2CMessageEntityCasingEject;
import top.ribs.scguns.particles.TrailData;

import java.util.function.Function;

// there's no decent gun builder in base scguns so I have to do everything here
public abstract class ScorchedSimGun implements SimulatedGun {

    private final Gun gunBase;
    private final int fireRate;
    private final int burstAmount;
    private final int burstInterval;
    private final float projectileDamage;
    private final double projectileSpeed;
    private final int ammoCapacity;
    private final int reloadTime;
    private final float idealRange;
    private final float maxRange;
    private final int gunIndex;
    private final ProjectileFactory projectileFactory;
    private final Function<Vec3, Vec3> velocityModifier;

    private int burstCooldown = 0;
    private int burstLeft = 0;
    private int nextAttack = 0;  // tickCount timestamp
    private int ammoCount;
    private int nextReload = 0;  // tickCount timestamp
    private boolean reloading = false;

    protected ScorchedSimGun(
            Gun gun,
            int fireRate,
            int burstAmount,
            int burstInterval,
            float projectileDamage,
            double projectileSpeed,
            boolean reloads,
            int ammoCapacity,
            int reloadTime,
            float idealRange,
            float maxRange,
            int gunIndex,
            ProjectileFactory projectileFactory,
            Function<Vec3, Vec3> velocityModifier)
    {
        this.gunBase = gun;
        this.fireRate = fireRate;
        this.burstAmount = burstAmount;
        this.burstInterval = burstInterval;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
        if (reloads) {
            this.ammoCapacity = ammoCapacity;
            this.reloadTime = reloadTime;
        } else {
            this.ammoCapacity = Integer.MAX_VALUE;
            this.reloadTime = 1;
        }
        this.idealRange = idealRange;
        this.maxRange = maxRange;
        this.gunIndex = gunIndex;
        this.projectileFactory = projectileFactory;
        this.velocityModifier = velocityModifier;
    }

    protected ScorchedSimGun(Gun gun, boolean reloads, float idealRange, float maxRange, int gunIndex,
                             ProjectileFactory projectileFactory, Function<Vec3, Vec3> velocityModifier) {
        this(
                gun,
                gun.getGeneral().getRate(),
                gun.getGeneral().getBurstAmount(),
                gun.getGeneral().getBurstCooldown(),
                gun.getProjectile().getDamage(),
                gun.getProjectile().getSpeed(),
                reloads,
                gun.getReloads().getMaxAmmo(),
                gun.getReloads().getReloadTimer() *
                        (gun.getReloads().getReloadAmount() / gun.getReloads().getMaxAmmo()),
                idealRange,
                maxRange,
                gunIndex,
                projectileFactory,
                velocityModifier
        );
    }

    @Override
    public boolean tickFire(LivingEntity shooter, Vec3 targetPos, float accuracyModifier, boolean firing) {
        int tickCount = shooter.tickCount;

        if (this.ammoCount > 0) {
            if (this.burstLeft > 0 && --this.burstCooldown <= 0) {
                this.burstLeft--;
                this.burstCooldown = this.burstInterval;
                fireGun(shooter, targetPos, accuracyModifier);
                return true;
            }

            if (this.nextAttack <= tickCount && firing) {
                this.nextAttack = tickCount + this.fireRate;
                if (this.burstAmount > 1) {
                    this.burstLeft = this.burstAmount-1;
                    this.burstCooldown = this.burstInterval;
                }
                fireGun(shooter, targetPos, accuracyModifier);
                return true;
            }
        } else {
            if (!this.reloading) {
                this.reloading = true;
                this.burstLeft = 0;
                this.nextReload = tickCount + this.reloadTime;
            } else if (tickCount > this.nextReload) {
                this.reloading = false;
                this.reloadAmmo();
            }
        }
        return false;
    }

    public void fireGun(LivingEntity shooter, Vec3 targetPos, float accuracyModifier) {
        Vec3 startPos = shooter instanceof BulletSpawnOffset bso
                ? shooter.position().add(bso.getBulletSpawnOffset(this.gunIndex))
                : shooter.getEyePosition();
        fireProjectiles(shooter, startPos, targetPos, accuracyModifier);
        this.gunShotSound(shooter.level(), startPos);
        this.gunShotFlash(shooter);
        this.gunShotCasing(shooter);
        this.ammoCount--;

        if (shooter instanceof Gunner gunner) {
            gunner.onGunFire(this ,targetPos);
        }
    }

    protected void fireProjectiles(LivingEntity shooter, Vec3 startPos, Vec3 targetPos, float accuracyModifier) {
        Level level = shooter.level();
        Gun gun = this.gunBase;
        Vec3 aimDir = SimulatedGun.getDirectionVector(startPos, targetPos);
        aimDir = addAimError(shooter, aimDir, accuracyModifier);
        float damageMult = getMobDamageMultiplier(level);
        if (shooter instanceof GunDamageMult gdm) {
            damageMult *= gdm.getGunDamageMult();
        }

        int count = gun.getProjectile().getProjectileAmount();
        ProjectileEntity[] projectiles = new ProjectileEntity[count];

        for (int i = 0; i < count; ++i) {
            ProjectileEntity projectileEntity = this.projectileFactory.create(level, shooter, gun);
            projectileEntity.setAdditionalDamage(this.projectileDamage - gun.getProjectile().getDamage());

            projectileEntity.getPersistentData().putFloat("AIDamageScale", damageMult);

            Vec3 vec = addWeaponSpread(shooter, aimDir, gun.getProjectile().getSpread());
            vec = vec.scale(this.projectileSpeed);
            vec = this.velocityModifier.apply(vec);
            projectileEntity.setDeltaMovement(vec);
            projectileEntity.updateHeading();
            projectileEntity.setPos(startPos);

            level.addFreshEntity(projectileEntity);
            projectiles[i] = projectileEntity;
            projectileEntity.tick();
        }

        int radius = (int)shooter.getX();
        int y1 = (int)(shooter.getY() + (double)1.0F);
        int z1 = (int)shooter.getZ();
        double r = Config.COMMON.network.projectileTrackingRange.get();
        ParticleOptions data = new TrailData(false);
        boolean isVisible = !gun.getProjectile().shouldHideTrail();
        S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(projectiles, gun.getProjectile(), shooter.getId(), data, isVisible);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(level, radius, y1, z1, r), messageBulletTrail);
    }

    protected void gunShotSound(Level level, Vec3 origin) {
        ResourceLocation fireSound = this.gunBase.getSounds().getFire();
        if (fireSound != null) {
            float volume = (float) Config.COMMON.gameplay.mobGunfireVolume.get();
            float pitch = 0.9F + level.getRandom().nextFloat() * 0.2F;
            level.playSound(
                    null,
                    origin.x, origin.y, origin.z,
                    SoundEvent.createVariableRangeEvent(fireSound),
                    SoundSource.HOSTILE,
                    volume - 0.5F,
                    pitch
            );
        }
    }

    protected void gunShotFlash(LivingEntity shooter) {
        if (this.gunBase.getDisplay().getFlash() == null) return;
        ResourceLocation flashTexture = ResourceLocation.fromNamespaceAndPath(ScorchedGuns.MODID,
                "textures/effect/" + this.gunBase.getDisplay().getFlash().getTextureLocation() + ".png");
        SCGEPacketHandler.sendToNearbyPlayers(() -> MobUtil.levelLocationFromEntity(shooter),
                new GunFlashMessage(shooter.getId(), this.gunIndex, flashTexture));
    }

    protected void gunShotCasing(LivingEntity shooter) {
        if (Config.COMMON.gameplay.spawnCasings.get() && this.gunBase.getProjectile().ejectsCasing()) {
            ResourceLocation particleLocation = this.gunBase.getProjectile().getCasingParticle();
            if (particleLocation != null) {
                S2CMessageEntityCasingEject casingMessage = new S2CMessageEntityCasingEject(shooter.getId(), particleLocation);
                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> MobUtil.levelLocationFromEntity(shooter), casingMessage);
            }
        }
    }

    public Gun getGunBase() {
        return this.gunBase;
    }

    @Override
    public boolean hasChanged(LivingEntity entity) {
        return false;
    }

    @Override
    public float getMaxRange() {
        return this.maxRange;
    }

    @Override
    public float getIdealRange() {
        return this.idealRange;
    }

    @Override
    public int getAmmoCapacity() {
        return this.ammoCapacity;
    }

    @Override
    public int getAmmoCount() {
        return this.ammoCount;
    }

    @Override
    public void setAmmoCount(int ammoCount) {
        this.ammoCount = ammoCount;
    }

    static float getMobDamageMultiplier(Level level) {
        float difficultyDamageMultiplier = getDifficultyDamageMultiplier(level.getDifficulty());
        float configDamageMultiplier = Config.COMMON.gameplay.mobGunDamageMultiplier.get().floatValue();
        return difficultyDamageMultiplier * configDamageMultiplier;
    }

    static float getDifficultyDamageMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 0.05F;
            case EASY ->  0.35F;
            case NORMAL -> 0.5F;
            case HARD -> 0.65F;
        };
    }

    static float getDifficultyAimError(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 3.0F;
            case EASY -> 2.0F;
            case NORMAL -> 1.5F;
            case HARD -> 1.0F;
        };
    }

    /**
     * Need to redo the sloppy implementation from original
     *
     * @param dir expects a direction unit vector
     */
    private static Vec3 addAimError(LivingEntity shooter, Vec3 dir, float accuracyModifier) {
        float aimError = (BASE_AIM_ERROR * getDifficultyAimError(shooter.level().getDifficulty())) / accuracyModifier;
        aimError = Math.min(aimError, 25F);

        float aimErrorRad = aimError * Mth.DEG_TO_RAD;
        float theta1 = shooter.level().random.nextFloat() * 2F * (float) Math.PI;
        float r1 = Mth.sqrt(shooter.level().random.nextFloat()) * (float) Math.tan(aimErrorRad);

        Vec3 vecUpwards = SimulatedGun.getVectorFromRotation(shooter.getViewXRot(1F) + 90F, shooter.getViewYRot(1F));
        Vec3 vecSideways = dir.cross(vecUpwards);

        float a1 = Mth.cos(theta1) * r1;
        float a2 = Mth.sin(theta1) * r1;

        return dir.add(vecSideways.scale(a1)).add(vecUpwards.scale(a2)).normalize();
    }

    /**
     * @param dir expects a direction unit vector
     */
    public static Vec3 addWeaponSpread(LivingEntity shooter, Vec3 dir, float spread) {
        spread = Math.min(spread, 170F) * 0.5F * Mth.DEG_TO_RAD;
        Vec3 spreadUpwards = SimulatedGun.getVectorFromRotation(shooter.getViewXRot(1F) + 90F, shooter.getViewYRot(1F));
        Vec3 spreadSideways = dir.cross(spreadUpwards);

        float theta2 = shooter.level().random.nextFloat() * 2F * (float) Math.PI;
        float r2 = Mth.sqrt(shooter.level().random.nextFloat()) * (float) Math.tan(spread);

        float b1 = Mth.cos(theta2) * r2;
        float b2 = Mth.sin(theta2) * r2;

        return dir.add(spreadSideways.scale(b1)).add(spreadUpwards.scale(b2)).normalize();
    }
}
