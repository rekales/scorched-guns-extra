package net.zincstudios.scgextra.entity.cog.juggernaut;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BurnNearby extends Behavior<Mob> {

    private final BiPredicate<Mob, LivingEntity> shouldBurn;

    public BurnNearby(float radius) {
        this(((mob, target) -> {
            double yDiff = Math.abs(mob.getY() - target.getY());
            return mob.distanceTo(target) <= radius && yDiff <= radius/2;
        }));
    }

    public BurnNearby(BiPredicate<Mob, LivingEntity> shouldBurn) {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ));
        this.shouldBurn = shouldBurn;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        if (gameTime%5 != 1) return;
        List<LivingEntity> entities = mob.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
        for (LivingEntity target : entities) {
            if (shouldBurn.test(mob, target)) {
                target.setSecondsOnFire(5);
                target.hurt(mob.damageSources().onFire(), 2.0F);
            }
        }
    }
}
