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

package com.cmgapps.lint

import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.TextFormat
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

class DebugLogIssueRegistryShould {

    lateinit var registry: DebugLogIssueRegistry

    @Before
    fun setUp() {
        lint()
        registry = DebugLogIssueRegistry()
    }

    @Test
    fun `check for issue with id LogDebugConditional`() {
        assertThat(registry.issues, contains(hasProperty("id", `is`("LogDebugConditional"))))
    }

    @Test
    fun `set the api to CURRENT_API`() {
        assertThat(registry.api, `is`(CURRENT_API))
    }

    @Test
    fun `set vendor`() {
        assertThat(
            registry.vendor.describe(TextFormat.TEXT),
            `is`(
                """
                    |Vendor: CMG Mobile Apps
                    |Identifier: lint-logdebug
                    |Contact: https://github.com/chrimaeon/lint-logdebug/issues
                    |Feedback: https://github.com/chrimaeon/lint-logdebug/issues
                    |
                """.trimMargin(),
            ),
        )
    }
}
