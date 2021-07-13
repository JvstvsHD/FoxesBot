plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "de.jvstvshd"
version = "1.0.0-alpha.5"

val jdaVersion = "4.3.0_295"
val log4jVersion = "2.14.1"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://eldonexus.de/repository/maven-public")
}

dependencies {
    implementation("net.dv8tion", "JDA", jdaVersion)
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.google.code.gson:gson:2.8.7")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:2.1.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    implementation("net.hypixel:HypixelAPI:3.0.0")
    implementation("de.chojo", "cjda-util", "1.4.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    destinationDirectory.set(file("C:\\IntelliJ\\Projects\\FoxesBot\\jars"))
    manifest {
        attributes["Main-Class"] = "de.jvstvshd.foxesbot.Launcher"
        attributes["Multi-Release"] = true
    }
}