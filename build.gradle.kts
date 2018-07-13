import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.50"
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "2.0.3"
}

group = "br.com.devsrsouza"
version = "0.0.1-SNAPSHOT"

repositories {
    jcenter()
    mavenLocal()
    maven {
        name = "spigot"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven {
        name = "exposed"
        url = uri("https://dl.bintray.com/kotlin/exposed")
    }
}

dependencies {
    compile(kotlin("stdlib"))

    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("br.com.devsrsouza:kotlinbukkitapi:0.1.0-SNAPSHOT")

    compile("com.zaxxer:HikariCP:3.2.0")
    compile("org.jetbrains.exposed:exposed:0.10.3")
}

tasks {
    "compileKotlin"(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    "shadowJar"(ShadowJar::class) {
        baseName = project.name
        classifier = ""
    }
}