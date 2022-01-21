import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    java
    application
}

group = "de.jvstvshd.chillingfoxes"
version = "1.1.21"

val log4jVersion = "2.17.1"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    //kotlin
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2-native-mt")
    //implementation("io.github.qbosst:kordex-hybrid-commands:1.0.3-SNAPSHOT")

    //discord
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.1-SNAPSHOT")
    //implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.1-20211218.123243-29")
    implementation("com.sedmelluq:lavaplayer:1.3.78")

    //logging
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.slf4j:slf4j-api:1.7.32")

    //database
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.4")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.mybatis:mybatis:3.5.9")

    //(de)serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")

    //some other stuff
    implementation("org.kohsuke:github-api:1.301")
    implementation("org.jsoup:jsoup:1.14.3")

    //JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "de.jvstvshd.foxesbot.LauncherKt"
            attributes["Multi-Release"] = true

        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        exclude("org/apache/logging/log4j&core/lookup/JndiLookup.class")
    }

    compileKotlin {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        targetCompatibility = "17"
    }

    compileTestKotlin {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        targetCompatibility = "17"
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.6"
}