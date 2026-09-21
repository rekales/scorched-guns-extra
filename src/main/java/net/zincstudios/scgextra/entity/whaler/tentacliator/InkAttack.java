package net.zincstudios.scgextra.entity.whaler.tentacliator;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.common.brain.PatchedEntityTracker;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.projectile.ink_projectile.InkProjectileEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class InkAttack extends Behavior<LivingEntity> {

    private final int cooldown;
    private final String animController;
    private final String animName;
    private final int aimThreshold;
    public static final String ABILITY_ID = "tentacliator_ink_attack";
    private long startTime = 0;  // gameTime timestamp
    private long cooldownEnd = 0;  // gameTime timestamp

    public InkAttack(int cooldown, String animController, String animName, int aimThreshold) {
        super(ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET, 
            MemoryStatus.VALUE_PRESENT,
            ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ), 15);
        this.cooldown = cooldown;
        this.animController = animController;
        this.animName = animName;
        this.aimThreshold = aimThreshold;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity owner) {
        return this.cooldownEnd < level.getGameTime();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get());
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"}) // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();

        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if(entity.distanceToSqr(target) > 256)return;

        if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new PatchedEntityTracker(target, true));
        }
        if(gameTime-this.startTime == 1)if(entity instanceof GeoEntity ge)ge.triggerAnim(this.animController, this.animName);

        if (gameTime-this.startTime >= this.aimThreshold) {
            if(entity instanceof TentacliatorEntity){
                InkProjectileEntity inkProjectile = new InkProjectileEntity(entity, level);
                Vec3 vec = entity.position().add(new Vec3(-5.5D/16D,20D/16D,2D/16D).yRot(-entity.yBodyRot * Mth.DEG_TO_RAD));
                inkProjectile.moveTo(
                    vec.x,
                    vec.y-1,
                    vec.z
                );
                double d0 = target.getX() - vec.x;
                double d1 = target.getY(0.333333333) - inkProjectile.getY();
                double d2 = target.getZ() - vec.z;
                double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                inkProjectile.shoot(
                    d0, 
                    d1 + d3 * (double)2F, 
                    d2, 
                    0.6F, 
                    (float)(14 - level.getDifficulty().getId() * 4));
                level.addFreshEntity(inkProjectile);
            }
        }
    }
    @Override
    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        this.startTime = gameTime;
        entity.getBrain().setMemoryWithExpiry(
                ModBrainMemories.ABILITY_STATE.get(),
                new AbilityState(ABILITY_ID, gameTime, gameTime + Behavior.DEFAULT_DURATION),
                Behavior.DEFAULT_DURATION
        );
    }

    @Override
    protected void stop(ServerLevel level, LivingEntity mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        this.cooldownEnd = gameTime + this.cooldown;
        brain.eraseMemory(ModBrainMemories.ABILITY_STATE.get());
    }
}
