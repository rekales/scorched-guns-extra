package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import java.util.List;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class SlamAttackGoal extends Goal{

    public ArmoredWhaleEntity entity;
    private int cooldown = 0;
    private int ticks = 0;
    private int startTick = 0;

    public SlamAttackGoal(ArmoredWhaleEntity entity){
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        if(cooldown>0){
            --cooldown;
        }
        return cooldown <= 0 && entity.getTarget() != null && entity.getRandom().nextInt(100)<10;
    }

    @Override
    public void start() {
        startTick = 60;//ticks to give entity time to move towards the player
        ticks = 0;
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        return ticks <= startTick+60 && entity.getTarget() != null && entity.getTarget() instanceof Player && cooldown<=0;//~22 ticks for going up, ~22 ticks coming down and then apply the damage,the few ticks extra are just to be sure
    }

    @Override
    public void tick() {
        super.tick();
        ticks++;
        if(!entity.level().isClientSide){
            LivingEntity target = entity.getTarget();
            if (target == null) return;
            double dx = target.getX() - entity.getX();
            double dz = target.getZ() - entity.getZ();
            double dist = Math.sqrt(dx*dx + dz*dz);
            if(dist > 0){
                double speed = 0.3;
                entity.setDeltaMovement(
                    dx / dist * speed,
                    entity.getDeltaMovement().y(),
                    dz / dist * speed
                );
            }
        }
        if(ticks==startTick+1){
            this.entity.level().playSound(
                this.entity, 
                this.entity.blockPosition(), 
                WhalerSounds.WHALE_SLAM.get(), 
                SoundSource.MASTER, 
                2.0F, 
                1.0F
            );
            entity.triggerAnim("special", "slam");
        }
        if(ticks > startTick+10 && ticks<=startTick+30){
            entity.setDeltaMovement(
                entity.getDeltaMovement().x(), 
                0.3, 
                entity.getDeltaMovement().z()
            );
            entity.setDidSlam(true);
        }else if(ticks > startTick+30 && entity.onGround()){//only runs after the jump and once it's on the ground
            AABB fiveBlockRangeAABB = new AABB(
                entity.getX()-5,
                entity.getY()-5,
                entity.getZ()-5,
                entity.getX()+5,
                entity.getY()+5,
                entity.getZ()+5
            );
            AABB twelveBlockRangeAABB = new AABB(
                entity.getX()-12,
                entity.getY()-12,
                entity.getZ()-12,
                entity.getX()+12,
                entity.getY()+12,
                entity.getZ()+12
            );
            List<LivingEntity> entitiesInFiveBlocks = entity.level().getEntitiesOfClass(LivingEntity.class, fiveBlockRangeAABB, e -> !Faction.isFriendlies(entity, e) && !e.is(entity));
            List<LivingEntity> entitiesInTwelveBlocks = entity.level().getEntitiesOfClass(LivingEntity.class, twelveBlockRangeAABB, e -> !Faction.isFriendlies(entity, e) && !e.is(entity));
            // entities within 5 blocks get 30 damage,
            // entities within 12 blocks get 10 damage,

            // so i made the 5 block one deal 20 damage, and the 12 block one deals the remaining 10 damage to the 5 block range and the entities in the outer layer just gets the 10 damage, so i don't have to specifically make a outer box, yes I'm lazy

            for(LivingEntity en : entitiesInFiveBlocks){
                en.hurt(en.damageSources().mobAttack(entity), 20);
            }
            for(LivingEntity en : entitiesInTwelveBlocks){
                en.hurt(en.damageSources().mobAttack(entity), 10);
            }
            this.stop();
        }
    }

    @Override
    public void stop() {
        cooldown=600;//start cooldown after the whole attack finishes
        ticks = 0;
        startTick = 0;
        if(!this.entity.level().isClientSide){
            double radius = 12;
            for (int j = 0; j < 360; j += 10) {
                double rad = Math.toRadians(j);
                double x = this.entity.getX() + Math.cos(rad) * radius;
                double z = this.entity.getZ() + Math.sin(rad) * radius;
                ServerLevel level = (ServerLevel) this.entity.level();
                level.sendParticles(
                    ParticleTypes.SMOKE, 
                    x, 
                    this.entity.getY()+1,
                    z,
                    20,
                    0.4,
                    0.4,
                    0.4,
                    0
                );
            }
        }
    }
}