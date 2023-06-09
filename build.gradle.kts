plugins {
    kotlin("jvm") version "1.8.20"
    application
    id("pl.allegro.tech.build.axion-release") version "1.15.0"
}

group = "gh.marad.tiler"
version = scmVersion.version

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/skija/maven")
}

dependencies {
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    implementation("org.greenrobot:eventbus-java:3.3.1")
    implementation("com.melloware:jintellitype:1.4.1")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.1")
    testImplementation("io.kotest:kotest-property:5.6.1")

    testImplementation("org.jetbrains.skija:skija-windows:0.93.6")
    testImplementation("org.lwjgl:lwjgl:3.3.2")
    testImplementation("org.lwjgl:lwjgl-opengl:3.3.2")
    testImplementation("org.lwjgl:lwjgl-glfw:3.3.2")
    testImplementation("org.lwjgl:lwjgl:3.3.2:natives-windows")
    testImplementation("org.lwjgl:lwjgl-opengl:3.3.2:natives-windows")
    testImplementation("org.lwjgl:lwjgl-glfw:3.3.2:natives-windows")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}