package net.zincstudios.scgextra.item;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.piglin.piglin_nob.PiglinNobEntity;
import net.zincstudios.scgextra.sounds.AsgharianSounds;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModTags;

public class NetheriteChainAxeItem extends AxeItem implements HurtEffects, GeoItem{
    private final float attackDamageBaseline;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   protected static final UUID BLOCK_REACH_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C0F433DB5CF");

   private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("saw");

    public NetheriteChainAxeItem(Tier tier, float attackDamageModifier, float attackSpeedModifier,
            Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        this.attackDamageBaseline = attackDamageModifier + tier.getAttackDamageBonus();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)attackSpeedModifier, Operation.ADDITION));
        builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BLOCK_REACH_UUID, "Tool modifier", (double)-2.0, Operation.ADDITION));
        this.defaultModifiers = builder.build();
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(!attacker.level().isClientSide()){
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), AsgharianSounds.ASGHAR_WORKER_SAW.get(), SoundSource.NEUTRAL, 1F, 1F);
        }
        this.hurtEffect(stack, target, attacker);
        target.hurt(attacker.damageSources().mobAttack(attacker), 11);
        stack.hurtAndBreak(2, attacker, (p_41007_) -> {
            if(p_41007_ instanceof PiglinNobEntity){
                p_41007_.broadcastBreakEvent(EquipmentSlot.OFFHAND);
            }
            else{
                p_41007_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            }
        });
        return true;
    }

    @Override
    public void hurtEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide() && !target.getType().is(ModTags.Entities.CANNOT_BE_LACERATED)) {
            target.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 200));
        }
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200));
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "saw", state->{
            state.setAnimation(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new SimpleCustomRenderer(new GeoItemRenderer<>(
                new DefaultedItemGeoModel<>(SCGExtra.asResource("netherite_chain_axe"))
        )));
    }
    
}
