package net.zincstudios.scgextra.entity.common;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ProjectileManager;
import top.ribs.scguns.entity.ai.AIGunEvent;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.interfaces.IProjectileFactory;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBulletTrail;
import top.ribs.scguns.network.message.S2CMessageEntityCasingEject;
import top.ribs.scguns.network.message.S2CMessageEntityMuzzleFlash;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class MobUtil {

    // Common Constants
    public static final int DEFAULT_STUN_DURATION = 60;

    public static void addGunAttachment(ItemStack gunStack, ItemStack attachmentStack) {
        if (gunStack.getItem() instanceof GunItem gunItem
                && attachmentStack.getItem() instanceof IAttachment<?> attachment
                && gunItem.getGun().canAttachType(attachment.getType())
                && attachment.canAttachTo(gunStack)) {

            CompoundTag tag = gunStack.getOrCreateTag();
            CompoundTag attachments;
            if (tag.contains("Attachments")) {
                attachments = tag.getCompound("Attachments");
            } else {
                attachments = new CompoundTag();
            }

            attachments.put(attachment.getType().getTagKey(), attachmentStack.save(new CompoundTag()));

            tag.put("Attachments", attachments);
        }
    }

    public static Vec3 lerpVec(Vec3 a, Vec3 b, double t) {
        return new Vec3(
                Mth.lerp(t, a.x, b.x),
                Mth.lerp(t, a.y, b.y),
                Mth.lerp(t, a.z, b.z)
        );
    }

    public static LevelLocation levelLocationFromEntity(LivingEntity entity) {
        return LevelLocation.create(
                entity.level(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                Config.COMMON.network.projectileTrackingRange.get()
        );
    }

    /**
     * Used to gradually turn an entity to a direction. Intended to be invoked every tick when being used.
     */
    public static void turnEntityToYaw(LivingEntity entity, float yaw, float turnSpeed) {
        entity.setYRot(Mth.approachDegrees(entity.getYRot(), yaw, turnSpeed));
        entity.setYHeadRot(entity.getYRot());
        entity.setYBodyRot(entity.getYRot());
    }

    public static void putBlockPosToTag(BlockPos blockPos, String prefix, CompoundTag tag) {
        tag.putInt(prefix + "X", blockPos.getX());
        tag.putInt(prefix + "Y", blockPos.getY());
        tag.putInt(prefix + "Z", blockPos.getZ());
    }

    public static @Nullable BlockPos getBlocKPosFromTag(String prefix, CompoundTag tag) {
        if (tag.contains(prefix+"X")) {
            return new BlockPos(tag.getInt(prefix+"X"), tag.getInt(prefix+"Y"), tag.getInt(prefix+"Z"));
        }
        return null;
    }

    public static Vec3 vecFromRot(float yRot) {
        float yaw = (float) Math.toRadians(yRot);
        float x = -Mth.sin(yaw);
        float z = Mth.cos(yaw);
        return new Vec3(x, 0, z);
    }

    public static float rotFromVec(Vec3 vec) {
        return (float) Mth.atan2(-vec.x, vec.z) * Mth.RAD_TO_DEG;
    }

    public static Vec3 toVec(float yaw, float pitch) {
        double yawRad   = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z =  Math.cos(yawRad) * Math.cos(pitchRad);

        return new Vec3(x, y, z);
    }

    // NOTE: Copied and edited from AIGunEvent, check for when scguns does a major update
    public static void performGunAttack(Mob shooter, LivingEntity target, ItemStack itemStack, Gun modifiedGun, float accuracyModifier, Vec3 spawnOffset) {
        if (shooter.hasEffect(ModEffects.DEAFENED.get()) || shooter.hasEffect(ModEffects.BLINDED.get())) {
            accuracyModifier *= 0.5F;
        }

        if (target.hasEffect(ModEffects.DEAFENED.get())) {
            accuracyModifier *= 0.75F;
        }

        float finalAccuracyModifier = accuracyModifier;
        performGunAttack(shooter, target, itemStack, modifiedGun,
                () -> AIGunEvent.getDirection(shooter, target, itemStack, (GunItem)itemStack.getItem(), modifiedGun, finalAccuracyModifier),
                spawnOffset);
    }

    public static void performGunAttack(Mob shooter, LivingEntity target, ItemStack itemStack, Gun modifiedGun, Supplier<Vec3> directionSupplier, Vec3 spawnOffset) {
        Level level = shooter.level();
        if (!level.isClientSide()) {
            int count = modifiedGun.getProjectile().getProjectileAmount();
            Gun.Projectile projectileProps = modifiedGun.getProjectile();
            ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];

            float difficultyDamageMultiplier = getDifficultyDamageMultiplier(level.getDifficulty());
            float configDamageMultiplier = Config.COMMON.gameplay.mobGunDamageMultiplier.get().floatValue();
            float finalDamageMultiplier = difficultyDamageMultiplier * configDamageMultiplier;

            for(int i = 0; i < count; ++i) {
                IProjectileFactory factory = ProjectileManager.getInstance().getFactory(BuiltInRegistries.ITEM.getKey((Item) Objects.requireNonNull(projectileProps.getItem())));
                ProjectileEntity projectileEntity = factory.create(level, shooter, itemStack, (GunItem)itemStack.getItem(), modifiedGun);
                projectileEntity.setWeapon(itemStack);
                float originalDamage = Gun.getAdditionalDamage(itemStack);
                float scaledDamage = originalDamage * finalDamageMultiplier;
                projectileEntity.setAdditionalDamage(scaledDamage);
                projectileEntity.getPersistentData().putFloat("AIDamageScale", finalDamageMultiplier);
                Vec3 dir = directionSupplier.get();
                double speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(itemStack);
                double speed = GunModifierHelper.getModifiedProjectileSpeed(itemStack, projectileEntity.getProjectile().getSpeed() * speedModifier);
                projectileEntity.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
                projectileEntity.updateHeading();
                double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / (double)2.0F;
                double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / (double)2.0F;
                double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / (double)2.0F;
                projectileEntity.setPos(posX + spawnOffset.x, posY+spawnOffset.y, posZ+spawnOffset.z);
                level.addFreshEntity(projectileEntity);
                spawnedProjectiles[i] = projectileEntity;
                projectileEntity.tick();
            }

            int radius = (int)shooter.getX();
            int y1 = (int)(shooter.getY() + (double)1.0F);
            int z1 = (int)shooter.getZ();
            double r = Config.COMMON.network.projectileTrackingRange.get();
            ParticleOptions data = GunEnchantmentHelper.getParticle(itemStack);
            boolean isVisible = !modifiedGun.getProjectile().shouldHideTrail();
            S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), data, isVisible);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(level, (double)radius, (double)y1, (double)z1, r), messageBulletTrail);
            if (modifiedGun.getDisplay().getFlash() != null) {
                float randomValue = level.random.nextFloat();
                Vec3 weaponOrigin = PropertyHelper.getModelOrigin(itemStack, PropertyHelper.GUN_DEFAULT_ORIGIN);
                Vec3 flashPosition = PropertyHelper.getMuzzleFlashPosition(itemStack, modifiedGun).subtract(weaponOrigin);
                S2CMessageEntityMuzzleFlash flashMessage = new S2CMessageEntityMuzzleFlash(shooter.getId(), randomValue, flashPosition, false);
                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(level, (double)radius, (double)y1, (double)z1, r), flashMessage);
            }

            if (Config.COMMON.gameplay.spawnCasings.get() && modifiedGun.getProjectile().ejectsCasing() && !modifiedGun.getProjectile().ejectDuringReload()) {
                ResourceLocation particleLocation = modifiedGun.getProjectile().getCasingParticle();
                if (particleLocation != null) {
                    S2CMessageEntityCasingEject casingMessage = new S2CMessageEntityCasingEject(shooter.getId(), particleLocation);
                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(level, (double)radius, (double)y1, (double)z1, r), casingMessage);
                }
            }

            if (Config.CLIENT.display.fireLights.get()) {
                BlockState targetState = shooter.level().getBlockState(BlockPos.containing(shooter.getEyePosition()));
                if (targetState.getBlock() == ModBlocks.TEMPORARY_LIGHT.get()) {
                    if (AIGunEvent.getValue(shooter.level(), BlockPos.containing(shooter.getEyePosition()), "Delay") < (double)1.0F) {
                        updateDelayAndNotify(shooter.level(), BlockPos.containing(shooter.getEyePosition()), targetState);
                    }
                } else if (targetState.getBlock() == Blocks.AIR || targetState.getBlock() == Blocks.CAVE_AIR) {
                    BlockState dynamicLightState = ModBlocks.TEMPORARY_LIGHT.get().defaultBlockState();
                    shooter.level().setBlock(BlockPos.containing(shooter.getEyePosition()), dynamicLightState, 3);
                }
            }

        }
    }

    // Copied from AIGunEvent
    private static void updateDelayAndNotify(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.getPersistentData().putDouble("Delay", (double)1.0F);
        }

        if (world instanceof Level) {
            ((Level)world).sendBlockUpdated(pos, state, state, 3);
        }

    }

    // Copied from AIGunEvent
    private static float getDifficultyDamageMultiplier(Difficulty difficulty) {
        float var10000;
        switch (difficulty) {
            case PEACEFUL -> var10000 = 0.05F;
            case EASY -> var10000 = 0.35F;
            case NORMAL -> var10000 = 0.5F;
            case HARD -> var10000 = 0.65F;
            default -> throw new IncompatibleClassChangeError();
        }

        return var10000;
    }
    public static void tickDeath(LivingEntity entity, int tick) {
        // Override to only extend death time
        ++entity.deathTime;
        if (entity.deathTime >= tick && !entity.level().isClientSide() && !entity.isRemoved()) {
            entity.level().broadcastEntityEvent(entity, (byte)60);
            entity.remove(RemovalReason.KILLED);
        }
    }
}
