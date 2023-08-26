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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
    alias(libs.plugins.kover)
    id("com.cmgapps.gradle.ktlint")
}

val buildConfigDirPath: Provider<Directory> =
    layout.buildDirectory.dir("generated/source/buildConfig")

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        java.srcDir(buildConfigDirPath)
    }
}

tasks {
    val generateBuildConfig by registering {
        val outputDir = buildConfigDirPath

        val projectArtifactId: String by project
        inputs.property("projectArtifactId", projectArtifactId)

        val feedbackUrl: String by project
        inputs.property("feedbackUrl", feedbackUrl)

        outputs.dir(outputDir)

        doLast {
            outputDir.get().asFile.mkdirs()
            val packageName = "com.cmgapps.lint"
            file(outputDir.get().asFile.resolve("BuildConfig.kt")).bufferedWriter().use {
                it.write(
                    """
                        |package $packageName
                        |const val FEEDBACK_URL = "$feedbackUrl"
                        |const val PROJECT_ARTIFACT = "$projectArtifactId"
                    """.trimMargin(),
                )
            }
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateBuildConfig)
    }

    withType<Test> {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    named<Jar>("jar") {
        manifest {
            val pomName: String by project
            val projectVersionName: String by project
            attributes(
                "Implementation-Title" to pomName,
                "Implementation-Version" to projectVersionName,
                "Built-By" to System.getProperty("user.name"),
                "Built-Date" to Date(),
                "Built-JDK" to System.getProperty("java.version"),
                "Built-Gradle" to gradle.gradleVersion,
                "Lint-Registry-v2" to "com.cmgapps.lint.DebugLogIssueRegistry",
            )
        }
    }
}

koverReport {
    defaults {
        verify {
            rule {
                bound {
                    minValue = 80
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

dependencies {
    compileOnly(libs.android.lintApi)
    compileOnly(libs.android.lintChecks)
    compileOnly(libs.auto.serviceAnnotations)
    // keep as compile only so "stdlibs" are not included as a dependency
    compileOnly(libs.kotlin.stdlib7)
    kapt(libs.auto.serviceAnnotations)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.android.lint)
    testImplementation(libs.android.lintTest)
    testImplementation(libs.android.testutils)
}
