plugins {
    id("net.neoforged.moddev") version "2.0.147"
    alias(libs.plugins.shadow)
}

val shade: Configuration by configurations.creating

neoForge {
    version = "26.1.2.109"

    runs {
        create("client") {
            client()
        }

        create("server") {
            server()
        }
    }

    mods {
        // define mod <-> source bindings
        // these are used to tell the game which sources are for which mod
        // multi mod projects should define one per mod
        create("signedvelocity") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    shadeModule(projects.signedvelocityBackendCommon)
    shadeModule(projects.signedvelocityShared)
}

fun DependencyHandlerScope.shadeModule(module: ProjectDependency) {
    shade(module) {
        isTransitive = false
    }
    implementation(module) {
        isTransitive = false
    }
}

tasks {
    shadowJar {
        configurations = listOf(shade)
        archiveFileName.set("${rootProject.name}-NeoForge-${project.version}.jar")
        destinationDirectory.set(file("${rootProject.projectDir}/build"))
    }
    processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }
}

java {
    withSourcesJar()
}
