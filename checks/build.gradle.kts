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
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

plugins {
    kotlin("jvm") version Version.KOTLIN
    kotlin("kapt") version Version.KOTLIN
    jacoco
    id("com.cmgapps.gradle.ktlint")
    id("org.jetbrains.dokka") version Version.DOKKA_PLUGIN
}

@OptIn(ExperimentalPathApi::class)
val buildConfigDirPath = buildDir.toPath() / "generated" / "source" / "buildConfig"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
            outputDir.toFile().mkdirs()
            val packageName = "com.cmgapps.lint"
            file(outputDir.resolve("BuildConfig.kt")).bufferedWriter().use {
                it.write(
                    """
                        |package $packageName
                        |const val FEEDBACK_URL = "$feedbackUrl"
                        |const val PROJECT_ARTIFACT = "$projectArtifactId"
                    """.trimMargin()
                )
            }
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
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
                "Lint-Registry-v2" to "com.cmgapps.lint.DebugLogIssueRegistry"
            )
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    minimum = "0.8".toBigDecimal()
                }
            }
        }
    }
}

dependencies {
    compileOnly(Deps.LINT_API)
    compileOnly(Deps.LINT_CHECKS)
    compileOnly(kotlin("stdlib-jdk8", Version.KOTLIN))
    // Necessary to bump a transitive dependency.
    compileOnly(kotlin("reflect", Version.KOTLIN))

    compileOnly(Deps.AUTO_SERVICE_ANNOTATIONS)
    kapt(Deps.AUTO_SERVICE)

    // Necessary to bump a transitive dependency.
    testImplementation(kotlin("reflect", Version.KOTLIN))

    testImplementation(Deps.JUNIT)
    testImplementation(Deps.HAMCREST)
    testImplementation(Deps.LINT)
    testImplementation(Deps.LINT_TEST)
    testImplementation(Deps.ANDROID_TESTUTILS)
}
