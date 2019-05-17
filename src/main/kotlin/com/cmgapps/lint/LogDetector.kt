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

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UIfExpression
import org.jetbrains.uast.java.JavaUClass
import org.jetbrains.uast.java.JavaUMethod

class LogDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("d", "v")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {

        val evaluator = context.evaluator

        if (!evaluator.isMemberInClass(method, LOG_CLS) && !evaluator.isMemberInClass(method, TIMBER_CLS)) {
            return
        }

        val withinConditional = checkWithinConditional(node)

        if (!withinConditional) {
            val message = "The log call Log.${node.methodName}(...) should be " +
                    "conditional: surround with `if (Log.isLoggable(...))` or " +
                    "`if (BuildConfig.DEBUG) { ... }`"
            context.report(issue, node, context.getLocation(node), message)
        }
    }

    private fun checkWithinConditional(node: UElement): Boolean {
        var curr: UElement? = node
        while (curr != null) {

            if (curr is UIfExpression) {
                if (curr.condition.asSourceString().contains("BuildConfig.DEBUG") ||
                        curr.condition.asSourceString().contains("Log.isLoggable")) {
                    return true
                }
            } else if (curr is JavaUMethod || curr is JavaUClass) {
                break
            }

            curr = curr.uastParent
        }
        return false
    }


    companion object {
        val issue = Issue.Companion.create(
                "LogConditional",
                "Unconditional Logging calls",
                "The BuildConfig class provides a constant, \"DEBUG\", " +
                        "which indicates whether the code is being built in release mode or in debug " +
                        "mode. In release mode, you typically want to strip out all the logging calls. " +
                        "Since the compiler will automatically remove all code which is inside a " +
                        "\"if (false)\" check, surrounding your logging calls with a check for " +
                        "BuildConfig.DEBUG is a good idea.\n\n" +
                        "If you *really* intend for the logging to be present in release mode, you can " +
                        "suppress this warning with a @SuppressLint annotation for the intentional " +
                        "logging calls.",
                Category.PERFORMANCE,
                5,
                Severity.WARNING,
                Implementation(LogDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )

        val issues = arrayOf(issue)

        private const val LOG_CLS = "android.util.Log"
        private const val TIMBER_CLS = "timber.log.Timber"
    }
}