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

package com.cmgapps.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class LogDetectorShould {

    @Test
    fun `report missing if statement in java class`() {
        lint()
                .files(java("""
                    |import android.util.Log;
                    |public class Test {
                    |   public void test() {
                    |       Log.d("TestTag", "Message");
                    |   }
                    |}""".trimMargin()))
                .issues(*LogDetector.issues)
                .run()
                .expect("""
                    |src/Test.java:4: Warning: The log call Log.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                    |       Log.d("TestTag", "Message");
                    |       ~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    |0 errors, 1 warnings""".trimMargin())
                .expectFixDiffs("""
                    |Fix for src/Test.java line 3: Surround with `if (BuildConfig.DEBUG)`:
                    |@@ -4 +4
                    |-        Log.d("TestTag", "Message");
                    |+        if (BuildConfig.DEBUG) {
                    |+     Log.d("TestTag", "Message");
                    |+ };
                    |Fix for src/Test.java line 3: Surround with `if (Log.isLoggable(...)`:
                    |@@ -4 +4
                    |-        Log.d("TestTag", "Message");
                    |+        if (Log.isLoggable(TAG, Log.DEBUG) {
                    |+    Log.d("TestTag", "Message");
                    |+ };
                """.trimMargin())
    }

    @Test
    fun `report no errors if nested in BuildConfig DEBUG in java class`() {
        lint()
                .files(java("""
                    |public class Test {
                    |   public void test() {
                    |       if (BuildConfig.DEBUG) {
                    |           android.util.Log.d("TestTag", "Message");
                    |       }
                    |   }
                    |}""".trimMargin()))
                .issues(*LogDetector.issues)
                .run()
                .expect("No warnings.")
    }

    @Test
    fun `report no errors if nested in Log isLoggable in java class`() {
        lint()
                .files(java("""
                    |public class Test {
                    |   public void test() {
                    |       if (android.util.Log.isLoggable("Tag", Log.DEBUG)) {
                    |           android.util.Log.d("TestTag", "Message");
                    |       }
                    |   }
                    |}""".trimMargin()))
                .issues(*LogDetector.issues)
                .run()
                .expect("No warnings.")
    }

    @Test
    fun `report no errors if nested in Log isLoggable and BuildConfig DEBUG in java class`() {
        lint()
                .files(java("""
                    |public class Test {
                    |   public void test() {
                    |       if ( android.util.Log.isLoggable("Tag", Log.DEBUG) || BuildConfig.DEBUG) {
                    |           android.util.Log.d("TestTag", "Message");
                    |       }
                    |   }
                    |}""".trimMargin()))
                .issues(*LogDetector.issues)
                .run()
                .expect("No warnings.")
    }

    @Test
    fun `report missing if statement in kotlin class`() {
        lint()
                .files(kotlin("""
                    |class Test {
                    |   fun test() {
                    |       android.util.Log.d("TestTag", "Message");
                    |   }
                    |}
            """.trimMargin()))
                .issues(*LogDetector.issues)
                .run()
                .expect("""
                    |src/Test.kt:3: Warning: The log call Log.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                    |       android.util.Log.d("TestTag", "Message");
                    |       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    |0 errors, 1 warnings""".trimMargin())
                .expectFixDiffs("""
                    |Fix for src/Test.kt line 3: Surround with `if (BuildConfig.DEBUG)`:
                    |@@ -3 +3
                    |-        android.util.Log.d("TestTag", "Message");
                    |+        if (BuildConfig.DEBUG) {
                    |+     android.util.Log.d("TestTag", "Message")
                    |+ };
                    |Fix for src/Test.kt line 3: Surround with `if (Log.isLoggable(...)`:
                    |@@ -3 +3
                    |-        android.util.Log.d("TestTag", "Message");
                    |+        if (Log.isLoggable(TAG, Log.DEBUG) {
                    |+    android.util.Log.d("TestTag", "Message")
                    |+ };
                """.trimMargin())
    }

    @Test
    fun `report no errors if nested in BuildConfig DEBUG in kotlin class`() {
        lint()
                .files(kotlin("""
                    |class Test {
                    |   fun test() {
                    |       if (BuildConfig.DEBUG) {
                    |           android.util.Log.d("TestTag", "Message");
                    |       }
                    |   }
                    |}""".trimMargin()))
                .issues(*LogDetector.issues)
                .run()
                .expect("No warnings.")
    }

    @Test
    fun `report no errors if nested in Log isLoggable in kotlin class`() {
        lint()
            .files(
                kotlin(
                    """
                    |class Test {
                    |   fun test() {
                    |       if (android.util.Log.isLoggable("Tag", Log.DEBUG)) {
                    |           android.util.Log.d("TestTag", "Message")
                    |       }
                    |   }
                    |}""".trimMargin()
                )
            )
            .issues(*LogDetector.issues)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `report errors if Timber in kotlin class`() {
        lint()
                .files(kotlin("""
                    |class Test {
                    |   fun test() {
                    |       timber.log.Timber.d("TestTag", "Message")
                    |   }
                    |}""".trimMargin()))
                .issues(*LogDetector.issues)
                .run()
            .expect("")
    }

    @Test
    fun `report errors if Timber in java class`() {
        lint()
            .files(
                java(
                    """
                    |import timber.log.Timber;
                    |public class Test {
                    |   public void test() {
                    |       Timber.d("TestTag", "Message");
                    |   }
                    |}""".trimMargin()
                )
            )
            .issues(*LogDetector.issues)
            .run()
            .expect("")
    }
}