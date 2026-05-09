plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.codeaura"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("com.intellij.java")
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.0")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "21"
    }
    buildSearchableOptions {
        enabled = false
    }
    runIde {
        jvmArgs("-Xms512m", "-Xmx2g", "-XX:+UseG1GC")
    }
    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("253.*")
    }
    test {
        useJUnitPlatform()
        workingDir = rootProject.projectDir
    }
}
