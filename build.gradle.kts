import dev.kordex.gradle.plugins.kordex.DataCollection

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
    idea
    id("org.cadixdev.licenser") version "0.6.1"
    id("dev.kordex.gradle.kordex") version "1.5.6"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

group = "de.jvstvshd.chillingfoxes"
version = "1.5.0-SNAPSHOT"

val log4jVersion = "2.24.1"
val exposedVersion = "0.56.0"

repositories {
    mavenCentral()
    maven("https://snapshots-repo.kordex.dev")
    maven("https://releases-repo.kordex.dev")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://m2.dv8tion.net/releases")
    maven("https://eldonexus.de/repository/maven-public")
}

kordEx {
    bot {
        dataCollection(DataCollection.Standard)

        mainClass = "de.jvstvshd.chillingfoxes.foxesbot.LauncherKt"
    }
}

idea { // Fixes IntelliJ indexing and build optimisation
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        // (Gradle closed this as fixed, but they broke it again)
        sourceDirs = sourceDirs +
                file("${layout.buildDirectory.get()}/generated/ksp/main/kotlin")

        testSources.setFrom(
            testSources.from + file("${layout.buildDirectory.get()}/generated/ksp/test/kotlin")
        )

        generatedSourceDirs = generatedSourceDirs +
                file("${layout.buildDirectory.get()}/generated/ksp/main/kotlin") +
                file("${layout.buildDirectory.get()}/generated/ksp/test/kotlin")
    }
}

dependencies {
    //discord
    //implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.5-SNAPSHOT")
    implementation("com.sedmelluq:lavaplayer:1.3.78")
    ksp(libs.kord.extensions.processor)

    //logging
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    annotationProcessor("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.slf4j:slf4j-api:2.0.16")

    //database
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.mybatis:mybatis:3.5.16")

    //Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    //some other stuff
    implementation("org.kohsuke:github-api:1.326")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("com.notkamui.libs:keval:0.9.0")
    runtimeOnly(kotlin("scripting-jsr223"))

    //JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
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
    kotlin {
        jvmToolchain(17)
        compilerOptions {
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
        }
    }
}

license {
    header(rootProject.file("LICENSE_HEADER"))
    include("**/*.kt")
    newLine(true)
}