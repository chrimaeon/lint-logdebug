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

package com.cmgapps.gradle.ktlint

import Deps
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.invoke

fun Project.configureKtlint() {

    val ktlintConfiguration = configurations.create("ktlint")

    tasks {
        register("ktlintFormat", JavaExec::class.java) {
            group = "Formatting"
            description = "Fix Kotlin code style deviations."
            main = "com.pinterest.ktlint.Main"
            classpath = ktlintConfiguration
            args = listOf("-F", "src/**/*.kt")
        }

        val ktlintTask = register("ktlint", JavaExec::class.java) {
            group = "Verification"
            description = "Check Kotlin code style."
            main = "com.pinterest.ktlint.Main"
            classpath = ktlintConfiguration
            args = listOf(
                "src/**/*.kt",
                "--reporter=plain",
                "--reporter=html,output=${buildDir}/reports/ktlint.html"
            )
        }

        named("check") {
            dependsOn(ktlintTask)
        }
    }

    dependencies.add(ktlintConfiguration.name, Deps.KTLINT)
}
