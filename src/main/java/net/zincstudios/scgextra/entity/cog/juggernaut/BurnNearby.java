package net.zincstudios.scgextra.entity.cog.juggernaut;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.BiPredicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BurnNearby extends Behavior<LivingEntity> {

    private final BiPredicate<LivingEntity, LivingEntity> shouldBurn;

    public BurnNearby(float radius) {
        this(((entity, target) -> {
            double yDiff = Math.abs(entity.getY() - target.getY());
            return entity.distanceTo(target) <= radius && yDiff <= radius/2;
        }));
    }

    public BurnNearby(BiPredicate<LivingEntity, LivingEntity> shouldBurn) {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ));
        this.shouldBurn = shouldBurn;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        if (gameTime%5 != 1) return;
        List<LivingEntity> entities = entity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
        for (LivingEntity target : entities) {
            if (shouldBurn.test(entity, target)) {
                target.setSecondsOnFire(5);
                target.hurt(entity.damageSources().onFire(), 2.0F);
            }
        }
    }
}
