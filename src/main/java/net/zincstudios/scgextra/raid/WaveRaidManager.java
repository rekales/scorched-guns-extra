package net.zincstudios.scgextra.raid;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.zincstudios.scgextra.SCGExtra;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;


// TODO: bossbar per boss
// TODO: maybe multiple active raids
// TODO: check nearby players
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaveRaidManager extends SavedData {

    public static final String SAVED_DATA_NAME = "SCGEWaveRaidData";
    public static final int RAID_TIMEOUT_TICKS = 12000;  // 10 minutes, TODO: config
    public static final float WAVE_SPAWN_RADIUS = 15;  // NOTE: turned constant from data, make dynamic if needed
    public static final float RAID_RADIUS = 512;  // NOTE: turned constant from data, make dynamic if needed
    private static final int NEXT_WAVE_DELAY = 30;

    private static final Map<ResourceLocation, WaveRaidManager> INSTANCES = new HashMap<>();

    @Nullable private WaveRaidState raidState = null;
    @Nullable private ServerBossEvent bossBar = null;
    private int nextWaveDelay = NEXT_WAVE_DELAY;

    // NOTE: doesn't check the saved data and might cause inconsistencies, unlikely but possible
    public static WaveRaidManager get(ResourceLocation key) {
        return INSTANCES.computeIfAbsent(key, (k) -> new WaveRaidManager());
    }

    public static WaveRaidManager get(ServerLevel level) {
        ResourceLocation dimension = level.dimension().location();
        if (!INSTANCES.containsKey(dimension)) {
            WaveRaidManager manager = level.getDataStorage().computeIfAbsent(
                    tag->load(level, tag),
                    WaveRaidManager::new,
                    SAVED_DATA_NAME
            );
            INSTANCES.put(dimension, manager);
            return manager;
        } else {
            return INSTANCES.get(dimension);
        }
    }

    public static List<WaveRaidManager> getAll() {
        return INSTANCES.values().stream().toList();
    }

    public void startRaid(WaveRaidData raidData, ServerLevel level, ServerPlayer player) {
        if (hasActiveRaid()) return;

        Vec3 raidCenter = player.position();
        Vec3 waveCenter = WaveRaidUtil.findWaveSpawnLocation(level, raidCenter, raidCenter);
        if (waveCenter == null) return;

        this.raidState = new WaveRaidState(raidData, level, raidCenter);
        this.raidState.spawnCurrentWaveMobs(waveCenter, WAVE_SPAWN_RADIUS, player);
        WaveRaidUtil.announceToNearbyPlayers(level, raidData.getAnnouncement(), raidCenter, RAID_RADIUS);
        this.nextWaveDelay = NEXT_WAVE_DELAY;
    }

    public void tick(ServerLevel level) {
        this.tickRaid(level);
        this.tickBossBar(level);
        this.setDirty();
    }

    private void tickRaid(ServerLevel level) {
        if (this.raidState == null) return;

        if (level.getGameTime() > this.raidState.getStartTime() + RAID_TIMEOUT_TICKS) {
            this.endRaid(level, false);
        }
        else if (this.raidState.raidersLeft() > 0) {
            this.raidState.updateRaiders();
        }
        else if (this.raidState.isFinalWave()) {
            this.endRaid(level, true);
        }
        else {
            if (this.nextWaveDelay-- < 0) {
                this.nextWaveDelay = NEXT_WAVE_DELAY;
                this.raidState.advanceWave();
                
                ServerPlayer player = WaveRaidUtil.findNearestPlayer(level, this.raidState.getCenter(), RAID_RADIUS);
                Vec3 waveCenter;
                if (player != null) {
                    waveCenter = WaveRaidUtil.findWaveSpawnLocation(level, player.position(), player.position());
                } else {
                    waveCenter = WaveRaidUtil.findWaveSpawnLocation(level, this.raidState.getCenter(), null);
                }
                if (waveCenter == null) {
                    SCGExtra.LOGGER.warn("Can't find wave spawn location");
                    return;
                }
                this.raidState.spawnCurrentWaveMobs(waveCenter, WAVE_SPAWN_RADIUS, player);
            }
        }
    }

    private void tickBossBar(ServerLevel level) {
        if (this.raidState == null) {
            if (this.bossBar != null) {
                this.bossBar.setProgress(0);
                this.bossBar.setVisible(false);
                this.bossBar = null;
            }
        } else {
            Component bossBarLabel = WaveRaidUtil.getBossBarLabel(this.raidState.getWaveRaidData(), this.raidState.getCurrentWave());
            if (this.bossBar == null) {
                this.bossBar = new ServerBossEvent(bossBarLabel, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
                this.bossBar.setProgress(1);
                this.bossBar.setVisible(true);
            }

            if (!this.bossBar.getName().getString().equals(bossBarLabel.getString())) {
                this.bossBar.setName(bossBarLabel);
            }
            this.bossBar.setProgress((float)this.raidState.raidersLeft() /this.raidState.getTotalWaveSpawned());

            if (level.getGameTime() % 10 == 1) {
                List<ServerPlayer> nearbyPlayers = level.getPlayers((player) -> {
                    if (player.isAlive() && !player.isRemoved() && !player.isSpectator()) {
                        double distance = player.position().distanceTo(this.raidState.getCenter());
                        return distance <= RAID_RADIUS;
                    } else {
                        return false;
                    }
                });
                // to avoid ConcurrentModificationException, maybe
                Collection<ServerPlayer> bossBarPlayers = new HashSet<>(this.bossBar.getPlayers());
                for(ServerPlayer player : bossBarPlayers) {
                    if (!nearbyPlayers.contains(player) || !player.isAlive() || player.isRemoved()) {
                        this.bossBar.removePlayer(player);
                    }
                }
                for(ServerPlayer player : nearbyPlayers) {
                    if (!bossBarPlayers.contains(player)) {
                        this.bossBar.addPlayer(player);
                    }
                }
            }
        }
    }

    public void endRaid(ServerLevel level, boolean success) {
        if (this.raidState == null) return;

        if (success) {
            WaveRaidUtil.announceToNearbyPlayers(level,
                    Component.translatable("raid.scguns.defeated"), this.raidState.getCenter(), 64);
            giveOrDropLoot(level, this.raidState);
        } else {
            this.raidState.discardRaiders();
            WaveRaidUtil.announceToNearbyPlayers(level,
                    Component.translatable("raid.scguns.failed"), this.raidState.getCenter(), 64);
        }
        this.raidState = null;
    }

    public static void giveOrDropLoot(ServerLevel level, WaveRaidState raidState) {
        List<ItemStack> loot = raidState.getWaveRaidData().rollLoot(level);
        ServerPlayer player = WaveRaidUtil.findNearestPlayer(level, raidState.getCenter(), RAID_RADIUS);
        if (player != null) {
            for (ItemStack stack : loot) {
                player.getInventory().placeItemBackInInventory(stack);
            }
        } else {
            for (ItemStack stack : loot) {
                Vec3 dropPos = raidState.getLootDropPos();
                ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
                level.addFreshEntity(entity);
            }
        }
    }

    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (event.level instanceof ServerLevel level) {
                WaveRaidManager manager = WaveRaidManager.get(level);
                manager.tick(level);
            }
        }
    }

    public static void onLevelLoad(LevelEvent.Load event) {
        INSTANCES.clear();
    }

    public boolean hasActiveRaid() {
        return this.raidState != null;
    }

    public @Nullable WaveRaidState getCurrentRaidState() {
        return this.raidState;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (this.raidState != null) {
            tag.put("RaidState", this.raidState.serialize());
        }
        return tag;
    }

    public static WaveRaidManager load(ServerLevel level, CompoundTag tag) {
        WaveRaidManager manager = new WaveRaidManager();
        if (tag.contains("RaidState")) {
            manager.raidState = WaveRaidState.deserialize(tag.getCompound("RaidState"), level);
        }
        return manager;
    }
}

