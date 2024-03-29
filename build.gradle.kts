plugins {
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
    id("maven-publish")
    java
}

val githubToken: String by project
val githubUser: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.rybalkinsd:kohttp:0.11.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("org.slf4j:slf4j-api:1.7.29")
    implementation("io.github.microutils:kotlin-logging:1+")
}

group = "ru.mekosichkin"
version = "3.0.0"
description = "tinkoff-api"
java.sourceCompatibility = JavaVersion.VERSION_1_8

java {
    withSourcesJar()
}

publishing{
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/MEJIOMAH17/tinkoff-api")
            credentials {
                username = githubUser
                password = githubToken
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}