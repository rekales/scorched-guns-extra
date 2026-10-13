package net.zincstudios.scgextra.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.zincstudios.scgextra.entity.EntityTypeTags;
import net.zincstudios.scgextra.entity.asgharian.AsgharianEntities;
import net.zincstudios.scgextra.entity.cog.COGEntities;
import net.zincstudios.scgextra.entity.fac.FACEntities;
import net.zincstudios.scgextra.entity.neutral.NeutralEntities;
import net.zincstudios.scgextra.entity.piglin.PiglinEntities;
import net.zincstudios.scgextra.entity.rrc.RRCEntities;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;
import net.zincstudios.scgextra.entity.wreckers.WreckersEntities;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModTags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModEntityTagProvider extends EntityTypeTagsProvider {

    public ModEntityTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EntityTypeTags.ASGHARIAN)
                .add(AsgharianEntities.FAILED_ONE.get())
                .add(AsgharianEntities.ASGHAR_SURGEON.get())
                .add(AsgharianEntities.ASGHAR_WORKER.get())
                .add(AsgharianEntities.ASGHAR_FLAMER.get())
                .add(AsgharianEntities.CANDLE_FIEND.get())
                .add(AsgharianEntities.SOUL_RIPPER.get())
                .add(top.ribs.scguns.init.ModEntities.DISSIDENT.get())
                .add(top.ribs.scguns.init.ModEntities.PRAETOR.get())
                .add(top.ribs.scguns.init.ModEntities.ADJUDICATOR.get())
                .add(top.ribs.scguns.init.ModEntities.SUBJUGATOR.get());

        tag(EntityTypeTags.COG)
                .add(top.ribs.scguns.init.ModEntities.COG_KNIGHT.get())
                .add(top.ribs.scguns.init.ModEntities.TRAUMA_UNIT.get())
                .add(top.ribs.scguns.init.ModEntities.COG_MINION.get())
                .add(top.ribs.scguns.init.ModEntities.SKY_CARRIER.get())
                .add(top.ribs.scguns.init.ModEntities.COG_KNIGHT.get())
                .add(top.ribs.scguns.init.ModEntities.SIGNAL_BEACON.get())
                .add(top.ribs.scguns.init.ModEntities.SCAMPLER.get())
                .add(top.ribs.scguns.init.ModEntities.SCAMP_TANK.get())
                .add(COGEntities.VULTURE.get())
                .add(COGEntities.DEVASTATOR.get())
                .add(COGEntities.BOMBARDIER.get())
                .add(COGEntities.GIGANTES.get())
                .add(COGEntities.VENATOR.get())
                .add(COGEntities.CENTIPEDE.get())
                .add(COGEntities.JUGGERNAUT.get())
                .add(COGEntities.TRENCHBLEEDER.get());

        tag(EntityTypeTags.FAC)
                .add(FACEntities.FAC_TRENCHER.get())
                .add(FACEntities.FAC_BLUECOAT.get())
                .add(FACEntities.TRENCH_GOBLIN.get())
                .add(FACEntities.TRENCH_SNIPER.get())
                .add(FACEntities.SHOVEL_KNIGHT.get())
                .add(FACEntities.FAC_TANK_BUSTER.get())
                .add(FACEntities.FAC_LION.get())
                .add(FACEntities.FAC_COMMISSAR.get())
                .add(FACEntities.FAC_WALKER.get())
                .add(FACEntities.FAC_TANK.get());

        tag(EntityTypeTags.WHALER)
                .add(WhalerEntities.FISH_FOLK.get())
                .add(WhalerEntities.TURTLEMAN.get())
                .add(WhalerEntities.SALMONSAUR.get())
                .add(WhalerEntities.TENTACLIATOR.get())
                .add(WhalerEntities.PUFFICUS.get())
                .add(WhalerEntities.GUARDIAN_STATUE.get())
                .add(WhalerEntities.ARMORED_WHALE.get())
                .add(WhalerEntities.LION_FISH.get())
                .add(top.ribs.scguns.init.ModEntities.FINFORCER.get());

        tag(EntityTypeTags.WRECKERS)
                .add(WreckersEntities.WRECKER_RED.get())
                .add(WreckersEntities.WRECKER_BLUE.get())
                .add(WreckersEntities.WRECKER_GREEN.get())
                .add(WreckersEntities.WRECKER_JUMBO.get())
                .add(WreckersEntities.WRECKER_HELICUBE.get())
                .add(WreckersEntities.WRECKER_TURRET_GUNNER.get())
                .add(WreckersEntities.WRECKER_DOZER.get())
                .add(WreckersEntities.WRECKER_BIKER.get());

        tag(EntityTypeTags.RRC)
                .add(RRCEntities.DRONE.get())
                .add(RRCEntities.TALLMAN.get())
                .add(RRCEntities.SCOUT.get())
                .add(RRCEntities.OPPRESSOR.get())
                .add(RRCEntities.SPRING_JUNKIE.get())
                .add(RRCEntities.SCRAP_GUARD.get())
                .add(RRCEntities.ARC_PSYCHO.get())
                .add(RRCEntities.FLAMING_HEAD.get())
                .add(RRCEntities.COPPER_KNIGHT.get())
                .add(RRCEntities.GARGOYLE.get());

        tag(EntityTypeTags.PIGLIN)
                .add(PiglinEntities.PIGLIN_NOB.get())
                .add(PiglinEntities.PIGLIN_STRIDER_RIDER.get())
                .add(PiglinEntities.PIGLIN_HOG_RIDER.get())
                .add(PiglinEntities.PIGLIN_BLAZE_KOPTER.get());

        tag(ModTags.Entities.CANNOT_BE_LACERATED)
                .add(WhalerEntities.GUARDIAN_STATUE.get())
                .add(AsgharianEntities.ASGHAR_WORKER.get())
                .add(FACEntities.FAC_TANK.get())
                .add(FACEntities.FAC_WALKER.get())
                .add(NeutralEntities.HEAD_HUNTER.get())
                .add(WreckersEntities.WRECKER_HELICUBE.get())
                .add(WreckersEntities.WRECKER_TURRET_GUNNER.get())
                .add(WreckersEntities.WRECKER_DOZER.get())
                .add(COGEntities.VULTURE.get())
                .add(COGEntities.DEVASTATOR.get())
                .add(COGEntities.BOMBARDIER.get())
                .add(COGEntities.GIGANTES.get())
                .add(COGEntities.VENATOR.get())
                .add(COGEntities.CENTIPEDE.get())
                .add(COGEntities.JUGGERNAUT.get())
                .add(COGEntities.TRENCHBLEEDER.get());

        tag(ModTags.Entities.WATER)
                .add(WhalerEntities.FISH_FOLK.get())
                .add(WhalerEntities.TURTLEMAN.get())
                .add(WhalerEntities.SALMONSAUR.get())
                .add(WhalerEntities.GUARDIAN_STATUE.get())
                .add(WhalerEntities.TENTACLIATOR.get())
                .add(WhalerEntities.PUFFICUS.get())
                .add(NeutralEntities.ANOMALY.get())
                .add(WhalerEntities.ARMORED_WHALE.get());

        tag(ModTags.Entities.HEAVY)
                .add(WhalerEntities.TURTLEMAN.get())
                .add(WhalerEntities.SALMONSAUR.get())
                .add(FACEntities.FAC_LION.get())
                .add(FACEntities.FAC_WALKER.get())
                .add(WreckersEntities.WRECKER_JUMBO.get())
                .add(WreckersEntities.WRECKER_DOZER.get())
                .add(COGEntities.DEVASTATOR.get())
                .add(COGEntities.BOMBARDIER.get())
                .add(COGEntities.GIGANTES.get())
                .add(COGEntities.VENATOR.get())
                .add(COGEntities.CENTIPEDE.get())
                .add(COGEntities.JUGGERNAUT.get())
                .add(COGEntities.TRENCHBLEEDER.get())
                .add(PiglinEntities.PIGLIN_NOB.get())
                .add(PiglinEntities.PIGLIN_HOG_RIDER.get())
                .add(PiglinEntities.PIGLIN_BLAZE_KOPTER.get());

        tag(ModTags.Entities.VERY_HEAVY)
                .add(WhalerEntities.GUARDIAN_STATUE.get())
                .add(WhalerEntities.ARMORED_WHALE.get())
                .add(FACEntities.FAC_TANK.get())
                .add(NeutralEntities.END_STONE_CRAB.get());

        tag(ModTags.Entities.BOT)
                .add(AsgharianEntities.ASGHAR_WORKER.get())
                .add(FACEntities.FAC_WALKER.get())
                .add(FACEntities.FAC_TANK.get())
                .add(WreckersEntities.WRECKER_HELICUBE.get())
                .add(WreckersEntities.WRECKER_TURRET_GUNNER.get())
                .add(WreckersEntities.WRECKER_DOZER.get())
                .add(COGEntities.VULTURE.get())
                .add(COGEntities.DEVASTATOR.get())
                .add(COGEntities.BOMBARDIER.get())
                .add(COGEntities.GIGANTES.get())
                .add(COGEntities.VENATOR.get())
                .add(COGEntities.CENTIPEDE.get())
                .add(COGEntities.JUGGERNAUT.get())
                .add(COGEntities.TRENCHBLEEDER.get());

        tag(ModTags.Entities.WHITE_BLOOD)
                .add(WhalerEntities.GUARDIAN_STATUE.get());

        tag(ModTags.Entities.BLACK_BLOOD)
                .add(NeutralEntities.HEAD_HUNTER.get())
                .add(FACEntities.FAC_WALKER.get())
                .add(FACEntities.FAC_TANK.get())
                .add(COGEntities.VULTURE.get())
                .add(COGEntities.DEVASTATOR.get())
                .add(COGEntities.BOMBARDIER.get())
                .add(COGEntities.GIGANTES.get())
                .add(COGEntities.VENATOR.get())
                .add(COGEntities.CENTIPEDE.get())
                .add(COGEntities.JUGGERNAUT.get());

        tag(ModTags.Entities.GREEN_BLOOD)
                .add(NeutralEntities.END_STONE_CRAB.get())
                .add(NeutralEntities.END_DWELLER.get());

        tag(ModTags.Entities.PURPLE_BLOOD)
                .add(NeutralEntities.END_POD.get())
                .add(NeutralEntities.END_SCORPION.get());

        tag(ModTags.Entities.YELLOW_BLOOD)
                .add(NeutralEntities.NITRO_BEETLE.get());

         tag(ModTags.Entities.FIRE)
                .add(NeutralEntities.HEAD_HUNTER.get())
                .add(NeutralEntities.NETHERITE_EATER.get())
                .add(NeutralEntities.NITRO_BEETLE.get())
                .add(PiglinEntities.PIGLIN_NOB.get())
                .add(PiglinEntities.PIGLIN_STRIDER_RIDER.get())
                .add(PiglinEntities.PIGLIN_HOG_RIDER.get())
                .add(PiglinEntities.PIGLIN_BLAZE_KOPTER.get());

        tag(ModTags.Entities.UNDEAD)
                .add(NeutralEntities.BIG_LUMP.get())
                .add(NeutralEntities.INFLICTED_BOAR.get())
                .add(NeutralEntities.INFLICTED_WOLF.get());

        tag(ModTags.Entities.WITHER)
                .add(NeutralEntities.HEAD_HUNTER.get());
        }
}
