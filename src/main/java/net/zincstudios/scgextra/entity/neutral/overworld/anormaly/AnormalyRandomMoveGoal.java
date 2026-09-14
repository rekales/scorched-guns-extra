package net.zincstudios.scgextra.entity.neutral.overworld.anormaly;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;

public class AnormalyRandomMoveGoal extends Goal {
    private final AnormalyEntity anormaly;

    public AnormalyRandomMoveGoal(AnormalyEntity entity) {
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.anormaly = entity;
    }

    public boolean canUse() {
        return anormaly.goalSelector.getRunningGoals().anyMatch((e)->{
            if(e.getGoal() instanceof MoveTowardsTargetGoal || e.getGoal() instanceof AnormalyAttackGoal){
                return false;
            }
            return true;
        }) && !anormaly.getMoveControl().hasWanted()
                && anormaly.getRandom().nextInt(reducedTickDelay(7)) == 0;
    }

    public boolean canContinueToUse() {
        return false;
    }

    public void tick() {
        BlockPos blockpos = anormaly.blockPosition();

        for (int i = 0; i < 3; ++i) {
            BlockPos blockpos1 = blockpos.offset(anormaly.getRandom().nextInt(15) - 7,
                    anormaly.getRandom().nextInt(11) - 5, anormaly.getRandom().nextInt(15) - 7);
            if (anormaly.level().isEmptyBlock(blockpos1)) {
                anormaly.getMoveControl().setWantedPosition((double) blockpos1.getX() + (double) 0.5F,
                        (double) blockpos1.getY() + (double) 0.5F, (double) blockpos1.getZ() + (double) 0.5F,
                        (double) 0.25F);
                if (anormaly.getTarget() == null) {
                    anormaly.getLookControl().setLookAt((double) blockpos1.getX() + (double) 0.5F,
                            (double) blockpos1.getY() + (double) 0.5F, (double) blockpos1.getZ() + (double) 0.5F,
                            180.0F, 20.0F);
                }
                break;
            }
        }

    }
}