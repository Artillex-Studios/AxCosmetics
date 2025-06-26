plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta15"
}

group = "com.artillexstudios.axcosmetics"
version = "1.0.0"

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))
}

allprojects {
    repositories {
        mavenCentral()

        maven("https://jitpack.io/")
        maven("https://repo.artillex-studios.com/releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("com.gradleup.shadow")
    }

    dependencies {
        implementation("com.artillexstudios.axapi:axapi:1.4.717:all")
        implementation("dev.jorel:commandapi-bukkit-shade:10.0.0")
        implementation("dev.triumphteam:triumph-gui:3.1.12")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.0")
        compileOnly("com.artillexstudios.axvanish:axvanish:1.0.0:all")
        compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
        compileOnly("org.apache.commons:commons-lang3:3.14.0")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("commons-io:commons-io:2.16.1")
        compileOnly("it.unimi.dsi:fastutil:8.5.13")
        compileOnly("org.slf4j:slf4j-api:2.0.9")
        compileOnly("com.h2database:h2:2.3.232")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        relocate("com.github.benmanes", "com.artillexstudios.axcosmetics.libs.axapi.libs.caffeine")
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axcosmetics.libs.axapi")
        relocate("dev.jorel.commandapi", "com.artillexstudios.axcosmetics.libs.commandapi")
        relocate("dev.triumphteam.gui", "com.artillexstudios.axcosmetics.libs.triumphgui")
        relocate("org.h2", "com.artillexstudios.axcosmetics.libs.h2")
    }
}