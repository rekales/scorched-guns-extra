package net.zincstudios.scgextra.raid;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.zincstudios.scgextra.SCGExtra;
import top.ribs.scguns.entity.monster.DissidentEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Holds the parsed raid data and provides methods for querying and generating data such as WaveRaidData#generateRaiders
 * <br/>
 * Deprecated fields are not intended to be accessed directly outside the class unless actually needed.
 */
// @SuppressWarnings({"unused", "deprecation"})
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record WaveRaidData(
        String id,
        String flare_raid_id,
        @Deprecated List<Wave> waves,
        @Deprecated List<RaiderEntry> infantry,
        @Deprecated List<RaiderEntry> elite,
        @Deprecated List<RaiderEntry> miniboss,
        @Deprecated List<RaiderEntry> boss,
        @Deprecated @Nullable ResourceLocation loot
) {

    private static final Map<String, WaveRaidData> RAIDS = new HashMap<>();  // Key: raid id
    private static final Map<String, WaveRaidData> FLARE_RAIDS = new HashMap<>();  // Key: original raid id

    public static void addWaveRaid(WaveRaidData raid) {
        RAIDS.put(raid.id, raid);
        if (!raid.flare_raid_id.isEmpty()) {
            FLARE_RAIDS.put(raid.flare_raid_id, raid);
        }
    }

    public static void clearWaveRaids() {
        RAIDS.clear();
        FLARE_RAIDS.clear();
    }

    public WaveRaidData {
        if (infantry.isEmpty() || elite.isEmpty() || miniboss.isEmpty() || boss.isEmpty()) {
            throw new IllegalArgumentException("entries cannot be empty");
        }
        for (Wave wave : waves) {
            if (wave.getTotal() == 0) {
                throw new IllegalArgumentException("waves cannot be empty");
            }
        }
        // TODO: complex check if there's enough max_spawns for the wave.
    }

    public static @Nullable WaveRaidData getWaveRaidFromFlareRaid(String flareRaidId) {
        return FLARE_RAIDS.get(flareRaidId);
    }

    public static @Nullable WaveRaidData getWaveRaid(String id) {
        return RAIDS.get(id);
    }

    public enum Rank {
        INFANTRY, ELITE, MINIBOSS, BOSS
    }

    public List<RaiderEntry> getRaiderEntries(Rank rank) {
        return switch (rank) {
            case INFANTRY -> infantry;
            case ELITE -> elite;
            case MINIBOSS -> miniboss;
            case BOSS -> boss;
        };
    }


    public List<RaiderEntry> generateRaiders(int waveIndex, RandomSource random) {
        return this.generateRaiders(this.waves.get(waveIndex), random);
    }

    public List<RaiderEntry> generateRaiders(Wave wave, RandomSource random) {
        List<RaiderEntry> spawnList = new ArrayList<>();
        spawnList.addAll(sampleRandomRaiders(this.getRaiderEntries(Rank.INFANTRY), wave.infantry, random));
        spawnList.addAll(sampleRandomRaiders(this.getRaiderEntries(Rank.ELITE), wave.elite, random));
        spawnList.addAll(sampleRandomRaiders(this.getRaiderEntries(Rank.MINIBOSS), wave.miniboss, random));
        spawnList.addAll(sampleRandomRaiders(this.getRaiderEntries(Rank.BOSS), wave.boss, random));

        return spawnList;
    }

    private static List<RaiderEntry> sampleRandomRaiders(List<RaiderEntry> entries, int value, RandomSource random) {
        Map<RaiderEntry, Integer> availableEntries = new HashMap<>();  // entry to spawns remaining pair
        for (RaiderEntry entry : entries) {
            availableEntries.put(entry, entry.maxCount);
        }

        List<RaiderEntry> spawnList = new ArrayList<>();
        int SCALE = 10000;  // scale so I can do fractions while not dealing with floating point imprecision
        int valueLeft = value * SCALE;

        while (valueLeft > 0) {
            double totalWeight = availableEntries.keySet().stream()
                    .mapToDouble(e-> e.weight)
                    .sum();

            double randomWeight = random.nextDouble() * totalWeight;
            for (RaiderEntry entry : availableEntries.keySet()) {
                randomWeight -= entry.weight;
                if (randomWeight < 0) {
                    spawnList.add(entry);
                    valueLeft -= (int) (entry.value * SCALE);

                    availableEntries.put(entry, availableEntries.get(entry)-1);
                    if (availableEntries.get(entry) <= 0) {
                        availableEntries.remove(entry);
                    }
                    break;
                }
            }
        }
        return spawnList;
    }

    public record RaiderEntry(EntityType<? extends Mob> entityType, double maxHealth, double weight, double value, int maxCount) {
        public static final int DEFAULT_MAX_SPAWNS = 1000;

        public RaiderEntry {
            if (weight <= 0) {
                throw new IllegalArgumentException("weight cannot be zero or lower");
            }
            if (value <= 0) {
                throw new IllegalArgumentException("value cannot be zero or lower");
            }
            if (maxCount <= 0) {
                maxCount = DEFAULT_MAX_SPAWNS;
            }
        }

        public @Nullable Mob createEntity(ServerLevel level) {
            Mob entity = entityType.create(level);
            if (entity == null) return null;

            if (this.maxHealth > 0) {
                AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
                if (healthAttr != null) {
                    double currentHealth = healthAttr.getBaseValue();
                    healthAttr.addPermanentModifier(new AttributeModifier(UUID.randomUUID(), "Raid fixed health", this.maxHealth - currentHealth, AttributeModifier.Operation.ADDITION));
                    entity.setHealth(entity.getMaxHealth());
                }
            }

            return entity;
        }
    }

    public record Wave(int infantry, int elite, int miniboss, int boss) {
        public int getTotal() {
            return this.infantry + this.elite + this.miniboss + this.boss;
        }
    }

    public boolean isFinalWave(int waveIndex) {
        return this.waves.size()-1 == waveIndex;
    }

    public Component getAnnouncement() {
        return Component.translatable(SCGExtra.MOD_ID+".raid.announcement."+this.id);
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean handleRaiderEdgeCase(Mob mob) {
        if (mob instanceof DissidentEntity) {
            return false;
        }

        return true;
    }

    public List<ItemStack> rollLoot(ServerLevel level) {
        if (this.loot == null) return List.of();
        LootTable table = level.getServer().getLootData().getLootTable(this.loot);
        LootParams params = new LootParams.Builder(level)
//                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                .create(LootContextParamSets.EMPTY);
        return table.getRandomItems(params);
    }
}
