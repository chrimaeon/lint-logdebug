/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.3.31"
    id("com.github.ben-manes.versions") version "0.21.0"
    id("com.jfrog.bintray") version "1.8.4"
}

repositories {
    google()
    jcenter()
}

group = "com.cmgapps.lint"
version = "0.2"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val pomName = "Android Log Lint Checks"
val projectWebsiteUrl = "https://github.com/chrimaeon/lint-logdebug"
val projectArtifactId = "lint-logdebug"

val mavenPublicationName = "bintray"

publishing {
    publications {
        register<MavenPublication>(mavenPublicationName) {

            from(components["java"])

            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            artifactId = projectArtifactId

            pom {
                name.set(pomName)
                description.set("Android Lint checks for log output")
                url.set(projectWebsiteUrl)

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("chrimaeon")
                        name.set("Christian Grach")
                        email.set("christian.grach@cmgapps.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com:chrimaeon/lint-logdebug.git")
                    developerConnection.set("scm:git:git://github.com:chrimaeon/lint-logdebug.git")
                    url.set("https://github.com/chrimaeon/lint-logdebug")
                }
            }
        }
    }
}

bintray {
    val credentialProps = Properties().apply {
        load(FileInputStream(file("${project.rootDir}/credentials.properties")))
    }

    user = credentialProps.getProperty("user")
    key = credentialProps.getProperty("key")

    setPublications(mavenPublicationName)

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "${project.group}:$projectArtifactId"
        userOrg = user
        setLicenses("Apache-2.0")
        vcsUrl = "$projectWebsiteUrl.git"
        issueTrackerUrl = "https://github.com/chrimaeon/lint-logdebug/issues"
        websiteUrl = projectWebsiteUrl
        githubRepo = "chrimaeon/lint-logdebug"
        version(closureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
            vcsTag = project.version as String
            released = Date().toString()
        })
    })
}

tasks {
    named<DependencyUpdatesTask>("dependencyUpdates") {
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

    named<Jar>("jar") {
        manifest {
            attributes("Implementation-Title" to pomName,
                    "Implementation-Version" to project.version.toString(),
                    "Built-By" to System.getProperty("user.name"),
                    "Built-Date" to Date(),
                    "Built-JDK" to System.getProperty("java.version"),
                    "Built-Gradle" to gradle.gradleVersion,
                    "Lint-Registry-v2" to "com.cmgapps.lint.DebugLogIssueRegistry")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val lintVersion = "26.4.1"

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$lintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$lintVersion")

    testImplementation("junit:junit:4.12")
    testImplementation("com.android.tools.lint:lint:$lintVersion")
    testImplementation("com.android.tools.lint:lint-tests:$lintVersion")
    testImplementation("com.android.tools:testutils:$lintVersion")
}
