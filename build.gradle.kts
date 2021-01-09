plugins {
    kotlin("jvm") version "1.4.10"
    id("maven-publish")
    java
}

val githubToken: String by project
val githubUser: String by project

repositories {
    jcenter()
}

dependencies {
    implementation("io.github.rybalkinsd:kohttp:0.11.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.71")
}

group = "ru.mekosichkin"
version = "1.0-SNAPSHOT"
description = "sberbank-api"
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