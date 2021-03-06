import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.0"
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "2.0.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.2.1"
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
    maven {
        name = "vault"
        url = uri("http://nexus.hc.to/content/repositories/pub_releases")
    }
    maven {
        name = "placeholderapi"
        url = uri("http://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    compile(kotlin("stdlib"))

    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("br.com.devsrsouza.kotlinbukkitapi:core:0.1.0-SNAPSHOT")
    compileOnly("br.com.devsrsouza.kotlinbukkitapi:plugins:0.1.0-SNAPSHOT")

    // plugins
    compileOnly("net.milkbowl.vault:VaultAPI:1.7") // Vault
    compileOnly("me.clip:placeholderapi:2.8.7") // PlaceholderAPI

    compile("com.zaxxer:HikariCP:3.2.0")
    compile("org.jetbrains.exposed:exposed:0.11.2")
    compile("redis.clients:jedis:2.9.0")
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

bukkit {
    main = "br.com.devsrsouza.souzaeconomy.SouzaEconomy"

    website = "https://github.com/DevSrSouza/SouzaEconomy"
    authors = listOf("DevSrSouza")

    depend = listOf("KotlinBukkitAPI")
    softDepend = listOf("Vault", "PlaceholderAPI")

    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

publishing {
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar)
            groupId = project.group.toString()
            artifactId = project.name.toLowerCase()
            version = project.version.toString()
            pom.withXml {
                asNode().apply {
                    appendNode(
                            "description",
                            "Bukkit multiple Economy plugin"
                    )
                    appendNode("name", project.name)
                    appendNode("url", "https://github.com/DevSrSouza/SouzaEconomy")

                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", "MIT License")
                        appendNode("url", "https://github.com/DevSrSouza/SouzaEconomy/blob/master/LICENSE")
                        appendNode("distribution", "repo")
                    }
                    appendNode("developers").apply {
                        appendNode("developer").apply {
                            appendNode("id", "DevSrSouza")
                            appendNode("name", "Gabriel Souza")
                            appendNode("email", "devsrsouza@gmail.com")
                        }
                    }
                    appendNode("scm").appendNode("url", "https://github.com/DevSrSouza/SouzaEconomy/tree/master")
                }
                asElement().apply {
                    removeChild(getElementsByTagName("dependencies").item(0))
                }
            }
        }
    }
}