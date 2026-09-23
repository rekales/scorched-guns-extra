package net.zincstudios.scgextra.entity.whaler.guardian_statue;

import net.zincstudios.scgextra.entity.Faction;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.WhalerSounds;

import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

// NOTE: maybe make an abstract class that automatically gives access to the target entity in client if it gets common enough
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuardianStatueEntity extends Monster implements GeoEntity {
    boolean triedRemove = false;
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(GuardianStatueEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> GUARDIAN_LASER_ACTIVE_TIMER =
            SynchedEntityData.defineId(GuardianStatueEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> BEAM_ACTIVE_TIMER =
            SynchedEntityData.defineId(GuardianStatueEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Vector3f> BEAM_LOOK_POS =
            SynchedEntityData.defineId(GuardianStatueEntity.class, EntityDataSerializers.VECTOR3);

    public static final RawAnimation EFFECTS_BASE = RawAnimation.begin().then("effect.none", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final RawAnimation EYE_FLASH = RawAnimation.begin().then("effect.eye_flash", Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nonnull
    private Direction prefferedDirection = Direction.NORTH;

    @Nullable
    private LivingEntity clientSideCachedTarget;

    private final GuardianStatuePart[] subEntities;

    public GuardianStatueEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        GuardianStatuePart eye = new GuardianStatuePart(this, "eye", 1F, 0.5F);
        GuardianStatuePart body = new GuardianStatuePart(this, "body", 1.6F, 4.15F);
        GuardianStatuePart l_fin = new GuardianStatuePart(this, "l_fin", 1F, 0.8F);
        GuardianStatuePart r_fin = new GuardianStatuePart(this, "r_fin", 1F, 0.8F);
        GuardianStatuePart base = new GuardianStatuePart(this, "base", 3F, 2F);
        GuardianStatuePart head = new GuardianStatuePart(this, "head", 1.3F, 1.5F);
        this.subEntities = new GuardianStatuePart[]{ eye, body, l_fin, r_fin, base, head };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new BeamLaserAttackGoal(this, 400, 32));
        this.goalSelector.addGoal(4, new GuardianLaserAttackGoal(this));

        // Bosses will prioritize players and does not require line of sight to maintain targeting to avoid cheese
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        this.prefferedDirection = Direction.fromYRot(this.getYRot());
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 700)
                .add(Attributes.ARMOR, 12)
                .add(Attributes.FOLLOW_RANGE, 36)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Centering the entity to the block
            Vec3 oldPos = this.position();
            Vec3 newPos = BlockPos.containing(oldPos).getCenter();
            newPos = new Vec3(newPos.x, oldPos.y, newPos.z);
            if (!oldPos.equals(newPos)) {
                this.setPos(newPos);
            }
            //make gaurdian statue fall faster
            if (!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y - 0.08 * 3, this.getDeltaMovement().z);
            }
            //remove self if it's in ocean monument but not at the entrance
            if(this.tickCount%5==0){
                checkShouldDeleteSelf();
            }
        }
        updateSubentities();
    }

    @Override
    public void aiStep() {
        if (this.isAlive()) {
            LivingEntity target = this.getTarget();

            if (target != null) {
                if (this.hasLineOfSight(target)) {
                    this.getLookControl().setLookAt(target, 90.0F, 90.0F);
                }
            } else if (getBeamActiveTimer() <= 0) {
                Vec3 lookPos = new Vec3(
                        this.position().x + this.prefferedDirection.getNormal().getX()*8,
                        this.position().y,
                        this.position().z + this.prefferedDirection.getNormal().getZ()*8
                );
                this.getLookControl().setLookAt(lookPos.x, lookPos.y, lookPos.z, 1.0F, 1.0F);
            }
            this.setYRot(this.getYHeadRot());
            this.setYBodyRot(this.getYHeadRot());
        }

        super.aiStep();
    }

    public int getAttackDuration() {
        return 80;
    }

    public float getAttackAnimationScale(float partialTick) {
        return (this.getAttackDuration() - getGuardianLaserAttackTimer() + partialTick) / (float) this.getAttackDuration();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            target = null;
        }
        super.setTarget(target);
        this.entityData.set(TARGET_ID, target == null ? 0 : target.getId());
        this.playSound(WhalerSounds.GUARDIAN_STATUE_AGGRO.get());
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        if (this.level().isClientSide()) {
            if (this.clientSideCachedTarget != null) {
                return this.clientSideCachedTarget;
            } else {
                Entity entity = this.level().getEntity(this.entityData.get(TARGET_ID));
                if (entity instanceof LivingEntity livingEntity) {
                    this.clientSideCachedTarget = livingEntity;
                    return this.clientSideCachedTarget;
                } else {
                    return null;
                }
            }
        } else {
            return super.getTarget();
        }
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (TARGET_ID.equals(key)) {
            this.clientSideCachedTarget = null;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET_ID, 0);
        this.entityData.define(GUARDIAN_LASER_ACTIVE_TIMER, 0L);
        this.entityData.define(BEAM_ACTIVE_TIMER, 0L);
        this.entityData.define(BEAM_LOOK_POS, this.position().add(this.getLookAngle()).toVector3f());
    }

    public void startBeamActiveTimer(int ticks) {
        this.entityData.set(BEAM_ACTIVE_TIMER, this.level().getGameTime() + ticks);
    }

    public int getBeamActiveTimer() {
        int timer = (int) (this.entityData.get(BEAM_ACTIVE_TIMER) - this.level().getGameTime());
        return Math.max(timer, 0);
    }

    public void startGuardianLaserActiveTimer(int ticks) {
        this.entityData.set(GUARDIAN_LASER_ACTIVE_TIMER, this.level().getGameTime() + ticks);
    }

    public int getGuardianLaserAttackTimer() {
        int timer = (int) (this.entityData.get(GUARDIAN_LASER_ACTIVE_TIMER) - this.level().getGameTime());
        return Math.max(timer, 0);
    }

    public void setBeamLookPos(Vec3 pos) {
        this.entityData.set(BEAM_LOOK_POS, pos.toVector3f());
    }

    public Vec3 getBeamLookPos() {
        return new Vec3(this.entityData.get(BEAM_LOOK_POS));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "effects", 0,
                state -> state.setAndContinue(EFFECTS_BASE))
                        .triggerableAnim("eye_flash", EYE_FLASH)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader pLevel) {
        return pLevel.isUnobstructed(this);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
        return true;
    }

    // Krei, leave this as is for now, I'll probably come back to it to make it better
    private void checkShouldDeleteSelf(){
        if(
            this.getSpawnType() == MobSpawnType.SPAWN_EGG ||
            this.getSpawnType() == MobSpawnType.COMMAND ||
            this.getSpawnType() == MobSpawnType.DISPENSER
        ){
            return;
        }
        //ocean monuments sometimes spawn under ice, so when the guardian statue spawns and falls, it might land on ice instead, so remove self if it's on ice
        if(this.onGround() && !this.isInWater()){
            BlockPos temp = this.getOnPos();
            Set<BlockState> tempStates = new HashSet<>();
            for (int x = temp.getX() - 1; x <= temp.getX() + 1; x++) {
                for (int z = temp.getZ() - 1; z <= temp.getZ() + 1; z++) {
                    int y = temp.getY();
                    BlockPos pos = new BlockPos(x, y, z);
                    tempStates.add(level().getBlockState(pos));
                }
            }
            boolean foundIce = false;
            for(BlockState state : tempStates){
                if(state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE)){
                    foundIce = true;
                    break;
                }
            }
            if(foundIce){
                this.remove(RemovalReason.DISCARDED);
                return;
            }
        }
        if(this.isInWater() && this.onGround()){
            if(triedRemove){
                return;
            }
            triedRemove=true;
            ArrayList<BlockState> tempStates = new ArrayList<>();
            AABB box = new AABB(
                this.getX()-12,
                this.getY()-2,
                this.getZ()-12,
                this.getX()+12,
                this.getY()+2,
                this.getZ()+12
            );
            for(double x = box.minX; x < box.maxX; x++){
                for(double y = box.minY; y < box.maxY; y++){
                    for(double z = box.minZ; z < box.maxZ; z++){
                        tempStates.add(level().getBlockState(new BlockPos((int)x,(int)y,(int)z)));
                    }
                }
            }
            int lanternCount = 0;
            int prismarineBricksCount = 0;
            for(BlockState state : tempStates){
                if(state.is(Blocks.SEA_LANTERN))lanternCount++;
                if(state.is(Blocks.PRISMARINE_BRICKS))prismarineBricksCount++;
            }
            if(lanternCount<10){
                this.remove(RemovalReason.DISCARDED);
                return;
            }
            if(prismarineBricksCount>300){
                this.remove(RemovalReason.DISCARDED);
                return;
            }
        }
    }

    @SuppressWarnings("unused")
    public GuardianStatuePart[] getSubEntities() {
        return this.subEntities;
    }

    @Override
    public net.minecraftforge.entity.PartEntity<?>[] getParts() {
        return this.subEntities;
    }
    @Override
    public boolean isMultipartEntity() {
        return true;
    }
    public void updateSubentities(){
        for (GuardianStatuePart part : this.subEntities) {
            part.xo = part.getX();
            part.yo = part.getY();
            part.zo = part.getZ();
            part.xOld = part.getX();
            part.yOld = part.getY();
            part.zOld = part.getZ();
        }
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        float[] offsets = new float[] { 0.6f, 0F, 0F, 0F, 0F, 0F };
        float[] lateralOffsets = new float[] { 0F, 0F, 1.5f, -1.3F, 0F, 0F };
        double yawRad = Math.toRadians(this.getYRot());
        if(this.yHeadRotO!=this.yHeadRot){
            yawRad = Math.toRadians(this.getYHeadRot());
        }else if(this.yBodyRotO!=this.yBodyRot){
            yawRad = Math.toRadians(this.yBodyRot);
        }
        for (int i = 0; i < this.subEntities.length; i++) {
            GuardianStatuePart part = this.subEntities[i];
            float fDistance = offsets[i];
            float lateral = lateralOffsets[i];

            double offsetX = -Math.sin(yawRad) * fDistance + Math.cos(yawRad) * lateral;
            double offsetZ =  Math.cos(yawRad) * fDistance + Math.sin(yawRad) * lateral;
            //eye
            if(i==0){
                part.setPosRaw(x + offsetX, y+5.7, z + offsetZ);
            }
            //body
            else if(i == 1){
                part.setPosRaw(x + offsetX, y+1, z + offsetZ);
            }
            //l_fin && r_fin
            else if(i == 2 || i == 3){
                part.setPosRaw(x + offsetX, y+4.33, z + offsetZ);
            }
            //head
            else if(i == 5){
                part.setPosRaw(x + offsetX, y+5, z + offsetZ);
            }
            else{
                part.setPosRaw(x + offsetX, y, z + offsetZ);
            }
            part.setOldPosAndRot();
            part.refreshDimensions();
        }
    }


    // NOTE: Uses no flags, checks if there's a target instead
    public static class ResetLookToDirection extends Goal {

        protected final Mob mob;
        protected final float turnSpeed;
        private final Supplier<Direction> dirSupplier;

        public ResetLookToDirection(Mob mob, Supplier<Direction> dirSupplier, float turnSpeed) {
            this.mob = mob;
            this.dirSupplier = dirSupplier;
            this.turnSpeed = turnSpeed;
        }

        @Override
        public boolean canUse() {
            return this.mob.getTarget() == null;
        }

        @Override
        public void tick() {
            this.mob.setYRot(Mth.approachDegrees(
                    this.mob.getYRot(),
                    getPreferredDirection().toYRot(),
                    this.turnSpeed
            ));

            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
        }

        public Direction getPreferredDirection() {
            Direction dir = this.dirSupplier.get();
            if (dir == Direction.UP || dir == Direction.DOWN) return Direction.NORTH;
            return dir;
        }
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WhalerSounds.GUARDIAN_STATUE_HURT.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return WhalerSounds.GUARDIAN_STATUE_DEATH.get();
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(WhalerSounds.GUARDIAN_STATUE_WALK.get(), this.getSoundVolume() * 0.15F, this.getVoicePitch());
    }
}
