package net.zincstudios.scgextra.entity.rrc.spring_junkie;

import java.util.EnumSet;
import java.util.List;

import org.joml.Vector3f;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.sounds.RRCSounds;
import top.ribs.scguns.init.ModEffects;

public class AttackAndExplodeGoal extends Goal{

    private final SpringJunkieEntity parent;
    private final float speedModifier;
    private final double explodeDistance;
    private int explodeTimer = -1;
    private int fuseTimer = -1;
    private boolean exploding = false;
    private int agroAnimTimer = 0;

    public AttackAndExplodeGoal(SpringJunkieEntity mob, float speed, double distance){
        parent = mob;
        speedModifier = speed;
        explodeDistance = distance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return parent.getTarget()!=null;
    }
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    @Override
    public void start() {
        super.start();
        this.explodeTimer = -1;
        this.fuseTimer = -1;
        this.exploding = false;
        this.agroAnimTimer = 40;
        this.parent.setAttacking(true);
        this.parent.triggerAnim("anim", "aggroed");
        this.parent.playSound(RRCSounds.RRC_SPRING_JUNKIE_SCREAM.get(), this.parent.getSoundVolume()*3F, this.parent.getVoicePitch());
    }
    @Override
    public void tick() {
        super.tick();
        if(this.agroAnimTimer > 0){
            this.agroAnimTimer--;
            if(!this.parent.level().isClientSide()){
                ServerLevel sl = (ServerLevel) this.parent.level();
                sl.sendParticles(
                        ParticleTypes.SMOKE,
                        this.parent.getX(),
                        this.parent.getY()+1,
                        this.parent.getZ(),
                        30,
                        0.3,
                        0.1,
                        0.3,
                        0.05
                );
            }
            return;
        }
        LivingEntity target = this.parent.getTarget();
        if(this.explodeTimer>0){
            this.explodeTimer--;
            this.parent.getNavigation().stop();
            if(this.explodeTimer==4){
                if(!this.parent.level().isClientSide()){
                    List<LivingEntity> entities = this.parent.level().getEntitiesOfClass(LivingEntity.class,  this.parent.getBoundingBox().inflate(explodeDistance));
                    for(LivingEntity entity : entities){
                        entity.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 100));
                    }
                    ServerLevel l = (ServerLevel)this.parent.level();
                    l.sendParticles( ParticleTypes.EXPLOSION, this.parent.getX(), this.parent.getY()+1, this.parent.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
                    this.parent.level().explode( this.parent, this.parent.getX(), this.parent.getY(), this.parent.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
                }
            }
            if(this.explodeTimer == 5){
                if(!this.parent.level().isClientSide()){
                    AABB aabb = new AABB(
                        this.parent.getX()-5,
                        this.parent.getY(),
                        this.parent.getZ()-5,
                        this.parent.getX()+5,
                        this.parent.getY()+5,
                        this.parent.getZ()+5
                    );
                    for(int j = 0; j < 6; j++){
                        double tx = Mth.lerp(this.parent.getRandom().nextDouble(), aabb.minX, aabb.maxX);
                        double ty = Mth.lerp(this.parent.getRandom().nextDouble(), aabb.minY, aabb.maxY);
                        double tz = Mth.lerp(this.parent.getRandom().nextDouble(), aabb.minZ, aabb.maxZ);
                        Vec3 start = this.parent.position();
                        Vec3 end = new Vec3(tx, ty, tz);
    
                        Vec3 dir = end.subtract(start);
                        double length = dir.length();
                        Vec3 step = dir.normalize().scale(0.3);
                        ServerLevel l = (ServerLevel)this.parent.level();
                        Vec3 current = start;
                        for (double i = 0; i < length; i += 0.3) {
                            l.sendParticles(
                                new DustParticleOptions(new Vector3f(1.0F, 1.0F, 0.0F), 0.8F),
                                current.x, current.y, current.z,
                                1,
                                0, 0, 0,
                                0.0D
                            );
                            current = current.add(step);
                        }
                    }
                }
            }
        }
        else if(this.explodeTimer==0){
            this.parent.remove(RemovalReason.DISCARDED);
        }
        else if(this.explodeTimer<0){
            if(target!=null){
                if(!(this.parent.distanceToSqr(target)<=(explodeDistance*explodeDistance))){
                    this.parent.getNavigation().moveTo(target, speedModifier);
                    this.parent.getMoveControl().setWantedPosition(
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        speedModifier
                    );
                }else{
                    if(!exploding){
                        fuseTimer = 60;
                    }
                    exploding = true;
                }
            }
        }
        if(this.fuseTimer>0){
            if(this.fuseTimer==60){
                this.parent.triggerAnim("anim1", "aggroed_long");
                this.parent.playSound(
                    RRCSounds.RRC_SPRING_JUNKIE_LAUGH.get(), 
                    this.parent.getSoundVolume(), 
                    this.parent.getVoicePitch()
                );
            }
            this.fuseTimer--;
        }else if(this.fuseTimer==0){
            this.parent.triggerAnim("behaviour", "death");
            this.parent.playSound(
                    RRCSounds.RRC_SPRING_JUNKIE_DEATH.get(),
                    this.parent.getSoundVolume(), 
                    this.parent.getVoicePitch()
                );
            this.explodeTimer = 8;
            fuseTimer--;
        }
    }
    @Override
    public void stop() {
        super.stop();
        this.parent.setAttacking(false);
        if(this.explodeTimer>0){
            this.parent.remove(RemovalReason.DISCARDED);
        }
    }
}