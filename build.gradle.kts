import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    `java-library`
    kotlin("jvm") version "1.3.31"
    id("com.github.ben-manes.versions") version "0.21.0"
}

repositories {
    google()
    jcenter()
}

group = "com.cmgapps.lint"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    revision = "release"
    resolutionStrategy {
        componentSelection {
            all {
                listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                    .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
                    .any { it.matches(candidate.version) }
                    .let {
                        if (it) {
                            reject("Release candidate")
                        }
                    }
            }
        }
    }
}

val lintVersion = "26.4.1"

configurations {
    register("timberLog")
}

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$lintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$lintVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("junit:junit:4.12")
    testImplementation("com.android.tools.lint:lint:$lintVersion")
    testImplementation("com.android.tools.lint:lint-tests:$lintVersion")
    testImplementation("com.android.tools:testutils:$lintVersion")
    testImplementation(fileTree("src/libs").matching { include("*.jar") })

    "timberLog"("com.jakewharton.timber:timber:4.7.1")
}
