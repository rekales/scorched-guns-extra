package net.zincstudios.scgextra.entity.common.raid_summoner;

import java.util.ArrayList;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.zincstudios.scgextra.entity.rrc.RRCEntities;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;

public class RaidSummonerEntity extends Mob {
    private static final ArrayList<EntityType<?>> whalerElite = new ArrayList<>();
    private static final ArrayList<EntityType<?>> rrcElite = new ArrayList<>();
    private static final ArrayList<EntityType<?>> rrcInfantry = new ArrayList<>();
//    private static final ArrayList<EntityType<?>> facElite = new ArrayList<>();
//    private static final ArrayList<EntityType<?>> facInfantry = new ArrayList<>();
    private static boolean poolsInitialized = false;
    private static long lastRaidAttempt = 0;  // minecraft day calculated by  level.gameTime / 24000L

    public RaidSummonerEntity(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        initPools();
    }

    private static void initPools() {
        if (poolsInitialized) {
            return;
        }

        whalerElite.add(WhalerEntities.SALMONSAUR.get());
        whalerElite.add(WhalerEntities.TURTLEMAN.get());
        whalerElite.add(WhalerEntities.TENTACLIATOR.get());
        whalerElite.add(WhalerEntities.PUFFICUS.get());

        rrcElite.add(RRCEntities.SPRING_JUNKIE.get());
        rrcElite.add(RRCEntities.SCRAP_GUARD.get());
        rrcElite.add(RRCEntities.ARC_PSYCHO.get());
        rrcInfantry.add(RRCEntities.COPPER_KNIGHT.get());
        rrcInfantry.add(RRCEntities.TALLMAN.get());
        rrcInfantry.add(RRCEntities.SCOUT.get());

//        facElite.add(ModEntities.TRENCH_SNIPER.get());
//        facElite.add(ModEntities.SHOVEL_KNIGHT.get());
//        facElite.add(ModEntities.FAC_TANK_BUSTER.get());
//        facInfantry.add(ModEntities.FAC_TRENCHER.get());
//        facInfantry.add(ModEntities.FAC_BLUECOAT.get());
//        facInfantry.add(ModEntities.TRENCH_GOBLIN.get());

        poolsInitialized = true;
    }

    @Override
    public void tick() {
        super.tick();
        if(this.level().isClientSide())return;
        if(this.isInWater()){
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        if(this.level().getNearestPlayer(this, 200)==null){
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        int rand = this.random.nextInt(2);
        if(rand==0){
            this.spawnWhaler();
        } else if (rand == 1) {
            this.spawnRRC();
        }
        this.remove(RemovalReason.DISCARDED);
    }
    @Override
    public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
        long dayTime = pLevel.dayTime() % 24000;
        long currentDay = pLevel.dayTime() / 24000L;

        if (!(dayTime >= 13000 && dayTime < 23000)) return false;
        if (RaidSummonerEntity.lastRaidAttempt == 0) {
            RaidSummonerEntity.lastRaidAttempt = currentDay;
            return false;
        } else if (RaidSummonerEntity.lastRaidAttempt == currentDay) {
            return false;
        } else {
            RaidSummonerEntity.lastRaidAttempt = currentDay;
            return true;
        }
    }
    @Override
    public boolean checkSpawnObstruction(LevelReader pLevel) {
        return pLevel.isUnobstructed(this);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10);
        }
    private void spawnWhaler(){
        ArrayList<EntityType<?>> spawned = new ArrayList<>();
        ServerLevel sLevel = (ServerLevel)this.level();
        for(int i = 0; i < 5; i++){
            WhalerEntities.FISH_FOLK.get().spawn(sLevel, this.blockPosition(), MobSpawnType.MOB_SUMMONED);
        }
        for(int i = 0; i < 3; i++){
            EntityType<?> entity = whalerElite.get(this.random.nextInt(whalerElite.size()));
            while(spawned.contains(entity)){
                entity = whalerElite.get(this.random.nextInt(whalerElite.size()));
            }
            entity.spawn(sLevel, this.blockPosition(), MobSpawnType.MOB_SUMMONED);
            spawned.add(entity);
        }
    }
    private void spawnRRC(){
        ServerLevel sLevel = (ServerLevel)this.level();
        int eliteCount = this.random.nextInt(1, 3);
        int infantryCount = this.random.nextInt(1, 5);
        for(int i = 0; i < eliteCount; i++){
            rrcElite.get(this.random.nextInt(rrcElite.size())).spawn(sLevel, this.blockPosition(), getSpawnType());
        }
        for(int i = 0; i < infantryCount; i++){
            rrcInfantry.get(this.random.nextInt(rrcInfantry.size())).spawn(sLevel, this.blockPosition(), getSpawnType());
        }
    }

//    private void spawnFAC() {
//        ServerLevel sLevel = (ServerLevel)this.level();
//        int eliteCount = this.random.nextInt(1, 3);
//        int infantryPairs = this.random.nextInt(2, 5);
//        int infantryCount = infantryPairs * 2;
//
//        for (int i = 0; i < eliteCount; i++) {
//            facElite.get(this.random.nextInt(facElite.size())).spawn(sLevel, this.blockPosition(), getSpawnType());
//        }
//        for (int i = 0; i < infantryCount; i++) {
//            facInfantry.get(this.random.nextInt(facInfantry.size())).spawn(sLevel, this.blockPosition(), getSpawnType());
//        }
//        if (this.random.nextFloat() < 0.18F) {
//            ModEntities.FAC_COMMISSAR.get().spawn(sLevel, this.blockPosition(), getSpawnType());
//        }
//    }
}
