package net.zincstudios.scgextra.entity.common.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoEntity;
import top.ribs.scguns.entity.ai.AIType;
import top.ribs.scguns.entity.ai.GunAttackGoal;

public class AnimatedGunAttackGoal<T extends PathfinderMob> extends GunAttackGoal<T>{

    public final String animController;
    public final String anim;
    public final int startAt;

    public AnimatedGunAttackGoal(T shooter, ItemStack gunStack, float speedModifier, AIType aiType, int difficulty, String animController, String anim, int startAt) {
        super(shooter, gunStack, speedModifier, aiType, difficulty);
        this.animController = animController;
        this.anim = anim;
        this.startAt = startAt;
    }

    @Override
    public void tick() {
        super.tick();
        if(this.attackTime == startAt && this.shooter instanceof GeoEntity ge){
            ge.triggerAnim(animController, anim);
        }
    }
    
}
