package net.zincstudios.scgextra.raid;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.zincstudios.scgextra.SCGExtra;
import top.ribs.scguns.entity.monster.MotherGhastEntity;

import javax.annotation.Nullable;
import java.util.*;

// TODO: check raid center location
//@SuppressWarnings("unused")
public class WaveRaidState {

    private final UUID raidId;
    private final ServerLevel level;
    private final long startTime;  // level gametime timestamp
    private final WaveRaidData waveRaidData;

    private final Map<UUID, LivingEntity> raiders = new HashMap<>();  // saved uuid to cached entity pair

    private Vec3 spawnCenter;
    private int currentWave;
    private int totalWaveSpawned = 0;
    private Vec3 lootDropPos;

    public WaveRaidState(WaveRaidData waveRaidData, ServerLevel level, Vec3 spawnCenter) {
        this.raidId = UUID.randomUUID();
        this.startTime = level.getGameTime();
        this.level = level;
        this.currentWave = 0;
        this.waveRaidData = waveRaidData;
        this.spawnCenter = spawnCenter;
        this.lootDropPos = spawnCenter;
    }

    // For nbt deserialization
    private WaveRaidState(ServerLevel level, UUID raidId, WaveRaidData waveRaidData, long startTime, Vec3 spawnCenter) {
        this.level = level;
        this.raidId = raidId;
        this.waveRaidData = waveRaidData;
        this.startTime = startTime;
        this.spawnCenter = spawnCenter;
    }

    public int getCurrentWave() {
        return this.currentWave;
    }

    public WaveRaidData getWaveRaidData() {
        return this.waveRaidData;
    }

    public Vec3 getCenter() {
        return this.spawnCenter;
    }

    public long getStartTime() {
        return startTime;
    }

    private void addRaider(Mob mob) {
        this.raiders.put(mob.getUUID(), mob);
        this.totalWaveSpawned = this.raiders.size();
    }

    public void discardRaiders() {
        this.raiders.forEach((raiderId, raider) -> {
            Entity entity = raider == null ? this.level.getEntity(raiderId) : raider;
            if (entity != null && !entity.isRemoved()) {
                entity.discard();
            }
        });
    }

    public void advanceWave() {
        if (!this.waveRaidData.isFinalWave(this.currentWave)) {
            this.currentWave++;
            this.totalWaveSpawned = 0;
        }
    }

    public void updateRaiders() {
        this.raiders.entrySet().removeIf(entry -> {
            if (entry.getValue() == null) {
                Entity entity = this.level.getEntity(entry.getKey());
                if (entity instanceof LivingEntity living) {
                    entry.setValue(living);
                } else {
                    return true;
                }
            }
            return entry.getValue().isRemoved();
        });

        if (this.level.getGameTime() % 20 == 1 && !this.raiders.isEmpty()) {
            this.spawnCenter = this.raiders.values().stream()
                    .map(Entity::position)
                    .reduce(Vec3.ZERO, Vec3::add)
                    .scale(1.0 / this.raiders.size());
        }
        this.raiders.values().stream()
                .findAny()
                .ifPresent(raider -> this.lootDropPos = raider.position().add(0, 0.5, 0));
    }

    public int getTotalWaveSpawned() {
        return this.totalWaveSpawned;
    }

    public int raidersLeft() {
        return this.raiders.size();
    }

    public boolean isFinalWave() {
        return this.waveRaidData.isFinalWave(this.currentWave);
    }

    public Vec3 getLootDropPos() {
        return this.lootDropPos;
    }

    public void spawnCurrentWaveMobs(Vec3 waveCenter, float waveSpawnRadius, @Nullable LivingEntity target) {
        WaveRaidData raidData = this.getWaveRaidData();
        List<WaveRaidData.RaiderEntry> spawnList = raidData.generateRaiders(this.getCurrentWave(), level.getRandom());

        int failedAttemptsLeft = spawnList.size()/2 + 5;
        while (!spawnList.isEmpty() && failedAttemptsLeft > 0) {
            for (WaveRaidData.RaiderEntry entry : List.copyOf(spawnList)) {
                Mob mob = entry.createEntity(level);
                if (mob == null) {
                    failedAttemptsLeft--;
                    continue;
                }
                Vec3 pos = WaveRaidUtil.findMobSpawnPos(level, waveCenter, waveSpawnRadius);

                mob.setPos(mob instanceof MotherGhastEntity ? pos.add(0, 20, 0) : pos);

                if (mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED) && target != null) {
                    mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
                    if (mob.getBrain().checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)) {
                        mob.getBrain().setMemoryWithExpiry(MemoryModuleType.WALK_TARGET, new WalkTarget(
                            new EntityTracker(target, false), 1.2f, 16
                        ), 200);
                    }
                } else {
                    mob.setTarget(target);
                }

                AttributeInstance attr = mob.getAttribute(Attributes.FOLLOW_RANGE);
                if (attr != null && attr.getValue() < 64) {
                    attr.setBaseValue(64.0);
                }

                mob.setPersistenceRequired();
                boolean finalize = raidData.handleRaiderEdgeCase(mob);
                if (finalize) {
                    ForgeEventFactory.onFinalizeSpawn(
                            mob, level,
                            level.getCurrentDifficultyAt(BlockPos.containing(pos)),
                            MobSpawnType.EVENT, null, null
                    );
                }
                level.addFreshEntity(mob);
                this.addRaider(mob);
                spawnList.remove(entry);
            }
        }

        if (!spawnList.isEmpty()) {
            for (WaveRaidData.RaiderEntry entry : spawnList) {
                SCGExtra.LOGGER.warn("Failed to spawn {}", entry.entityType());
            }
        }
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("RaidId", this.raidId);

        ListTag raiderList = new ListTag();
        for (UUID uuid : this.raiders.keySet()) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("UUID", uuid);
            raiderList.add(uuidTag);
        }
        tag.put("Raiders", raiderList);

        tag.putLong("StartTime", this.startTime);
        tag.putDouble("SpawnX", this.spawnCenter.x);
        tag.putDouble("SpawnY", this.spawnCenter.y);
        tag.putDouble("SpawnZ", this.spawnCenter.z);
        tag.putString("WaveRaidData", this.waveRaidData.id());
        tag.putInt("CurrentWave", this.currentWave);
        tag.putInt("TotalWaveSpawned", this.totalWaveSpawned);

        return tag;
    }

    public static @Nullable WaveRaidState deserialize(CompoundTag tag, ServerLevel level) {
        UUID raidId = tag.getUUID("RaidId");

        Set<UUID> raiders = new HashSet<>();
        ListTag raiderList = tag.getList("Raiders", Tag.TAG_COMPOUND);
        for (int i = 0; i < raiderList.size(); i++) {
            raiders.add(raiderList.getCompound(i).getUUID("UUID"));
        }

        long startTime = tag.getLong("StartTime");
        Vec3 spawnCenter = new Vec3(
                tag.getDouble("SpawnX"),
                tag.getDouble("SpawnY"),
                tag.getDouble("SpawnZ")
        );

        String waveRaidDataId = tag.getString("WaveRaidData");
        WaveRaidData waveRaidData = WaveRaidData.getWaveRaid(waveRaidDataId);
        if (waveRaidData == null) {
            SCGExtra.LOGGER.warn("raid id: {}", waveRaidDataId);
            return null;
        }

        int currentWave = tag.getInt("CurrentWave");
        int totalWaveSpawned = tag.getInt("TotalWaveSpawned");

        WaveRaidState state = new WaveRaidState(level, raidId, waveRaidData, startTime, spawnCenter);
        raiders.forEach(raiderId -> state.raiders.put(raiderId, null));
        state.currentWave = currentWave;
        state.totalWaveSpawned = totalWaveSpawned;
        return state;
    }
}
