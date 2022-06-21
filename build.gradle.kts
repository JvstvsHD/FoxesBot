import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    application
    kotlin("plugin.serialization") version ("1.6.21")
}

group = "de.jvstvshd.chillingfoxes"
version = "1.4.0"

val log4jVersion = "2.17.2"
val exposedVersion = "0.38.2"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://m2.dv8tion.net/releases")
    maven("https://eldonexus.de/repository/maven-public")
}

dependencies {
    //kotlin
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2-native-mt")
    //implementation("io.github.qbosst:kordex-hybrid-commands:1.0.3-SNAPSHOT")

    //discord
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.2-SNAPSHOT")
    implementation("com.sedmelluq:lavaplayer:1.3.78")

    //logging
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.slf4j:slf4j-api:1.7.36")

    //database
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.4")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.mybatis:mybatis:3.5.10")
    implementation("de.chojo:sql-util:1.4.3")

    //Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    //some other stuff
    implementation("org.kohsuke:github-api:1.306")
    implementation("org.jsoup:jsoup:1.15.1")
    implementation("com.notkamui.libs:keval:0.8.0")

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
            attributes["Main-Class"] = "de.jvstvshd.chillingfoxes.foxesbot.LauncherKt"
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