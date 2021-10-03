plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.5.31"
}
repositories {
    mavenCentral()
}

val jrVersion = "2.12.0"

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:$jrVersion")
    implementation("com.fasterxml.jackson.jr:jackson-jr-stree:$jrVersion")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.electronwill.night-config:toml:3.6.3")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

sourceSets {
    main.get().java.srcDirs("src/main/kotlin")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-XXLanguage:+InlineClasses"
    }
}

gradlePlugin {
    plugins {
        create("mcgenPlugin") {
            id = "szewek.mcgen.mcgen-plugin"
            implementationClass = "szewek.mcgen.MCGenPlugin"
        }
    }
}
