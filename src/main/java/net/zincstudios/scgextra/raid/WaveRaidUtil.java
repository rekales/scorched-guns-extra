package net.zincstudios.scgextra.raid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.SCGExtra;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@ParametersAreNonnullByDefault
public final class WaveRaidUtil {

    public static final int FIND_SPAWN_LOCATION_ATTEMPTS = 40;
    public static final double SPAWN_MIN_PLAYER_DISTANCE = 32;
    public static final double SPAWN_MAX_PLAYER_DISTANCE = 48;

    private static final float CHECK_ELEVATION = 12;

    public static Component getBossBarLabel(WaveRaidData raidData, int currentWave) {
//        if (raidData.isFinalWave(currentWave) && this.raiderUUIDs.size() == 1) {
//            UUID bossId = this.raiderUUIDs.iterator().next();
//            Entity entity = this.level.getEntity(bossId);
//            if (entity instanceof LivingEntity bossEntity) {
//                return bossEntity.getDisplayName();
//            }
//        }

        String wave = switch (currentWave) {
            case 0 -> "wave_1";
            case 1 -> "wave_2";
            case 2 -> "wave_3";
            case 3 -> "wave_4";
            case 4 -> "wave_5";
            case 5 -> "wave_6";
            case 6 -> "wave_7";
            case 7 -> "wave_8";
            case 8 -> "wave_9";
            default -> "";
        };
        if (raidData.isFinalWave(currentWave)) wave = "last_wave";

        return Component.translatable(SCGExtra.MOD_ID+".raid.label."+raidData.id())
                .append(" ")
                .append(Component.translatable(SCGExtra.MOD_ID+".raid.label."+wave));
    }

    public static void announceToNearbyPlayers(ServerLevel level, Component message, Vec3 pos, double radius) {
        for(ServerPlayer player : level.getPlayers((player) -> player.position().distanceTo(pos) <= radius)) {
            player.sendSystemMessage(message);
        }
    }

    public static @Nullable ServerPlayer findNearestPlayer(ServerLevel level, Vec3 pos, float radius) {
        ServerPlayer nearest = null;
        double nearestDist = radius;

        for(ServerPlayer player : level.players()) {
            if (!player.isSpectator() && !player.isCreative()) {
                double dist = player.position().distanceTo(pos);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = player;
                }
            }
        }

        return nearest;
    }

    private static @Nullable BlockPos clipBlock(ServerLevel level, Vec3 start, Vec3 end) {
        BlockHitResult result = level.clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                null
        ));

        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getBlockPos();
        }

        return null;
    }

    static @Nullable Vec3 findWaveSpawnLocation(ServerLevel level, Vec3 center, @Nullable Vec3 playerPos) {
        RandomSource random = level.getRandom();

        Vec3 checkStart = center.add(0, CHECK_ELEVATION, 0);
        if (!level.getBlockState(BlockPos.containing(checkStart)).isAir()) return null;

        for(int attempt = 0; attempt < FIND_SPAWN_LOCATION_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0F;
            double distance = 30.0F + random.nextDouble() * 14.0F;
            double x = checkStart.x + Math.cos(angle) * distance;
            double z = checkStart.z + Math.sin(angle) * distance;
            Vec3 xzTestPos = Vec3.atCenterOf(new BlockPos((int) x, (int) checkStart.y, (int) z));

            if (playerPos != null) {
                if (playerPos.subtract(xzTestPos).horizontalDistanceSqr() < SPAWN_MIN_PLAYER_DISTANCE * SPAWN_MIN_PLAYER_DISTANCE) {
                    SCGExtra.LOGGER.debug("Too close: " + xzTestPos + "  dist: " + playerPos.subtract(xzTestPos).horizontalDistance());
                    continue;
                } else if (playerPos.subtract(xzTestPos).horizontalDistanceSqr() > SPAWN_MAX_PLAYER_DISTANCE * SPAWN_MAX_PLAYER_DISTANCE) {
                    SCGExtra.LOGGER.debug("Too far: " + xzTestPos + "  dist: " + playerPos.subtract(xzTestPos).horizontalDistance());
                    continue;
                }
            }

            if (clipBlock(level, checkStart, xzTestPos) != null) continue;

            BlockPos groundPos = clipBlock(level, xzTestPos, xzTestPos.add(0, -CHECK_ELEVATION*1.5, 0));
            if (groundPos == null) continue;
            groundPos = groundPos.above();

            if (level.getBlockState(groundPos.below()).isFaceSturdy(level, groundPos.below(), Direction.UP, SupportType.FULL)
                    && level.getBlockState(groundPos).isPathfindable(level, groundPos, PathComputationType.LAND)
                    && level.getBlockState(groundPos.above()).isPathfindable(level, groundPos.above(), PathComputationType.LAND)
                    && level.getBlockState(groundPos.above(2)).isPathfindable(level, groundPos.above(2), PathComputationType.LAND)) {
                return Vec3.atBottomCenterOf(groundPos);
            }
        }

        return null;
    }

    // Copied and altered from RaidManager#findHenchmanSpawnPos
    @SuppressWarnings({"deprecation", "SameParameterValue"})
    static Vec3 findMobSpawnPos(ServerLevel level, Vec3 center, double radius) {
        RandomSource random = level.getRandom();

        for(int attempt = 0; attempt < 10; ++attempt) {
            double angle = random.nextDouble() * Math.PI * (double)2.0F;
            double distance = random.nextDouble() * radius;
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            BlockPos pos = new BlockPos((int)x, (int)center.y, (int)z);
            BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);//changed to MOTION_BLOCKING from MOTION_BLOCKING_NO_LEAVES
            if (level.getBlockState(groundPos.below()).isSolid()
                    && level.getBlockState(groundPos).isAir()
                    && level.getBlockState(groundPos.above()).isAir()) {
                return new Vec3((double)groundPos.getX() + (double)0.5F, groundPos.getY(), (double)groundPos.getZ() + (double)0.5F);
            }
        }

        return center;  // TODO: better fallback
    }
}
