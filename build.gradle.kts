import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    apply {
        plugin("java")
        plugin("com.github.johnrengelman.shadow")
    }

    group = "xyz.acrylicstyle.expressions"
    version = "1.0.0-SNAPSHOT"

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
    }

    dependencies {
        implementation("xyz.acrylicstyle.java-util:expression:2.0.0-SNAPSHOT")
    }

    tasks {
        processResources {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE

            from(sourceSets.main.get().resources.srcDirs) {
                filter(ReplaceTokens::class, mapOf("tokens" to mapOf("version" to project.version.toString())))
                filteringCharset = "UTF-8"
            }
        }

        shadowJar {
            relocate("xyz.acrylicstyle.util", "xyz.acrylicstyle.expressions.libs.xyz.acrylicstyle.util")

            archiveBaseName.set("Expressions-${project.name}")
        }
    }
}
