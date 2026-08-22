package net.zincstudios.scgextra.entity.common;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.mixin.GunAttackGoalAccessor;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.ai.GunAttackGoal;

// TODO: consider synced data for target entity too.
// If so, consider updating client-side rotation to look at the target to mitigate minecraft's shitty ai look controls.

// TODO: consider manipulating goal to hold fire.

// TODO: consider extensions for mobs with non-item guns.

/**
 * Base class for entities that uses guns and the GunAttackGoal goal.
 * Used for handling and fetching states from the aforementioned goal.
 * @see GunAttackGoal
 *
 * @deprecated Extend EquippedEntity and use brain instead
 */
@SuppressWarnings("unused")
@Deprecated
public abstract class GunnerEntity extends EquippedEntity {

    private static final EntityDataAccessor<Boolean> AIMING =
            SynchedEntityData.defineId(GunnerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int CACHE_UPDATE_INTERVAL = 100;

    @SuppressWarnings("FieldMayBeFinal")
    @Nullable private GunAttackGoal<?> gunAttackGoalCache = null;  // server-side only

    protected GunnerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    protected GunnerEntity(EntityType<? extends Monster> entity, Level level, ResourceLocation equipmentResLoc) {
        super(entity, level, equipmentResLoc);
    }

    public @Nullable GunAttackGoal<?> getGunAttackGoal() {
        if (this.level().isClientSide()) return null;

        if (this.gunAttackGoalCache != null && this.tickCount%CACHE_UPDATE_INTERVAL != 0) {
            return this.gunAttackGoalCache;
        } else {
            for(WrappedGoal goal : this.goalSelector.getAvailableGoals()){
                if(goal.getGoal() instanceof GunAttackGoal<?> gunAttackGoal){
                    return gunAttackGoal;
                }
            }
            return null;
        }
    }

    private void updateAimStates() {
        GunAttackGoal<?> gunAttackGoal = getGunAttackGoal();
        if (gunAttackGoal == null) return;
        if (gunAttackGoal instanceof GunAttackGoalAccessor accessor) {
            int aimingStabilityTimer = accessor.getAimingStabilityTimer();
            this.entityData.set(AIMING, aimingStabilityTimer > 0);
        }
    }

    // Can be discarded if we control setAggressive on GunAttackGoalProperly
    public boolean isAiming() {
        return this.entityData.get(AIMING);
    }

    // Called on server when firing a gun, primarily used for animation triggers
    public void onGunAttack(LivingEntity target, ItemStack itemStack) {
    }

    public Vec3 getProjectileSpawnOffset() {
        return Vec3.ZERO;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AIMING, false);
    }

    @Override
    public void tick() {
        super.tick();
        updateAimStates();
    }
}
