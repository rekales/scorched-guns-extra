package net.zincstudios.scgextra.entity.cog.gigantes;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.entity.monster.SkyCarrierEntity;
import top.ribs.scguns.init.ModEntities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class SummonCarriers extends Behavior<Mob> {

    public static final int DEFAULT_COOLDOWN_DURATION_TICKS = 400;

    public final int cooldownDuration;

    private long cooldownEnd = 0;  // gameTime timestamp

    public SummonCarriers() {
        this(DEFAULT_COOLDOWN_DURATION_TICKS);
    }

    public SummonCarriers(int cooldownDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ), 80);
        this.cooldownDuration = cooldownDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        return this.cooldownEnd < level.getGameTime();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        this.cooldownEnd = gameTime + this.cooldownDuration;
        spawnSkyCarriers(mob);
    }

    // Copied and edited from SignalBeaconEntity
    private void spawnSkyCarriers(Mob parent) {
        Level level = parent.level();
        if (level instanceof ServerLevel serverLevel) {
            int maxSpawns = 2;
            int successfulSpawns = 0;
            int maxAttempts = maxSpawns * 5;
            Vec3 beaconPosition = parent.position();

            for(int attempt = 0; attempt < maxAttempts && successfulSpawns < maxSpawns; ++attempt) {
                Vec3 spawnPos = findValidSpawnPosition(serverLevel, parent);
                if (spawnPos != null) {
                    SkyCarrierEntity skyCarrier = (SkyCarrierEntity)((EntityType<?>) ModEntities.SKY_CARRIER.get()).create(serverLevel);
                    if (skyCarrier != null) {
                        skyCarrier.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, level.random.nextFloat() * 360.0F, 0.0F);
                        skyCarrier.setInitialTarget(beaconPosition);
                        skyCarrier.getMoveControl().setWantedPosition(parent.getX(), parent.getY(), parent.getZ(), 1.0F);
                        serverLevel.addFreshEntity(skyCarrier);
                        serverLevel.sendParticles(ParticleTypes.CLOUD, skyCarrier.getX(), skyCarrier.getY(), skyCarrier.getZ(), 10, 0.5F, 0.2, 0.2, 0.1);
                        serverLevel.playSound(null, skyCarrier.getX(), skyCarrier.getY(), skyCarrier.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
                        ++successfulSpawns;
                    }
                }
            }

        }
    }

    // Copied and edited from SignalBeaconEntity
    private static Vec3 findValidSpawnPosition(ServerLevel serverLevel, Mob mob) {
        for(int radiusAttempt = 0; radiusAttempt < 3; ++radiusAttempt) {
            double baseDistance = (double)20.0F + (double)radiusAttempt * (double)10.0F;

            for(int positionAttempt = 0; positionAttempt < 8; ++positionAttempt) {
                double distance = baseDistance + serverLevel.getRandom().nextDouble() * (double)5.0F;
                double angle = serverLevel.getRandom().nextDouble() * (double)2.0F * Math.PI;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;

                for(int heightOffset = 10; heightOffset >= -5; heightOffset -= 3) {
                    double spawnX = mob.getX() + offsetX;
                    double spawnY = mob.getY() + (double)heightOffset;
                    double spawnZ = mob.getZ() + offsetZ;
                    BlockPos spawnBlockPos = new BlockPos((int)spawnX, (int)spawnY, (int)spawnZ);
                    if (isValidSpawnPosition(serverLevel, spawnBlockPos, spawnX, spawnY, spawnZ)) {
                        return new Vec3(spawnX, spawnY, spawnZ);
                    }
                }
            }
        }

        return null;
    }

    // Copied from SignalBeaconEntity
    @SuppressWarnings("deprecation")
    private static boolean isValidSpawnPosition(ServerLevel serverLevel, BlockPos blockPos, double exactX, double exactY, double exactZ) {
        AABB boundingBox = new AABB(exactX - (double)1.5F, exactY - (double)1.0F, exactZ - (double)1.5F, exactX + (double)1.5F, exactY + (double)2.0F, exactZ + (double)1.5F);

        for(BlockPos pos : BlockPos.betweenClosed((int)boundingBox.minX, (int)boundingBox.minY, (int)boundingBox.minZ, (int)boundingBox.maxX, (int)boundingBox.maxY, (int)boundingBox.maxZ)) {
            if (!serverLevel.getBlockState(pos).isAir() && !serverLevel.getBlockState(pos).canBeReplaced() && serverLevel.getBlockState(pos).getBlock().defaultBlockState().blocksMotion()) {
                return false;
            }
        }

        BlockPos groundCheck = new BlockPos((int)exactX, (int)exactY - 5, (int)exactZ);

        for(int i = 0; i < 5 && serverLevel.getBlockState(groundCheck.below(i)).isAir(); ++i) {
            if (i == 4) {
                return false;
            }
        }

        List<Entity> entitiesInArea = serverLevel.getEntitiesOfClass(Entity.class, boundingBox);
        return entitiesInArea.isEmpty() && serverLevel.getWorldBorder().isWithinBounds(blockPos);
    }
}
