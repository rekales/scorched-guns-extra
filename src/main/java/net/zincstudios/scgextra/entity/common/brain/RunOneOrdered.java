package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;
import java.util.Map;

/**
 * Like RunOne but ordered and no weight pairs
 * @param <E>
 */
public class RunOneOrdered<E extends LivingEntity> extends GateBehavior<E> {

    public RunOneOrdered(List<BehaviorControl<? super E>> behaviors) {
        this(ImmutableMap.of(), behaviors);
    }

    public RunOneOrdered(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, List<BehaviorControl<? super E>> behaviors) {
        super(entryCondition, ImmutableSet.of(), OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, createWeightPairs(behaviors));
    }

    private static <E extends LivingEntity> ImmutableList<Pair<? extends BehaviorControl<? super E>, Integer>> createWeightPairs(
            List<BehaviorControl<? super E>> behaviors) {
        ImmutableList.Builder<Pair<? extends BehaviorControl<? super E>, Integer>> builder = ImmutableList.builder();
        for(BehaviorControl<? super E> behaviorcontrol : behaviors) {
            builder.add(Pair.of(behaviorcontrol, 1));
        }
        return builder.build();
    }
}