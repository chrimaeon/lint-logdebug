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
import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import org.junit.Test

class LogDebugDetectorShould {
    private val timberStub =
        java(
            """
                package timber.log;
                public class Timber {
                    private Timber() {}

                    public static void d(String message, Object... args) {}
                    public static void v(String message, Object... args) {}
                    public static void e(String message, Object... args) {}
                    public static Tree tag(String tag) {}

                    public static class Tree {
                        public void d(String message, Object... args) {}
                        public void v(String message, Object... args) {}
                        public void e(String message, Object... args) {}
                    }
                }
            """,
        ).indented()

    private val manifestStub = manifest("<manifest package=\"com.cmgapps\"/>")

    @Test
    fun `report missing if statement in java class`() {
        lint().files(
            manifestStub,
            java(
                """
                    import android.util.Log;
                    public class Test {
                        public void test() {
                           Log.d(TAG, "Message");
                        }
                    }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Log.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Log.d(TAG, "Message");
                       ~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Log.d(TAG, "Message");
                +        if (com.cmgapps.BuildConfig.DEBUG) {
                +     Log.d(TAG, "Message");
                + };
                Autofix for src/Test.java line 4: Surround with `if (Log.isLoggable(...))`:
                @@ -4 +4
                -        Log.d(TAG, "Message");
                +        if (Log.isLoggable(TAG, Log.DEBUG)) {
                +     Log.d(TAG, "Message");
                + };
                """,
            )
    }

    @Test
    fun `report no errors if nested in BuildConfig DEBUG in java class`() {
        lint().files(
            java(
                """
                    public class Test {
                       public void test() {
                           if (BuildConfig.DEBUG) {
                               android.util.Log.d("TestTag", "Message");
                           }
                       }
                    }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `report no errors if nested in Log#isLoggable in java class`() {
        lint().files(
            java(
                """
                    public class Test {
                       public void test() {
                           if (android.util.Log.isLoggable("TestTag", Log.DEBUG)) {
                               android.util.Log.d("TestTag", "Message");
                           }
                       }
                    }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `report missing if statement in kotlin class`() {
        lint().files(
            manifestStub,
            kotlin(
                """
                class Test {
                    fun test() {
                        android.util.Log.v("TestTag", "Message")
                    }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .skipTestModes(TestMode.IF_TO_WHEN)
            .run()
            .expect(
                """
                src/Test.kt:3: Warning: The log call Log.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                        android.util.Log.v("TestTag", "Message")
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.kt line 3: Surround with `if (BuildConfig.DEBUG)`:
                @@ -3 +3
                -         android.util.Log.v("TestTag", "Message")
                +         if (com.cmgapps.BuildConfig.DEBUG) {
                +     android.util.Log.v("TestTag", "Message")
                + }
                Autofix for src/Test.kt line 3: Surround with `if (Log.isLoggable(...))`:
                @@ -3 +3
                -         android.util.Log.v("TestTag", "Message")
                +         if (android.util.Log.isLoggable("TestTag", Log.VERBOSE)) {
                +     android.util.Log.v("TestTag", "Message")
                + }
                """,
            )
    }

    @Test
    fun `report no errors if nested in BuildConfig DEBUG in kotlin class`() {
        lint().files(
            kotlin(
                """
                class Test {
                   fun test() {
                       if (BuildConfig.DEBUG) {
                           android.util.Log.v("TestTag", "Message")
                       }
                   }
                }""",
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .skipTestModes(TestMode.IF_TO_WHEN)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `report no errors if nested in Log isLoggable in kotlin class`() {
        lint().files(
            kotlin(
                """
                class Test {
                   fun test() {
                       if (android.util.Log.isLoggable("TestTag", Log.DEBUG)) {
                           android.util.Log.d("TestTag", "Message")
                       }
                   }
                }""",
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .skipTestModes(TestMode.IF_TO_WHEN)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `report errors if Timber in kotlin class`() {
        lint().files(
            timberStub,
            manifestStub,
            kotlin(
                """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.d("Message")
                   }
                }""",
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Timber.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.d("Message")
                       ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Fix for src/Test.kt line 3: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Timber.d("Message")
                +        if (com.cmgapps.BuildConfig.DEBUG) {
                +     Timber.d("Message")
                + }
                """,
            )
    }

    @Test
    fun `report errors if Timber in java class`() {
        lint().files(
            timberStub,
            manifestStub,
            java(
                """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.d("Message");
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.d("Message");
                       ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Timber.d("Message");
                +        if (com.cmgapps.BuildConfig.DEBUG) {
                +     Timber.d("Message");
                + };
                """,
            )
    }

    @Test
    fun `report errors if Timber in java class for verbose`() {
        lint().files(
            timberStub,
            manifestStub,
            java(
                """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.v("Message");
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.v("Message");
                       ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Timber.v("Message");
                +        if (com.cmgapps.BuildConfig.DEBUG) {
                +     Timber.v("Message");
                + };
                """,
            )
    }

    @Test
    fun `report errors if Timber in java class for verbose and tag`() {
        lint().files(
            timberStub,
            manifestStub,
            java(
                """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.tag("TestTag").v("Message");
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag").v("Message");
                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Timber.tag("TestTag").v("Message");
                +        if (com.cmgapps.BuildConfig.DEBUG) {
                +     Timber.tag("TestTag").v("Message");
                + };
                """,
            )
    }

    @Test
    fun `report errors if Timber in kotlin class for verbose and tag`() {
        lint().files(
            timberStub,
            manifestStub,
            kotlin(
                """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.tag("TestTag").v("Message")
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag").v("Message")
                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.kt line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Timber.tag("TestTag").v("Message")
                +        if (com.cmgapps.BuildConfig.DEBUG) {
                +     Timber.tag("TestTag").v("Message")
                + }
                """,
            )
    }

    @Test
    fun `report errors if Timber in java class for verbose and tag with new line`() {
        lint().files(
            timberStub,
            manifestStub,
            java(
                """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.tag("TestTag")
                           .v("Message");
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag")
                       ^
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Timber.tag("TestTag")
                -            .v("Message");
                +         if (com.cmgapps.BuildConfig.DEBUG) {
                +      Timber.tag("TestTag")
                + .v("Message");
                +  };
                """,
            )
    }

    @Test
    fun `report errors if Timber in kotlin class for verbose and tag with new line`() {
        lint().files(
            timberStub,
            manifestStub,
            kotlin(
                """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.tag("TestTag")
                           .v("Message")
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag")
                       ^
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.kt line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Timber.tag("TestTag")
                -            .v("Message")
                +         if (com.cmgapps.BuildConfig.DEBUG) {
                +      Timber.tag("TestTag")
                + .v("Message")
                +  }
                """,
            )
    }

    @Test
    fun `not check 'd' method from unknown class`() {
        lint().files(
            kotlin(
                """
                class CustomClass {
                    fun d(text: String) {
                        println(text)
                    }
                }

                class OtherClass {
                    fun callD() {
                        val test = CustomClass()
                        test.d("Test")
                    }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect("No warnings.")
            .expectFixDiffs("")
    }

    @Test
    fun `not check if error log with 'e'`() {
        lint().files(
            timberStub,
            manifestStub,
            kotlin(
                """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.tag("TestTag")
                           .e("Error")
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect("No warnings.")
            .expectFixDiffs("")
    }

    @Test
    fun `render quickfix with elvis operator correctly`() {
        lint().files(
            timberStub,
            manifestStub,
            kotlin(
                """
                import android.util.Log
                class Test {
                   fun test() {
                       Log.d("TestTag", null ?: "Message")
                   }
                }
                """,
            ).indented(),
        ).issues(*LogDebugDetector.issues)
            .run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Log.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Log.d("TestTag", null ?: "Message")
                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            )
            .expectFixDiffs(
                """
                Autofix for src/Test.kt line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -4 +4
                -        Log.d("TestTag", null ?: "Message")
                +        if (com.cmgapps.BuildConfig.DEBUG) {
                +     Log.d("TestTag", null ?: "Message")
                + }
                Autofix for src/Test.kt line 4: Surround with `if (Log.isLoggable(...))`:
                @@ -4 +4
                -        Log.d("TestTag", null ?: "Message")
                +        if (Log.isLoggable("TestTag", Log.DEBUG)) {
                +     Log.d("TestTag", null ?: "Message")
                + }
                """,
            )
    }
}
