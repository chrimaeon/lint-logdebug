/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
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

plugins {
    id("com.android.library")
    `maven-publish`
    signing
}

android {
    namespace = "com.cmgapps.lint.logdebug"
    buildToolsVersion = "34.0.0"

    compileSdk = 34
    defaultConfig {
        minSdk = 15
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    publishing {
        singleVariant("release")
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(projectDir.resolve("README.md"))
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(projectDir.resolve("README.md"))
}

val projectGroup: String by project
group = projectGroup
val projectVersionName: String by project
version = projectVersionName

val pomName: String by project
val pomDescription: String by project
val projectWebsiteUrl: String by project
val projectArtifactId: String by project

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("libraryMaven") {
                from(components["release"])

                artifact(sourcesJar.get())
                artifact(javadocJar.get())

                artifactId = projectArtifactId

                pom {
                    name.set(pomName)
                    description.set(pomDescription)
                    url.set(projectWebsiteUrl)

                    issueManagement {
                        url.set("$projectWebsiteUrl/issues")
                        system.set("github")
                    }

                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
                        val pomScmConnection: String by project
                        val pomScmDeveloperConnection: String by project
                        connection.set(pomScmConnection)
                        developerConnection.set(pomScmDeveloperConnection)
                        url.set(projectWebsiteUrl)
                    }
                }
            }
        }

        repositories {
            maven {
                name = "sonatype"
                val releaseUrl =
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (projectVersionName.endsWith("SNAPSHOT")) snapshotUrl else releaseUrl

                val username by credentials()
                val password by credentials()

                credentials {
                    this.username = username
                    this.password = password
                }
            }
        }
    }

    signing {
        sign(publishing.publications["libraryMaven"])
    }
}

dependencies {
    lintPublish(project(":checks"))
}
