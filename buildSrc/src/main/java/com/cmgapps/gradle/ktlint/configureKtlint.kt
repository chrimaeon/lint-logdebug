/*
 * Copyright (c) 2020. Christian Grach <christian.grach@cmgapps.com>
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

@file:Suppress("ktlint:standard:filename")

package com.cmgapps.gradle.ktlint

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named

fun Project.configureKtlint() {
    val ktlintConfiguration = configurations.create("ktlint")

    val inputFiles = fileTree("src") {
        include("**/*.kt")
    }
    val outputDir = layout.buildDirectory.dir("reports")

    tasks.register("ktlintFormat", JavaExec::class.java) {
        inputs.files(inputFiles)
        outputs.dir(outputDir)

        group = "Formatting"
        description = "Fix Kotlin code style deviations."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlintConfiguration
        args = listOf("-F", "src/**/*.kt")
    }

    val ktlintTask = tasks.register("ktlint", JavaExec::class.java) {
        inputs.files(inputFiles)
        outputs.dir(outputDir)

        group = "Verification"
        description = "Check Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlintConfiguration
        args = listOf(
            "src/**/*.kt",
            "--reporter=plain",
            "--reporter=html,output=${outputDir.get().asFile.absoluteFile}/ktlint.html",
        )
    }

    tasks.named("check") {
        dependsOn(ktlintTask)
    }

    val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

    dependencies {
        ktlintConfiguration(
            libs.findLibrary("ktlint")
                .orElseThrow { NoSuchElementException("ktlint not found in version catalog") },
        ) {
            attributes {
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            }
        }
    }
}
