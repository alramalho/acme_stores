plugins {
    kotlin("jvm") version "1.4.30"
}

group = "com.alramalho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

val exposedVersion="0.29.1"
dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.javalin:javalin:3.+")
    implementation("org.slf4j:slf4j-simple:1.+")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.+")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.+")
    testImplementation("io.mockk:mockk:1.+")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
//    implementation("com.impossibl.pgjdbc-ng:pgjdbc-ng:LATEST")
}
tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
