plugins {
    id("idea")
    id("java")
    id("java-library")
    id("net.neoforged.moddev.legacyforge") version("2.0.107")
}

version = project.properties["mod_version"]!!
group = project.properties["mod_group"]!!

repositories {
    mavenLocal()
    maven("https://maven.neoforged.net/#/releases/")
    maven("https://www.cursemaven.com")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.minecraftforge.net/")  // TerraBlender
    maven("https://modmaven.dev") // JEI
    maven("https://maven.theillusivec4.top/") // Curios
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") // GeckoLib
}

dependencies {
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")!!)
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    modImplementation("curse.maven:scorchedguns-802940:7232063")  // 0.5.5
    modImplementation("curse.maven:framework-549225:5692398")  // 0.7.8

//    modImplementation("curse.maven:scorched-guns-oregunized-1447333:7540971")  // 1.0.0
//    modImplementation("curse.maven:oreganized-769203:7707202")  // 4.3.2
//    modImplementation("curse.maven:blueprint-382216:6408581")  // 7.1.3
//    modImplementation("curse.maven:scorched-guns-caverns-chasms-compat-1369675:7670670")  // 1.3.1a
//    modImplementation("curse.maven:caverns-and-chasms-438005:7858512")  // 2.1.0
//    modImplementation("curse.maven:yungs-api-421850:5769971")  // 4.0.6

    modImplementation("software.bernie.geckolib:geckolib-forge-${property("minecraft_version")}:${property("geckolib_version")}")
    modCompileOnly("top.theillusivec4.curios:curios-forge:${property("curios_version")}:api")
    modRuntimeOnly("top.theillusivec4.curios:curios-forge:${property("curios_version")}")
    modImplementation("com.github.glitchfiend:TerraBlender-forge:${property("minecraft_version")}-${property("terrablender_version")}")

    // Fallback
//    modImplementation("curse.maven:curios-309927:5367944")
//    modImplementation("curse.maven:geckolib-388172:7020955")

    // Dev QOL
    modRuntimeOnly("curse.maven:jei-238222:7270446")
    modRuntimeOnly("curse.maven:neat-238372:5838485")
    modRuntimeOnly("curse.maven:configured-457570:5180900")
    modRuntimeOnly("curse.maven:debug-utils-783008:4644077")
}

legacyForge {
    version = property("forge_version").toString()

    accessTransformers.from("src/main/resources/META-INF/accesstransformer.cfg")

    parchment {
        mappingsVersion = property("parchment_mappings_version")!!.toString()
        minecraftVersion = property("parchment_minecraft_version")!!.toString()
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "")
            systemProperty("forge.logging.console.level", "debug")
        }

        create("client") {
            client()
            systemProperty("forge.enabledGameTestNamespaces", property("mod_id")!!.toString())
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", property("mod_id")!!.toString())
        }

        create("data") {
            data()
            programArguments.addAll(
                "--mod", property("mod_id")!!.toString(),
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
    }

    mods {
        create(property("mod_id")!!.toString()) {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    config("${property("mod_id")}.mixins.json")
    add(sourceSets.main.get(), "${property("mod_id")}.refmap.json")
}

tasks.processResources {
    val props = project.providers.gradlePropertiesPrefixedBy("").get()
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") { expand(props) }
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.property("mod_id")}-forge")
        manifest.attributes("MixinConfigs" to "${rootProject.property("mod_id")}.mixins.json")
    }
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("src/generated/resources")
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}