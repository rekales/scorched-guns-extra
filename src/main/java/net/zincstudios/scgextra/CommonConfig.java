package net.zincstudios.scgextra;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@SuppressWarnings("unused")
public class CommonConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Ability Enables
    private static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_ALERT = BUILDER
            .comment("enables alert ability, aggroing nearby mobs to the their targets")
            .define("enableAbilityAlert", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_FLARE = BUILDER
            .comment("enables flare ability, summoning 3 infantry mobs")
            .define("enableAbilityFlare", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_DIG = BUILDER
            .comment("enables dig ability, allowing mobs to destroy dirt like blocks")
            .define("enableAbilityDig", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_WEAKNESS = BUILDER
            .comment("enables the Weakness Exposed effect: enemies are stunned and take double damage")
            .define("enableAbilityWeakness", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_BULLETPROOF = BUILDER
            .comment("enables bulletproof capabilities, enables certain mobs to have bulletproof parts")
            .define("enableAbilityBulletproof", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_BAD_AIM = BUILDER
            .comment("enables bad aim state, certain mobs will fire guns with bad aim at the start but improves over time")
            // TODO: figure out if the disabled state would make accuracy constantly good or bad.
            .define("enableAbilityBadAim", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_WARNING = BUILDER
            .comment("enables warning state, flash of light appears before a massive attack. disabling would just remove the flash.")
            .comment("REQUIRES GAME RESTART")
            .define("enableAbilityWarning", true);


    private static final ForgeConfigSpec.IntValue ABILITY_ALERT_RANGE = BUILDER
            .comment("range for how far are mobs aggroed")
            .defineInRange("abilityAlertRange", 64, 1, 100);

    private static final ForgeConfigSpec.IntValue ABILITY_ALERT_COOLDOWN = BUILDER
            .comment("alert ability cooldown in seconds")
            .defineInRange("abilityAlertCooldown", 10, 1, 1000);


    private static final ForgeConfigSpec.IntValue ABILITY_FLARE_CHARGE = BUILDER
            .comment("flare ability cooldown in ticks")
            .defineInRange("abilityFlareCharge", 40, 1, 1000);

    private static final ForgeConfigSpec.IntValue ABILITY_FLARE_COOLDOWN = BUILDER
            .comment("flare ability cooldown in seconds")
            .defineInRange("abilityFlareCooldown", 10, 1, 1000);


    private static final ForgeConfigSpec.IntValue ABILITY_WEAKNESS_DURATION = BUILDER
            .comment("weakness exposed state duration in ticks")
            .defineInRange("abilityWeaknessDuration", 100, 1, 10000);

    private static final ForgeConfigSpec.IntValue ABILITY_WEAKNESS_COOLDOWN = BUILDER
            .comment("weakness exposed state cooldown in ticks")
            .defineInRange("abilityWeaknessCooldown", 200, 1, 10000);

    private static final ForgeConfigSpec.IntValue ABILITY_WEAKNESS_HEADSHOTS = BUILDER
            .comment("amount of headshots needed to trigger weakness exposed state")
            .defineInRange("abilityWeaknessHeadshots", 5, 1, 100);

    private static final ForgeConfigSpec.DoubleValue ABILITY_WEAKNESS_DAMAGE_MULT = BUILDER
            .comment("weakness exposed state incoming damage multiplier")
            .defineInRange("abilityWeaknessMult", 2, 0.1, 100);

    private static final ForgeConfigSpec.DoubleValue ABILITY_WEAKNESS_MIN = BUILDER
            .comment("percent threshold before weakness exposed is allowed to be triggered")
            .defineInRange("abilityWeaknessHealthThreshold", 100, 1.0, 100);

    public static final ForgeConfigSpec.IntValue WARZONE_SPAWN_WEIGHT = BUILDER
            .comment("spawn weight for the warzone biome")
            .defineInRange("warzoneSpawnWeight", 10, 1, 1000);

    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MINIMUM_TEMPERATURE = BUILDER
            .comment("""
                minimum spawn temperature for the warzone biome,
                Temperature: Determines if the biome is cold or hot, -1.0(Frozen) to 1.0(HOT)
                """)
            .defineInRange("warzoneSpawnMinimumTemperature", -1.0, -1.0, 1.0);
    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MAXIMUM_TEMPERATURE = BUILDER
            .comment("maximum spawn temperature for the warzone biome")
            .defineInRange("warzoneSpawnMaximumTemperature", 1.0, -1.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MINIMUM_HUMIDITY = BUILDER
            .comment("""
                minimum spawn humidity for the warzone biome
                Humidity: WET OR DRY, -1.0(desert, snowy biomes, etc) to 1.0(jungle, swamp etc)
                """)
            .defineInRange("warzoneSpawnMinimumHumidity", -1.0, -1.0, 1.0);
    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MAXIMUM_HUMIDITY = BUILDER
            .comment("maximum spawn humidity for the warzone biome")
            .defineInRange("warzoneSpawnMaximumHumidity", 1.0, -1.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MINIMUM_CONTINENTALNESS = BUILDER
            .comment("""
                minimum spawn continentalness for the warzone biome
                Continentalness: if it is closer to(even inside) or further than the ocean, -1.0(in ocean) to 1.0(deep in land)
                """)
            .defineInRange("warzoneSpawnMinimumContinentalness", -0.11, -1.0, 1.0);
    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MAXIMUM_CONTINENTALNESS = BUILDER
            .comment("maximum spawn continentalness for the warzone biome")
            .defineInRange("warzoneSpawnMaximumContinentalness", 1.0, -1.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MINIMUM_EROSION = BUILDER
            .comment("""
                minimum spawn erosion for the warzone biome
                Erosion: flatness, -1.0(not flat) to 1.0(very flat)
                """)
            .defineInRange("warzoneSpawnMinimumErosion", 0.45, -1.0, 1.0);
    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MAXIMUM_EROSION = BUILDER
            .comment("maximum spawn erosion for the warzone biome")
            .defineInRange("warzoneSpawnMaximumErosion", 1.0, -1.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MINIMUM_WEIRDNESS = BUILDER
            .comment("""
                minimum spawn weirdness for the warzone biome
                Weirdness: how weird a biome is, -1.0(not weird) to 1.0 (very weird)
            """)
            .defineInRange("warzoneSpawnMinimumWeirdness", -1.0, -1.0, 1.0);
    public static final ForgeConfigSpec.DoubleValue WARZONE_SPAWN_MAXIMUM_WEIRDNESS = BUILDER
            .comment("maximum spawn weirdness for the warzone biome")
            .defineInRange("warzoneSpawnMaximumWeirdness", 1.0, -1.0, 1.0);


    // Might not be possible actually
//    private static final ForgeConfigSpec.IntValue ABILITY_WARNING_LENGTH = BUILDER
//            .comment("how long would the warning flash be in ticks")
//            .defineInRange("abilityWeaknessHealthThreshold", 10, 4, 100);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableAbilityAlert;
    public static boolean enableAbilityFlare;
    public static boolean enableAbilityDig;
    public static boolean enableAbilityWeakness;
    public static boolean enableAbilityBulletproof;
    public static boolean enableAbilityBadAim;
    public static boolean enableAbilityWarning;

    public static int abilityAlertRange;
    public static int abilityAlertCooldown;

    public static int abilityFlareCharge;
    public static int abilityFlareCooldown;

    public static int abilityWeaknessDuration;
    public static int abilityWeaknessCooldown;
    public static int abilityWeaknessHeadshots;
    public static double abilityWeaknessDamageMult;
    public static double abilityWeaknessMinHealth;
    public static int warzoneSpawnWeight;
    public static double warzoneSpawnMaxTemp;
    public static double warzoneSpawnMinTemp;
    public static double warzoneSpawnMaxHumidity;
    public static double warzoneSpawnMinHumidity;
    public static double warzoneSpawnMaxContinentalness;
    public static double warzoneSpawnMinContinentalness;
    public static double warzoneSpawnMaxErosion;
    public static double warzoneSpawnMinErosion;
    public static double warzoneSpawnMaxWeirdness;
    public static double warzoneSpawnMinWeirdness;

    private static void updateConfigs() {
        enableAbilityAlert = ENABLE_ABILITY_ALERT.get();
        enableAbilityFlare = ENABLE_ABILITY_FLARE.get();
        enableAbilityDig = ENABLE_ABILITY_DIG.get();
        enableAbilityWeakness = ENABLE_ABILITY_WEAKNESS.get();
        enableAbilityBulletproof = ENABLE_ABILITY_BULLETPROOF.get();
        enableAbilityBadAim = ENABLE_ABILITY_BAD_AIM.get();
        enableAbilityWarning = ENABLE_ABILITY_WARNING.get();

        abilityAlertRange = ABILITY_ALERT_RANGE.get();
        abilityAlertCooldown = ABILITY_ALERT_COOLDOWN.get();

        abilityFlareCharge = ABILITY_FLARE_CHARGE.get();
        abilityFlareCooldown = ABILITY_FLARE_COOLDOWN.get();

        abilityWeaknessDuration = ABILITY_WEAKNESS_DURATION.get();
        abilityWeaknessCooldown = ABILITY_WEAKNESS_COOLDOWN.get();
        abilityWeaknessHeadshots = ABILITY_WEAKNESS_HEADSHOTS.get();
        abilityWeaknessDamageMult = ABILITY_WEAKNESS_DAMAGE_MULT.get();
        abilityWeaknessMinHealth = ABILITY_WEAKNESS_MIN.get();

        warzoneSpawnWeight = WARZONE_SPAWN_WEIGHT.get();
        warzoneSpawnMaxTemp = WARZONE_SPAWN_MAXIMUM_TEMPERATURE.get();
        warzoneSpawnMinTemp = WARZONE_SPAWN_MINIMUM_TEMPERATURE.get();
        warzoneSpawnMaxHumidity = WARZONE_SPAWN_MAXIMUM_HUMIDITY.get();
        warzoneSpawnMinHumidity = WARZONE_SPAWN_MINIMUM_HUMIDITY.get();
        warzoneSpawnMaxContinentalness = WARZONE_SPAWN_MAXIMUM_CONTINENTALNESS.get();
        warzoneSpawnMinContinentalness = WARZONE_SPAWN_MINIMUM_CONTINENTALNESS.get();
        warzoneSpawnMaxErosion = WARZONE_SPAWN_MAXIMUM_EROSION.get();
        warzoneSpawnMinErosion = WARZONE_SPAWN_MINIMUM_EROSION.get();
        warzoneSpawnMaxWeirdness = WARZONE_SPAWN_MAXIMUM_WEIRDNESS.get();
        warzoneSpawnMinWeirdness = WARZONE_SPAWN_MINIMUM_WEIRDNESS.get();
    }

    static void onLoad(final ModConfigEvent.Loading event) {
        updateConfigs();
    }

    static void onReload(final ModConfigEvent.Reloading event) {
        updateConfigs();
    }
}
