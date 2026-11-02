package net.zincstudios.scgextra.entity.common.brain;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.zincstudios.scgextra.entity.ModBrainMemories;

public class FlyCloseToTarget {

    public static <E extends Mob> BehaviorControl<E> create(float minDist, float maxDist, float speedModifier) {
        return BehaviorBuilder.create(builder ->
                builder.group(
                        builder.present(MemoryModuleType.ATTACK_TARGET),
                        builder.absent(ModBrainMemories.FLY_DIRECT_TARGET.get())
                ).apply(builder, (targetAcc, flyAcc) ->
                        ((level, mob, gameTime) -> {
                            flyAcc.setWithExpiry(new FlyDirectTarget(
                                    new EntityTracker(builder.get(targetAcc), false), speedModifier, minDist, maxDist
                            ), 100);
                            return true;
                        }))
        );
    }
}
