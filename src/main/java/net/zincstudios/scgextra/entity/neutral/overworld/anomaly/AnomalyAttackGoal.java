package net.zincstudios.scgextra.entity.neutral.overworld.anomaly;

import java.util.EnumSet;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public class AnomalyAttackGoal extends Goal{
   protected final AnomalyEntity mob;
   private final double speedModifier;
   private final boolean followingTargetEvenIfNotSeen;
   private Path path;
   private double pathedTargetX;
   private double pathedTargetY;
   private double pathedTargetZ;
   private int ticksUntilNextPathRecalculation;
   private int ticksUntilNextAttack;
   private long lastCanUseCheck;
   private int failedPathFindingPenalty = 0;
   private boolean canPenalize = false;
   private final int cooldownDuration;
   private int cooldown = 0;
   private boolean attacked = false;

   public AnomalyAttackGoal(AnomalyEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen, int cooldown) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
      this.cooldownDuration = cooldown;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      if(this.cooldown > 0)this.cooldown--;
      long i = this.mob.level().getGameTime();
      if (i - this.lastCanUseCheck < 20L) {
         return false;
      } else {
         this.lastCanUseCheck = i;
         LivingEntity livingentity = this.mob.getTarget();
         if (livingentity == null) {
            return false;
         } else if (!livingentity.isAlive()) {
            return false;
         } else if (this.canPenalize) {
            if (--this.ticksUntilNextPathRecalculation <= 0) {
               this.path = this.mob.getNavigation().createPath(livingentity, 0);
               this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
               return this.path != null && this.cooldown<=0;
            } else {
               return true && this.cooldown<=0;
            }
         } else {
            this.path = this.mob.getNavigation().createPath(livingentity, 0);
            if (this.path != null) {
               return true && this.cooldown<=0;
            } else {
               return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ()) && this.cooldown<=0;
            }
         }
      }
   }

   public boolean canContinueToUse() {
      LivingEntity livingentity = this.mob.getTarget();
      if (livingentity == null) {
         return false;
      } else if (!livingentity.isAlive()) {
         return false;
      } else if (!this.followingTargetEvenIfNotSeen) {
         return !this.mob.getNavigation().isDone() && !this.attacked;
      } else if (!this.mob.isWithinRestriction(livingentity.blockPosition())) {
         return false;
      } else {
         return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative() && !this.attacked;
      }
   }

   public void start() {
      this.mob.getNavigation().moveTo(this.path, this.speedModifier);
      this.mob.setAggressive(true);
      this.ticksUntilNextPathRecalculation = 0;
      this.ticksUntilNextAttack = 0;
      this.mob.setAttacking(true);
      this.cooldown = this.cooldownDuration;
      this.attacked = false;
   }

   public void stop() {
      LivingEntity livingentity = this.mob.getTarget();
      if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
         this.mob.setTarget((LivingEntity)null);
      }

      this.mob.setAggressive(false);
      this.mob.getNavigation().stop();
      this.mob.setAttacking(false);
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void tick() {
      LivingEntity livingentity = this.mob.getTarget();
      if (livingentity != null) {
         this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
         double d0 = this.mob.getPerceivedTargetDistanceSquareForMeleeAttack(livingentity);
         this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
         if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == (double)0.0F && this.pathedTargetY == (double)0.0F && this.pathedTargetZ == (double)0.0F || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= (double)1.0F || this.mob.getRandom().nextFloat() < 0.05F)) {
            this.pathedTargetX = livingentity.getX();
            this.pathedTargetY = livingentity.getY();
            this.pathedTargetZ = livingentity.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            if (this.canPenalize) {
               this.ticksUntilNextPathRecalculation += this.failedPathFindingPenalty;
               if (this.mob.getNavigation().getPath() != null) {
                  Node finalPathPoint = this.mob.getNavigation().getPath().getEndNode();
                  if (finalPathPoint != null && livingentity.distanceToSqr((double)finalPathPoint.x, (double)finalPathPoint.y, (double)finalPathPoint.z) < (double)1.0F) {
                     this.failedPathFindingPenalty = 0;
                  } else {
                     this.failedPathFindingPenalty += 10;
                  }
               } else {
                  this.failedPathFindingPenalty += 10;
               }
            }

            if (d0 > (double)1024.0F) {
               this.ticksUntilNextPathRecalculation += 10;
            } else if (d0 > (double)256.0F) {
               this.ticksUntilNextPathRecalculation += 5;
            }

            if (!this.mob.getNavigation().moveTo(livingentity, this.speedModifier)) {
               this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
         }

         this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
         this.checkAndPerformAttack(livingentity, d0);
      }

   }

   protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
      double d0 = this.getAttackReachSqr(enemy);
      if (distToEnemySqr <= d0 && this.ticksUntilNextAttack <= 0) {
         this.resetAttackCooldown();
         this.mob.swing(InteractionHand.MAIN_HAND);
         this.mob.doHurtTarget(enemy);
         this.attacked = true;
      }

   }

   protected void resetAttackCooldown() {
      this.ticksUntilNextAttack = this.adjustedTickDelay(20);
   }

   protected boolean isTimeToAttack() {
      return this.ticksUntilNextAttack <= 0;
   }

   protected int getTicksUntilNextAttack() {
      return this.ticksUntilNextAttack;
   }

   protected int getAttackInterval() {
      return this.adjustedTickDelay(20);
   }

   protected double getAttackReachSqr(LivingEntity attackTarget) {
      return 64;
   }
}
